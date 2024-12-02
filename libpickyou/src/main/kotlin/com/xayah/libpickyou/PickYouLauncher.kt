package com.xayah.libpickyou

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.xayah.libpickyou.parcelables.DirChildrenParcelable
import com.xayah.libpickyou.ui.LibPickYouActivity
import com.xayah.libpickyou.ui.model.PermissionType
import com.xayah.libpickyou.ui.model.PickerType
import com.xayah.libpickyou.ui.model.isRoot
import com.xayah.libpickyou.ui.tokens.LibPickYouTokens
import com.xayah.libpickyou.util.PreferencesUtil
import com.xayah.libpickyou.util.ThemeType
import com.xayah.libpickyou.util.registerForActivityResultCompat
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Pick you launcher
 *
 * @property title The title on the top of activity.
 * @property pickerType [PickerType.FILE] | [PickerType.DIRECTORY]
 * @property permissionType [PermissionType.NORMAL] | [PermissionType.ROOT]
 * @property checkPermission Check permissions first
 * @property pathPrefixHiddenNum Correct the return path according to the value and affect the top chips display
 * @property defaultPathList The default path
 * @property rootPathList The root path
 * @property traverseBackend Custom traverse backend
 * @property mkdirsBackend Custom mkdirs() backend
 * @property dynamicColor Whether to enable monet
 * @property themeType [ThemeType.AUTO] | [ThemeType.LIGHT_THEME] | [ThemeType.DARK_THEME]
 */
@OptIn(ExperimentalPermissionsApi::class)
class PickYouLauncher(
    var title: String = LibPickYouTokens.STRING_PLACEHOLDER,
    var pickerType: PickerType = PickerType.FILE,
    var permissionType: PermissionType = PermissionType.NORMAL,
    var checkPermission: Boolean = true,
    var pathPrefixHiddenNum: Int = LibPickYouTokens.PATH_PREFIX_HIDDEN_NUM,
    var defaultPathList: List<String> = LibPickYouTokens.DefaultPathList,
    var rootPathList: List<String> = LibPickYouTokens.DefaultPathList,
    var traverseBackend: (suspend (pathString: String) -> DirChildrenParcelable)? = null,
    var mkdirsBackend: (suspend (parent: String, child: String) -> Boolean)? = null,
    private val dynamicColor: Boolean = true,
    private val themeType: ThemeType = ThemeType.AUTO,
) {
    private lateinit var mLauncher: ActivityResultLauncher<Intent>
    private val mNextLocalRequestCode = AtomicInteger()

    companion object {
        internal val sIsRootMode: Boolean
            get() = sPickYouLauncher.permissionType.isRoot() && PreferencesUtil.readRequestedRoot()
        internal var sPickYouLauncher = PickYouLauncher()

        private fun onResult(result: ActivityResult, onResult: (path: String) -> Unit) {
            if (result.resultCode == Activity.RESULT_OK) {
                onResult(result.data?.getStringExtra(LibPickYouTokens.INTENT_EXTRA_PATH) ?: "")
            }
        }

        private fun getLauncher(context: Context, requestCode: AtomicInteger, onPathResult: (path: String) -> Unit): ActivityResultLauncher<Intent> =
            context.registerForActivityResultCompat(
                requestCode,
                ActivityResultContracts.StartActivityForResult()
            ) { result: ActivityResult ->
                onResult(result, onPathResult)
            }

        private fun getLauncher(context: Context, requestCode: AtomicInteger, cont: CancellableContinuation<String>): ActivityResultLauncher<Intent> =
            context.registerForActivityResultCompat(
                requestCode,
                ActivityResultContracts.StartActivityForResult()
            ) { result: ActivityResult ->
                if (cont.isActive.not()) {
                    return@registerForActivityResultCompat
                }
                if (result.resultCode == Activity.RESULT_OK) {
                    val r = result.data?.getStringExtra(LibPickYouTokens.INTENT_EXTRA_PATH)
                    if (r != null) {
                        cont.resume(r)
                    } else {
                        cont.resumeWithException(CancellationException("Launcher returned empty list."))
                    }
                } else if (result.resultCode == Activity.RESULT_CANCELED) {
                    cont.resumeWithException(CancellationException("Launcher got cancelled."))
                }
            }
    }

    private fun launch(context: Context) {
        PreferencesUtil.saveDynamicColor(dynamicColor)
        PreferencesUtil.saveThemeType(themeType)
        sPickYouLauncher = this
        mLauncher.launch(Intent(context, LibPickYouActivity::class.java))
    }

    fun launch(context: Context, onResult: (path: String) -> Unit) {
        mLauncher = getLauncher(context, mNextLocalRequestCode, onResult)
        launch(context)
    }

    suspend fun awaitLaunch(context: Context): String = suspendCancellableCoroutine { cont ->
        // There is no cancellation support since we cannot cancel a launched Intent
        mLauncher = getLauncher(context, mNextLocalRequestCode, cont)
        launch(context)
    }
}
