package com.xayah.libpickyou.ui

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_OPEN_DOCUMENT_TREE
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.documentfile.provider.DocumentFile
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.topjohnwu.superuser.Shell
import com.xayah.libpickyou.PickYouLauncher
import com.xayah.libpickyou.R
import com.xayah.libpickyou.parcelables.DirChildrenParcelable
import com.xayah.libpickyou.parcelables.FileParcelable
import com.xayah.libpickyou.ui.components.DialogState
import com.xayah.libpickyou.ui.model.PickerType
import com.xayah.libpickyou.ui.model.isRoot
import com.xayah.libpickyou.ui.model.isStorage
import com.xayah.libpickyou.ui.tokens.LibPickYouTokens
import com.xayah.libpickyou.ui.tokens.LibPickYouTokens.CONTENT_SHOW_ADVANCED
import com.xayah.libpickyou.ui.tokens.LibPickYouTokens.DOCUMENT_AUTHORITY
import com.xayah.libpickyou.ui.tokens.LibPickYouTokens.DOCUMENT_URI_ANDROID_DATA
import com.xayah.libpickyou.ui.tokens.LibPickYouTokens.DOCUMENT_URI_ANDROID_OBB
import com.xayah.libpickyou.ui.tokens.LibPickYouTokens.PATH_SEPARATOR
import com.xayah.libpickyou.ui.tokens.LibPickYouTokens.PROVIDER_SHOW_ADVANCED
import com.xayah.libpickyou.ui.tokens.LibPickYouTokens.SpecialPathAndroidData
import com.xayah.libpickyou.ui.tokens.LibPickYouTokens.SpecialPathAndroidObb
import com.xayah.libpickyou.util.PathUtil
import com.xayah.libpickyou.util.PathUtil.isSpecialPathAndroid
import com.xayah.libpickyou.util.PathUtil.isSpecialPathAndroidData
import com.xayah.libpickyou.util.PathUtil.isSpecialPathAndroidObb
import com.xayah.libpickyou.util.PermissionUtil
import com.xayah.libpickyou.util.PermissionUtil.Companion.checkStoragePermissions
import com.xayah.libpickyou.util.PreferencesUtil
import com.xayah.libpickyou.util.RemoteRootService
import com.xayah.libpickyou.util.registerForActivityResultCompat
import com.xayah.libpickyou.util.toPath
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicInteger
import com.xayah.libpickyou.PickYouLauncher.Companion.sPickYouLauncher as Launcher

internal data class IndexUiState(
    val pathList: List<String> = Launcher.defaultPathList,
    val title: String = Launcher.title,
    val pickerType: PickerType = Launcher.pickerType,
    val children: DirChildrenParcelable = DirChildrenParcelable(),
    val pathPrefixHiddenNum: Int = Launcher.pathPrefixHiddenNum,
    val canUp: Boolean = false,
    val isLoading: Boolean = true,
    val hasPermissions: Boolean = true,
    val firstResume: Boolean = true,
    val showBottomSheet: Boolean = false,
    val grantTimes: Int = 0,
) : UiState {
    val pathString: String
        get() = run {
            val newPath = pathList.toMutableList()
            repeat(pathPrefixHiddenNum) {
                newPath.removeFirstOrNull()
            }
            newPath.toPath()
        }
}

@ExperimentalPermissionsApi
internal sealed class IndexUiIntent : UiIntent {
    data class InitRootService(val context: ComponentActivity) : IndexUiIntent()
    data class UpdatePathList(val context: Context) : IndexUiIntent()
    data class Enter(val context: Context, val item: FileParcelable) : IndexUiIntent()
    data class Exit(val context: Context) : IndexUiIntent()
    data class JumpToList(val context: Context, val target: List<String>) : IndexUiIntent()
    data class OnResult(val context: ComponentActivity, val name: String?) : IndexUiIntent()
    data class OnCreatingDir(val context: Context, val dialogState: DialogState) : IndexUiIntent()
    data class GrantPermissions(val context: Context, val permissionsState: MultiplePermissionsState) : IndexUiIntent()
    data class OnPermissionsChanged(val context: Context, val permissionsState: MultiplePermissionsState) : IndexUiIntent()

    data class RequestSpecialDir(val context: Context, val specialPath: String, val documentUri: String, val name: String) : IndexUiIntent()
    data class SetExceptionMessage(val msg: String?) : IndexUiIntent()
}

@ExperimentalPermissionsApi
internal class LibPickYouViewModel : BaseViewModel<IndexUiState, IndexUiIntent, UiEffect>(IndexUiState()) {
    lateinit var remoteRootService: RemoteRootService
    private val mutex = Mutex()

    private var _documentUriState: MutableStateFlow<Uri?> = MutableStateFlow(null)
    val documentUriState: StateFlow<Uri?> = _documentUriState.stateInScope(null)

    private var _exceptionMessageState: MutableStateFlow<String?> = MutableStateFlow(null)
    val exceptionMessageState: StateFlow<String?> = _exceptionMessageState.stateInScope(null)

    override suspend fun onEvent(state: IndexUiState, intent: IndexUiIntent) {
        when (intent) {
            is IndexUiIntent.InitRootService -> {
                mutex.withLock {
                    withMainContext {
                        if (this@LibPickYouViewModel::remoteRootService.isInitialized.not()) {
                            remoteRootService = RemoteRootService(intent.context)
                        }
                    }
                }
            }

            is IndexUiIntent.UpdatePathList -> {
                val context = intent.context
                emitState(uiState.value.copy(isLoading = true))
                runCatching {
                    val children: DirChildrenParcelable
                    val path = uiState.value.pathList.toPath()
                    children = PickYouLauncher.sPickYouLauncher.traverseBackend?.invoke(path) ?: if (PickYouLauncher.sIsRootMode) {
                        remoteRootService.traverse(path)
                    } else {
                        if (documentUriState.value != null) {
                            PathUtil.traverse(DocumentFile.fromTreeUri(context, documentUriState.value!!)!!)
                        } else {
                            if (isSpecialPathAndroid(path)) {
                                PathUtil.traverseSpecialPathAndroid(path)
                            } else if (isSpecialPathAndroidData(path) || isSpecialPathAndroidObb(
                                    path
                                )
                            ) {
                                PathUtil.traverseSpecialPathAndroidDataOrObb(path, context.packageManager)
                            } else {
                                PathUtil.traverse(path)
                            }
                        }
                    }
                    emitIntent(IndexUiIntent.SetExceptionMessage(null))
                    emitState(state.copy(children = children, canUp = isAccessible(uiState.value.pathList.toMutableList().apply { removeLast() })))
                }.onFailure {
                    emitIntent(IndexUiIntent.SetExceptionMessage(it.localizedMessage))
                }
                emitState(uiState.value.copy(isLoading = false))
            }

            is IndexUiIntent.Enter -> {
                val item = intent.item
                val context = intent.context

                if (item.link.isNullOrEmpty().not()) {
                    emitIntent(IndexUiIntent.JumpToList(context, item.link!!.split(PATH_SEPARATOR)))
                } else {
                    if (item.name.isEmpty()) return
                    val path = state.pathList.toMutableList()
                    path.add(item.name)
                    emitState(state.copy(pathList = path.toList()))

                    checkSpecialPath(context, path)
                    emitIntent(IndexUiIntent.UpdatePathList(context))
                }
            }

            is IndexUiIntent.Exit -> {
                val context = intent.context
                if (!state.canUp) return
                val path = state.pathList.toMutableList()
                path.removeLast()
                onAccessible(path) {
                    emitState(state.copy(pathList = path.toList()))
                }

                checkSpecialPath(context, path)
                emitIntent(IndexUiIntent.UpdatePathList(context))
            }

            is IndexUiIntent.JumpToList -> {
                val target = intent.target
                val context = intent.context
                val link = remoteRootService.getSymbolicLink(target.toPath())
                if (link != null) {
                    emitIntent(IndexUiIntent.JumpToList(context, link.split(PATH_SEPARATOR)))
                } else {
                    if (intent.target.isEmpty()) return
                    onAccessible(intent.target) {
                        emitState(state.copy(pathList = intent.target))
                    }

                    checkSpecialPath(context, intent.target)
                    emitIntent(IndexUiIntent.UpdatePathList(context))
                }
            }

            is IndexUiIntent.RequestSpecialDir -> {
                val context = intent.context
                val name = intent.name.replace("${intent.specialPath}/", "")
                val documentUri = DocumentsContract.buildDocumentUri(DOCUMENT_AUTHORITY, "${intent.documentUri}/$name")
                val treeUri = DocumentsContract.buildTreeDocumentUri(DOCUMENT_AUTHORITY, "${intent.documentUri}/$name")
                val documentFile = DocumentFile.fromTreeUri(context, treeUri)
                val canRead = documentFile?.canRead() ?: false
                val canWrite = documentFile?.canWrite() ?: false
                if (canRead && canWrite) {
                    _documentUriState.value = treeUri
                } else {
                    withMainContext {
                        val newIntent = Intent(ACTION_OPEN_DOCUMENT_TREE)
                            .addFlags(
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                        or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                                        or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
                            )
                            .putExtra(PROVIDER_SHOW_ADVANCED, true)
                            .putExtra(CONTENT_SHOW_ADVANCED, true)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            newIntent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, documentUri)
                        }
                        context.registerForActivityResultCompat(
                            AtomicInteger(),
                            ActivityResultContracts.StartActivityForResult()
                        ) { result: ActivityResult ->
                            if (result.resultCode == RESULT_OK) {
                                if (result.data?.data != null) {
                                    _documentUriState.value = treeUri
                                    val data = result.data?.data!!
                                    val flags = result.data?.flags!!
                                    context.contentResolver.takePersistableUriPermission(data, flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION))

                                    _exceptionMessageState.value = "Mismatch: $data and $treeUri"
                                }
                            }
                        }.launch(newIntent)
                    }
                }
            }

            is IndexUiIntent.SetExceptionMessage -> {
                _exceptionMessageState.value = intent.msg
            }

            is IndexUiIntent.OnResult -> {
                val context = intent.context
                val name = intent.name
                Intent().apply {
                    val path = uiState.value.pathList.toMutableList()
                    if (name != null) path.add(name)
                    putExtra(LibPickYouTokens.INTENT_EXTRA_PATH, path.toPath())
                    context.setResult(RESULT_OK, this)
                }
                withMainContext {
                    remoteRootService.destroyService()
                }
                context.finish()
            }

            is IndexUiIntent.OnCreatingDir -> {
                val context = intent.context
                val parent = uiState.value.pathString

                val (dismissState, child) = intent.dialogState.openEdit(
                    title = context.getString(R.string.create_folder),
                    icon = ImageVector.vectorResource(theme = context.theme, res = context.resources, resId = R.drawable.ic_rounded_folder_open),
                    label = context.getString(R.string.name),
                )
                if (dismissState.isConfirm) {
                    val result = Launcher.mkdirsBackend?.invoke(parent, child)
                        ?: if (PickYouLauncher.sIsRootMode) {
                            remoteRootService.mkdirs("$parent/$child")
                        } else {
                            PathUtil.mkdirs("$parent/$child")
                        }
                    Toast.makeText(
                        context,
                        if (result) {
                            context.getString(R.string.created)
                        } else {
                            context.getString(R.string.failed_to_create)
                        } + ": $child", Toast.LENGTH_SHORT
                    ).show()
                }
            }

            is IndexUiIntent.GrantPermissions -> {
                emitState(uiState.value.copy(grantTimes = uiState.value.grantTimes + 1))
                val context = intent.context
                val permissionsState = intent.permissionsState
                if (Launcher.permissionType.isRoot()) {
                    RemoteRootService.initService()
                    PreferencesUtil.saveRequestedRoot(Shell.getShell().isRoot)
                    emitIntent(IndexUiIntent.OnPermissionsChanged(context, permissionsState))
                }
                if (Launcher.permissionType.isStorage()) {
                    if (checkStoragePermissions(intent.permissionsState, Launcher.permissionType).not()) PermissionUtil.requestStoragePermissions(context, permissionsState)
                }
            }

            is IndexUiIntent.OnPermissionsChanged -> {
                mutex.withLock {
                    val hasPermissions = checkStoragePermissions(intent.permissionsState, Launcher.permissionType)
                    if (hasPermissions.not()) {
                        emitState(uiState.value.copy(showBottomSheet = true))
                    } else {
                        if (uiState.value.firstResume.not()) emitState(uiState.value.copy(showBottomSheet = false))
                    }
                    if (uiState.value.firstResume) {
                        emitState(uiState.value.copy(firstResume = false))
                    }
                }
            }
        }
    }

    private suspend fun checkSpecialPath(context: Context, path: List<String>) {
        if (PickYouLauncher.sIsRootMode.not() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (PathUtil.underSpecialPathAndroidData(path)) {
                emitIntent(
                    IndexUiIntent.RequestSpecialDir(context = context, specialPath = SpecialPathAndroidData.toPath(), documentUri = DOCUMENT_URI_ANDROID_DATA, name = path.toPath())
                )
            } else if (PathUtil.underSpecialPathAndroidObb(path)) {
                emitIntent(IndexUiIntent.RequestSpecialDir(context, SpecialPathAndroidObb.toPath(), DOCUMENT_URI_ANDROID_OBB, path.toPath()))
            } else {
                _documentUriState.value = null
            }
        }
    }

    private suspend fun onAccessible(path: List<String>, block: suspend () -> Unit) {
        if (isAccessible(path)) block()
    }
}

fun isAccessible(path: List<String>): Boolean {
    if (path.isEmpty()) return false
    val rootPath = PickYouLauncher.sPickYouLauncher.rootPathList.toPath()
    if (rootPath in path.toPath()) return true
    return if (PickYouLauncher.sIsRootMode) Shell.getShell().isRoot else false
}
