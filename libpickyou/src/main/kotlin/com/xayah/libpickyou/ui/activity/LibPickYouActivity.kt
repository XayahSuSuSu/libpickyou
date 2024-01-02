package com.xayah.libpickyou.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.topjohnwu.superuser.Shell
import com.xayah.libpickyou.R
import com.xayah.libpickyou.ui.PickYouLauncher
import com.xayah.libpickyou.ui.components.ContentList
import com.xayah.libpickyou.ui.components.PermissionScaffold
import com.xayah.libpickyou.ui.components.PickYouScaffold
import com.xayah.libpickyou.ui.components.SelectionScaffold
import com.xayah.libpickyou.ui.components.currentRoute
import com.xayah.libpickyou.ui.components.navigateAndPopBackStack
import com.xayah.libpickyou.ui.model.PickYouRoutes
import com.xayah.libpickyou.ui.model.isRoot
import com.xayah.libpickyou.ui.model.isStorage
import com.xayah.libpickyou.ui.theme.LibPickYouTheme
import com.xayah.libpickyou.ui.tokens.LibPickYouTokens
import com.xayah.libpickyou.util.PermissionUtil
import com.xayah.libpickyou.util.PermissionUtil.Companion.checkStoragePermissions
import com.xayah.libpickyou.util.PreferencesUtil
import com.xayah.libpickyou.util.RemoteRootService

internal class LibPickYouActivity : ComponentActivity() {
    private val viewModel: LibPickYouViewModel by viewModels()
    private var firstResume = true

    @ExperimentalPermissionsApi
    @ExperimentalAnimationApi
    @ExperimentalMaterial3Api
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            LibPickYouTheme {
                val uiState by viewModel.uiState.collectAsState()
                var permissions by remember { mutableStateOf(true) }
                val permissionsState = PermissionUtil.getPermissionsState()
                val owner = LocalLifecycleOwner.current
                val navController = rememberNavController()
                val currentRoute = navController.currentRoute()
                val onResult = {
                    val intent = Intent()
                    intent.putStringArrayListExtra(LibPickYouTokens.IntentExtraPath, ArrayList(viewModel.uiState.value.selection))
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
                val onPermissionsChanged: () -> Unit = {
                    permissions = checkStoragePermissions(permissionsState, PickYouLauncher.permissionType)
                    if (permissions.not()) {
                        if (currentRoute == PickYouRoutes.Permission.route) {
                            viewModel.emitEffect(IndexUiEffect.ShowSnackbar(getString(R.string.failed_to_grant_permission)))
                        } else {
                            navController.navigateAndPopBackStack(PickYouRoutes.Permission.route)
                        }
                    } else {
                        if (firstResume.not()) navController.navigateAndPopBackStack(PickYouRoutes.PickYou.route)
                    }
                }

                DisposableEffect(owner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            onPermissionsChanged()

                            if (firstResume) {
                                firstResume = false
                            }
                        }
                    }
                    owner.lifecycle.addObserver(observer)
                    onDispose {
                        owner.lifecycle.removeObserver(observer)
                    }
                }

                LaunchedEffect(null) {
                    viewModel.emitIntent(
                        IndexUiIntent.SetConfig(
                            path = PickYouLauncher.defaultPathList,
                            type = PickYouLauncher.pickerType,
                            limitation = PickYouLauncher.limitation,
                            title = PickYouLauncher.title,
                            pathPrefixHiddenNum = PickYouLauncher.pathPrefixHiddenNum,
                        )
                    )
                    viewModel.remoteRootService = RemoteRootService(this@LibPickYouActivity)
                }

                NavHost(
                    navController = navController,
                    startDestination = PickYouRoutes.PickYou.route,
                    enterTransition = { fadeIn() },
                    popEnterTransition = { fadeIn() },
                    exitTransition = { fadeOut() },
                    popExitTransition = { fadeOut() },
                ) {
                    composable(PickYouRoutes.Permission.route) {
                        PermissionScaffold(
                            permissionType = PickYouLauncher.permissionType,
                            snackbarHostState = viewModel.snackbarHostState,
                            onBack = onResult,
                            onConfirm = {
                                if (PickYouLauncher.permissionType.isRoot()) {
                                    runCatching {
                                        RemoteRootService.initService()
                                    }
                                    PreferencesUtil.saveRequestedRoot(Shell.getShell().isRoot)
                                    onPermissionsChanged()
                                }
                                if (PickYouLauncher.permissionType.isStorage()) {
                                    if (permissions.not()) PermissionUtil.requestStoragePermissions(context = this@LibPickYouActivity, permissionsState)
                                }
                            }
                        )
                    }
                    composable(PickYouRoutes.PickYou.route) {
                        PickYouScaffold(
                            navController = navController,
                            viewModel = viewModel,
                            onResult = onResult,
                            content = {
                                ContentList(viewModel = viewModel)
                            }
                        )
                    }
                    composable(PickYouRoutes.Selection.route) {
                        SelectionScaffold(
                            selection = uiState.selection,
                            snackbarHostState = viewModel.snackbarHostState,
                            onBack = { navController.popBackStack() },
                            onConfirm = onResult
                        )
                    }
                }
            }
        }
    }
}
