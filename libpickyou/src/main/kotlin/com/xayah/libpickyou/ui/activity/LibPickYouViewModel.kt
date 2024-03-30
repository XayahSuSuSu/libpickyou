package com.xayah.libpickyou.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_OPEN_DOCUMENT_TREE
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.topjohnwu.superuser.Shell
import com.xayah.libpickyou.parcelables.DirChildrenParcelable
import com.xayah.libpickyou.ui.PickYouLauncher
import com.xayah.libpickyou.ui.model.PickerType
import com.xayah.libpickyou.ui.tokens.LibPickYouTokens
import com.xayah.libpickyou.ui.tokens.LibPickYouTokens.ContentShowAdvanced
import com.xayah.libpickyou.ui.tokens.LibPickYouTokens.DocumentAuthority
import com.xayah.libpickyou.ui.tokens.LibPickYouTokens.DocumentUriAndroidData
import com.xayah.libpickyou.ui.tokens.LibPickYouTokens.DocumentUriAndroidObb
import com.xayah.libpickyou.ui.tokens.LibPickYouTokens.ProviderShowAdvanced
import com.xayah.libpickyou.ui.tokens.LibPickYouTokens.SpecialPathAndroidData
import com.xayah.libpickyou.ui.tokens.LibPickYouTokens.SpecialPathAndroidObb
import com.xayah.libpickyou.util.PathUtil
import com.xayah.libpickyou.util.RemoteRootService
import com.xayah.libpickyou.util.registerForActivityResultCompat
import com.xayah.libpickyou.util.toPath
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.atomic.AtomicInteger

internal data class IndexUiState(
    val path: List<String>,
    val selection: List<String> = listOf(),
    val children: DirChildrenParcelable = DirChildrenParcelable(),
    val type: PickerType,
    val limitation: Int,
    val title: String,
    val pathPrefixHiddenNum: Int,
    val refreshState: Boolean,
    val safOnSpecialPath: Boolean,
) : UiState {
    val canUp: Boolean
        get() = isAccessible(path.toMutableList().apply { removeLast() })

    val pathString: String
        get() = run {
            val newPath = path.toMutableList()
            repeat(pathPrefixHiddenNum) {
                newPath.removeFirstOrNull()
            }
            newPath.toPath()
        }

    val selectedItems: String
        get() = selection.joinToString(separator = LibPickYouTokens.SelectedItemsSeparator)
}

internal sealed class IndexUiIntent : UiIntent {
    data class Enter(val context: Context, val item: String) : IndexUiIntent()
    data class Exit(val context: Context) : IndexUiIntent()
    data class JumpToList(val context: Context, val target: List<String>) : IndexUiIntent()
    data class JumpTo(val context: Context, val target: String) : IndexUiIntent()
    data class UpdateChildren(val children: DirChildrenParcelable) : IndexUiIntent()
    data class JoinSelection(val name: String) : IndexUiIntent()
    data class RemoveSelection(val name: String) : IndexUiIntent()
    object Refresh : IndexUiIntent()

    data class RequestSpecialDir(val context: Context, val specialPath: String, val documentUri: String, val name: String) : IndexUiIntent()
    data class SetExceptionMessage(val msg: String?) : IndexUiIntent()
}

internal class LibPickYouViewModel(
    path: List<String>,
    type: PickerType,
    limitation: Int,
    title: String,
    pathPrefixHiddenNum: Int,
    safOnSpecialPath: Boolean,
) : BaseViewModel<IndexUiState, IndexUiIntent, IndexUiEffect>(
    IndexUiState(
        path = path,
        type = type,
        limitation = limitation,
        title = title,
        pathPrefixHiddenNum = pathPrefixHiddenNum,
        refreshState = true,
        safOnSpecialPath = safOnSpecialPath,
    )
) {
    companion object {
        @Suppress("UNCHECKED_CAST")
        class Factory(
            private val path: List<String>,
            private val type: PickerType,
            private val limitation: Int,
            private val title: String,
            private val pathPrefixHiddenNum: Int,
            private val safOnSpecialPath: Boolean,
        ) : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                return LibPickYouViewModel(
                    path = path,
                    type = type,
                    limitation = limitation,
                    title = title,
                    pathPrefixHiddenNum = pathPrefixHiddenNum,
                    safOnSpecialPath = safOnSpecialPath,
                ) as T
            }
        }
    }

    lateinit var remoteRootService: RemoteRootService

    private var _documentUriState: MutableStateFlow<Uri?> = MutableStateFlow(null)
    val documentUriState: StateFlow<Uri?> = _documentUriState.stateInScope(null)

    private var _exceptionMessageState: MutableStateFlow<String?> = MutableStateFlow(null)
    val exceptionMessageState: StateFlow<String?> = _exceptionMessageState.stateInScope(null)

    override suspend fun onEvent(state: IndexUiState, intent: IndexUiIntent) {
        when (intent) {
            is IndexUiIntent.Enter -> {
                if (intent.item.isEmpty()) return
                val path = state.path.toMutableList()
                path.add(intent.item)
                emitStateSuspend(state.copy(path = path.toList()))

                checkSpecialPath(intent.context, path)
            }

            is IndexUiIntent.Exit -> {
                if (!state.canUp) return
                val path = state.path.toMutableList()
                path.removeLast()
                onAccessible(path) {
                    emitState(state.copy(path = path.toList()))
                }

                checkSpecialPath(intent.context, path)
            }

            is IndexUiIntent.JumpToList -> {
                if (intent.target.isEmpty()) return
                onAccessible(intent.target) {
                    emitState(state.copy(path = intent.target))
                }

                checkSpecialPath(intent.context, intent.target)
            }

            is IndexUiIntent.JumpTo -> {
                emitIntent(IndexUiIntent.JumpToList(intent.context, intent.target.split(LibPickYouTokens.PathSeparator)))
            }

            is IndexUiIntent.UpdateChildren -> {
                emitState(state.copy(children = intent.children))
            }

            is IndexUiIntent.JoinSelection -> {
                if (intent.name.isEmpty() || isItemSelected(intent.name)) return
                val selection = state.selection.toMutableList()
                selection.add(getPathString(intent.name))
                emitState(state.copy(selection = selection.toList()))
            }

            is IndexUiIntent.RemoveSelection -> {
                val index = state.selection.indexOf(getPathString(intent.name))
                if (index == -1) return
                val selection = state.selection.toMutableList()
                selection.removeAt(index)
                emitState(state.copy(selection = selection.toList()))
            }

            is IndexUiIntent.Refresh -> {
                emitState(state.copy(refreshState = state.refreshState.not()))
            }

            is IndexUiIntent.RequestSpecialDir -> {
                val context = intent.context
                val name = intent.name.replace("${intent.specialPath}/", "")
                val documentUri = DocumentsContract.buildDocumentUri(DocumentAuthority, "${intent.documentUri}/$name")
                val treeUri = DocumentsContract.buildTreeDocumentUri(DocumentAuthority, "${intent.documentUri}/$name")
                val documentFile = DocumentFile.fromTreeUri(context, treeUri)
                val canRead = documentFile?.canRead() ?: false
                val canWrite = documentFile?.canWrite() ?: false
                if (canRead && canWrite) {
                    _documentUriState.value = treeUri
                } else {
                    withMainContext {
                        context.registerForActivityResultCompat(
                            AtomicInteger(),
                            ActivityResultContracts.StartActivityForResult()
                        ) { result: ActivityResult ->
                            if (result.resultCode == Activity.RESULT_OK) {
                                if (result.data?.data != null) {
                                    _documentUriState.value = treeUri
                                    val data = result.data?.data!!
                                    val flags = result.data?.flags!!
                                    context.contentResolver.takePersistableUriPermission(data, flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION))

                                    _exceptionMessageState.value = "Mismatch: $data and $treeUri"
                                }
                            }
                        }.launch(
                            Intent(ACTION_OPEN_DOCUMENT_TREE)
                                .addFlags(
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                                            or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                            or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                                            or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
                                )
                                .putExtra(ProviderShowAdvanced, true)
                                .putExtra(ContentShowAdvanced, true)
                                .putExtra(DocumentsContract.EXTRA_INITIAL_URI, documentUri)
                        )
                    }
                }
            }

            is IndexUiIntent.SetExceptionMessage -> {
                _exceptionMessageState.value = intent.msg
            }
        }
    }

    private suspend fun checkSpecialPath(context: Context, path: List<String>) {
        if (PathUtil.underSpecialPathAndroidData(path)) {
            emitIntentSuspend(IndexUiIntent.RequestSpecialDir(context, SpecialPathAndroidData.toPath(), DocumentUriAndroidData, path.toPath()))
        } else if (PathUtil.underSpecialPathAndroidObb(path)) {
            emitIntentSuspend(IndexUiIntent.RequestSpecialDir(context, SpecialPathAndroidObb.toPath(), DocumentUriAndroidObb, path.toPath()))
        } else {
            _documentUriState.value = null
        }
    }

    private fun getPathString(name: String? = null): String {
        val uiState = uiState.value

        return if (!name.isNullOrEmpty()) {
            "${uiState.pathString}/$name"
        } else
            uiState.pathString
    }

    fun isItemSelected(name: String): Boolean {
        return uiState.value.selection.indexOf(getPathString(name)) != -1
    }

    private fun onAccessible(path: List<String>, block: () -> Unit) {
        if (isAccessible(path)) block()
    }
}

private fun isAccessible(path: List<String>): Boolean {
    if (path.isEmpty()) return false
    val rootPath = PickYouLauncher.rootPathList.toPath()
    if (rootPath in path.toPath()) return true
    return if (PickYouLauncher.isRootMode) Shell.getShell().isRoot else false
}
