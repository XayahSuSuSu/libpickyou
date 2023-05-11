package com.xayah.libpickyou.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.LaunchedEffect
import androidx.core.view.WindowCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.xayah.libpickyou.R
import com.xayah.libpickyou.ui.components.ContentList
import com.xayah.libpickyou.ui.components.PickYouScaffold
import com.xayah.libpickyou.ui.theme.LibPickYouTheme
import com.xayah.libpickyou.ui.tokens.LibPickYouTokens
import com.xayah.libpickyou.util.PermissionUtil
import com.xayah.libpickyou.util.RemoteRootService

internal class LibPickYouActivity : ComponentActivity() {
    private val viewModel: LibPickYouViewModel by viewModels()

    @ExperimentalPermissionsApi
    @ExperimentalAnimationApi
    @ExperimentalMaterial3Api
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.exit()
            }
        })

        setContent {
            LibPickYouTheme {
                // Check or request storage permissions
                PermissionUtil.RequestStoragePermissions()
                LaunchedEffect(null) {
                    viewModel.setPickerType(PickerType.of(intent.getStringExtra(LibPickYouTokens.IntentExtraType)))
                    viewModel.setLimitation(
                        intent.getIntExtra(
                            LibPickYouTokens.IntentExtraLimitation,
                            LibPickYouTokens.NoLimitation
                        )
                    )
                    viewModel.setTitle(
                        intent.getStringExtra(LibPickYouTokens.IntentExtraTitle) ?: getString(R.string.lib_name)
                    )
                    viewModel.remoteRootService = RemoteRootService(this@LibPickYouActivity)
                }

                PickYouScaffold(
                    viewModel = viewModel,
                    onResult = {
                        val intent = Intent()
                        intent.putStringArrayListExtra(
                            LibPickYouTokens.IntentExtraPath,
                            ArrayList(viewModel.uiState.value.selection)
                        )
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    },
                    content = {
                        ContentList(viewModel = viewModel)
                    })
            }
        }
    }
}
