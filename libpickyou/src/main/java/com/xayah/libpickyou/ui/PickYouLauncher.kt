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
    private var type = PickerType.FILE

    private fun onResult(result: ActivityResult, onResult: (path: List<String>) -> Unit) {
        if (result.resultCode == Activity.RESULT_OK) {
            onResult(result.data?.getStringArrayListExtra(LibPickYouTokens.IntentExtraPath)?.toList() ?: listOf())
        }
    }

    /**
     * Set the type of PickYou.
     *
     * @param type [PickerType.FILE] | [PickerType.DIRECTORY] | [PickerType.BOTH]
     */
    fun setType(type: PickerType) {
        this.type = type
    }

    private fun launch(context: Context) {
        val intent = Intent(context, LibPickYouActivity::class.java)
        intent.putExtra(LibPickYouTokens.IntentExtraType, type.type)
        launcher.launch(intent)
    }

    /**
     * Launch PickYou immediately, you can do anything in [onResult] with the picked path.
     */
    fun launch(componentActivity: ComponentActivity, onResult: (path: List<String>) -> Unit) {
        launcher =
            componentActivity.registerForActivityResultCompat(
                mNextLocalRequestCode,
                ActivityResultContracts.StartActivityForResult()
            ) { result: ActivityResult ->
                onResult(result, onResult)
            }
        launch(componentActivity)
    }

    /**
     * Launch PickYou immediately, you can do anything in [onResult] with the picked path.
     */
    fun launch(fragment: Fragment, onResult: (path: List<String>) -> Unit) {
        launcher =
            fragment.registerForActivityResultCompat(
                mNextLocalRequestCode,
                ActivityResultContracts.StartActivityForResult()
            ) { result: ActivityResult ->
                onResult(result, onResult)
            }
        launch(fragment.requireContext())
    }
}
