package com.xayah.libpickyou.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.xayah.libpickyou.ui.activity.LibPickYouActivity
import com.xayah.libpickyou.ui.activity.PickerType
import com.xayah.libpickyou.ui.tokens.LibPickYouTokens
import com.xayah.libpickyou.util.registerForActivityResultCompat
import java.util.concurrent.atomic.AtomicInteger

class PickYouLauncher {
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private val mNextLocalRequestCode = AtomicInteger()
    private var defaultPathList = LibPickYouTokens.DefaultPathList
    private var type = PickerType.FILE
    private var limitation = LibPickYouTokens.NoLimitation
    private var title = LibPickYouTokens.StringPlaceHolder
    private var pathPrefixHiddenNum = LibPickYouTokens.PathPrefixHiddenNum

    private fun onResult(result: ActivityResult, onResult: (path: List<String>) -> Unit) {
        if (result.resultCode == Activity.RESULT_OK) {
            onResult(result.data?.getStringArrayListExtra(LibPickYouTokens.IntentExtraPath)?.toList() ?: listOf())
        }
    }

    /**
     * Set the default path
     */
    fun setDefaultPath(path: String) {
        this.defaultPathList = path.split(LibPickYouTokens.PathSeparator)
    }

    /**
     * Set the type of PickYou.
     *
     * @param type [PickerType.FILE] | [PickerType.DIRECTORY] | [PickerType.BOTH]
     */
    fun setType(type: PickerType) {
        this.type = type
    }

    /**
     * Set the limitation of PickYou.
     *
     * @param number 0: No limitation, others: The number of files/directories user can pick
     */
    fun setLimitation(number: Int) {
        this.limitation = number
    }

    /**
     * Set the title of PickYou.
     */
    fun setTitle(title: String) {
        this.title = title
    }

    /**
     * Set prefix path hidden num.
     * This will hide path prefix on the top of the activity and the return value.
     */
    fun setPathPrefixHiddenNum(pathPrefixHiddenNum: Int) {
        this.pathPrefixHiddenNum = pathPrefixHiddenNum
    }

    private fun launch(context: Context) {
        val intent = Intent(context, LibPickYouActivity::class.java)
        intent.putExtra(LibPickYouTokens.IntentExtraType, type.type)
        intent.putExtra(LibPickYouTokens.IntentExtraLimitation, limitation)
        intent.putExtra(LibPickYouTokens.IntentExtraTitle, title)
        intent.putExtra(LibPickYouTokens.IntentPathPrefixHiddenNum, pathPrefixHiddenNum)
        intent.putStringArrayListExtra(LibPickYouTokens.IntentExtraDefaultPathList, ArrayList(defaultPathList))
        launcher.launch(intent)
    }

    /**
     * Launch PickYou immediately, you can do anything in [onResult] with the picked path.
     */
    fun launch(context: Context, onResult: (path: List<String>) -> Unit) {
        launcher =
            context.registerForActivityResultCompat(
                mNextLocalRequestCode,
                ActivityResultContracts.StartActivityForResult()
            ) { result: ActivityResult ->
                onResult(result, onResult)
            }
        launch(context)
    }

    /**
     * Launch PickYou immediately, you can do anything in [onResult] with the picked path.
     */
    fun launch(componentActivity: ComponentActivity, onResult: (path: List<String>) -> Unit) =
        launch(context = componentActivity, onResult)

    /**
     * Launch PickYou immediately, you can do anything in [onResult] with the picked path.
     */
    fun launch(fragment: Fragment, onResult: (path: List<String>) -> Unit) =
        launch(fragment.requireContext(), onResult)
}
