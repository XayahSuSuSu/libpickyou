package com.xayah.libpickyou.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.xayah.libpickyou.parcelables.DirChildrenParcelable
import com.xayah.libpickyou.ui.activity.LibPickYouActivity
import com.xayah.libpickyou.ui.model.PermissionType
import com.xayah.libpickyou.ui.model.PickerType
import com.xayah.libpickyou.ui.tokens.LibPickYouTokens
import com.xayah.libpickyou.util.registerForActivityResultCompat
import kotlinx.coroutines.CancellationException
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class PickYouLauncher {
    private lateinit var mLauncher: ActivityResultLauncher<Intent>
    private var mTraverseBackend: ((path: Path) -> DirChildrenParcelable)? = null
    private var mPermissionType: PermissionType = PermissionType.NORMAL
    private val mNextLocalRequestCode = AtomicInteger()
    private var mDefaultPathList = LibPickYouTokens.DefaultPathList
    private var mPickerType = PickerType.FILE
    private var mLimitation = LibPickYouTokens.NoLimitation
    private var mTitle = LibPickYouTokens.StringPlaceHolder
    private var mPathPrefixHiddenNum = LibPickYouTokens.PathPrefixHiddenNum

    companion object {
        internal var traverseBackend: ((path: Path) -> DirChildrenParcelable)? = null
        internal var permissionType: PermissionType = PermissionType.NORMAL
        internal var defaultPathList = LibPickYouTokens.DefaultPathList
        internal var pickerType = PickerType.FILE
        internal var limitation = LibPickYouTokens.NoLimitation
        internal var title = LibPickYouTokens.StringPlaceHolder
        internal var pathPrefixHiddenNum = LibPickYouTokens.PathPrefixHiddenNum

        private fun onResult(result: ActivityResult, onResult: (path: List<String>) -> Unit) {
            if (result.resultCode == Activity.RESULT_OK) {
                onResult(result.data?.getStringArrayListExtra(LibPickYouTokens.IntentExtraPath)?.toList() ?: listOf())
            }
        }

        private fun getLauncher(context: Context, requestCode: AtomicInteger, onPathResult: (path: List<String>) -> Unit): ActivityResultLauncher<Intent> =
            context.registerForActivityResultCompat(
                requestCode,
                ActivityResultContracts.StartActivityForResult()
            ) { result: ActivityResult ->
                onResult(result, onPathResult)
            }

        private fun getLauncher(context: Context, requestCode: AtomicInteger, cont: Continuation<List<String>>): ActivityResultLauncher<Intent> =
            context.registerForActivityResultCompat(
                requestCode,
                ActivityResultContracts.StartActivityForResult()
            ) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val r = result.data?.getStringArrayListExtra(LibPickYouTokens.IntentExtraPath)
                        ?.toList()?.takeIf { it.isNotEmpty() }
                    if (r != null) {
                        cont.resume(r)
                    } else {
                        cont.resumeWithException(CancellationException("Launcher returned empty list."))
                    }
                } else if (result.resultCode == Activity.RESULT_CANCELED) {
                    cont.resumeWithException(CancellationException("Launcher got cancelled."))
                }
            }

        var sTraverseBackend: ((path: Path) -> DirChildrenParcelable)? = null
        var sPermissionType: PermissionType = PermissionType.NORMAL
        var sDefaultPathList = LibPickYouTokens.DefaultPathList
        var sPickerType = PickerType.FILE
        var sLimitation = LibPickYouTokens.NoLimitation
        var sTitle = LibPickYouTokens.StringPlaceHolder
        var sPathPrefixHiddenNum = LibPickYouTokens.PathPrefixHiddenNum

        fun launch(context: Context, onPathResult: (path: List<String>) -> Unit) {
            traverseBackend = sTraverseBackend
            permissionType = sPermissionType
            pickerType = sPickerType
            limitation = sLimitation
            title = sTitle
            pathPrefixHiddenNum = sPathPrefixHiddenNum
            defaultPathList = sDefaultPathList
            getLauncher(context, AtomicInteger(), onPathResult).apply {
                launch(Intent(context, LibPickYouActivity::class.java))
            }
        }

        suspend fun awaitPickerOnce(context: Context): List<String> = suspendCoroutine { cont ->
            traverseBackend = sTraverseBackend
            permissionType = sPermissionType
            pickerType = sPickerType
            limitation = sLimitation
            title = sTitle
            pathPrefixHiddenNum = sPathPrefixHiddenNum
            defaultPathList = sDefaultPathList
            getLauncher(context, AtomicInteger(), cont).apply {
                launch(Intent(context, LibPickYouActivity::class.java))
            }
        }
    }

    /**
     * Set the default path.
     */
    fun setDefaultPath(path: String) { this.mDefaultPathList = path.split(LibPickYouTokens.PathSeparator) }

    /**
     * Set the type of PickYou.
     *
     * @param type [PickerType.FILE] | [PickerType.DIRECTORY] | [PickerType.BOTH]
     */
    fun setType(type: PickerType) { this.mPickerType = type }

    /**
     * Set the limitation of PickYou.
     *
     * @param number 0: No limitation, others: The number of files/directories user can pick
     */
    fun setLimitation(number: Int) { this.mLimitation = number }

    /**
     * Set the title of PickYou.
     */
    fun setTitle(title: String) { this.mTitle = title }

    /**
     * Set prefix path hidden num.
     * This will hide path prefix on the top of the activity and the return value.
     */
    fun setPathPrefixHiddenNum(pathPrefixHiddenNum: Int) { this.mPathPrefixHiddenNum = pathPrefixHiddenNum }

    /**
     * Set the backend of traverse.
     */
    fun setTraverseBackend(backend: (path: Path) -> DirChildrenParcelable) { this.mTraverseBackend = backend }

    /**
     * Set the permission type.
     */
    fun setPermissionType(type: PermissionType) { this.mPermissionType = type }

    private fun launch(context: Context) {
        traverseBackend = mTraverseBackend
        permissionType = mPermissionType
        pickerType = mPickerType
        limitation = mLimitation
        title = mTitle
        pathPrefixHiddenNum = mPathPrefixHiddenNum
        defaultPathList = mDefaultPathList
        mLauncher.launch(Intent(context, LibPickYouActivity::class.java))
    }

    /**
     * Launch PickYou immediately, you can do anything in [onResult] with the picked path.
     */
    fun launch(context: Context, onResult: (path: List<String>) -> Unit) {
        mLauncher = getLauncher(context, mNextLocalRequestCode, onResult)
        launch(context)
    }

    /**
     * Launch PickYou immediately, you can do anything in [onResult] with the picked path.
     */
    fun launch(componentActivity: ComponentActivity, onResult: (path: List<String>) -> Unit) = launch(context = componentActivity, onResult)

    /**
     * Launch PickYou immediately, you can do anything in [onResult] with the picked path.
     */
    fun launch(fragment: Fragment, onResult: (path: List<String>) -> Unit) = launch(fragment.requireContext(), onResult)

    suspend fun awaitPickerOnce(context: Context): List<String> = suspendCoroutine { cont ->
        // There is no cancellation support since we cannot cancel a launched Intent
        mLauncher = getLauncher(context, mNextLocalRequestCode, cont)
        launch(context)
    }
}
