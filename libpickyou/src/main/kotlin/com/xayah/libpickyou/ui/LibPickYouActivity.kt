package com.xayah.libpickyou.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.core.view.WindowCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.xayah.libpickyou.R
import com.xayah.libpickyou.ui.components.Button
import com.xayah.libpickyou.ui.components.LabelSmallText
import com.xayah.libpickyou.ui.components.LocalSlotScope
import com.xayah.libpickyou.ui.components.ModalBottomSheet
import com.xayah.libpickyou.ui.components.OnResume
import com.xayah.libpickyou.ui.components.PermissionCard
import com.xayah.libpickyou.ui.components.PickYouScaffold
import com.xayah.libpickyou.ui.components.TextButton
import com.xayah.libpickyou.ui.components.TitleLargeText
import com.xayah.libpickyou.ui.components.paddingBottom
import com.xayah.libpickyou.ui.components.paddingHorizontal
import com.xayah.libpickyou.ui.components.paddingTop
import com.xayah.libpickyou.ui.components.paddingVertical
import com.xayah.libpickyou.ui.model.PickerType
import com.xayah.libpickyou.ui.model.isRoot
import com.xayah.libpickyou.ui.model.isStorage
import com.xayah.libpickyou.ui.theme.LibPickYouTheme
import com.xayah.libpickyou.ui.tokens.AnimationToken
import com.xayah.libpickyou.ui.tokens.SizeTokens
import com.xayah.libpickyou.util.PermissionUtil
import com.xayah.libpickyou.util.PreferencesUtil
import com.xayah.libpickyou.util.RemoteRootService
import kotlinx.coroutines.launch
import com.xayah.libpickyou.PickYouLauncher.Companion.sPickYouLauncher as Launcher

@ExperimentalPermissionsApi
internal class LibPickYouActivity : ComponentActivity() {
    private val viewModel: LibPickYouViewModel by viewModels()

    @ExperimentalPermissionsApi
    @ExperimentalAnimationApi
    @ExperimentalMaterial3Api
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            LibPickYouTheme {
                val dialogState = LocalSlotScope.current!!.dialogSlot
                val uiState by viewModel.uiState.collectAsState()
                val permissionsState = PermissionUtil.getPermissionsState()
                val owner = LocalLifecycleOwner.current
                val scope = rememberCoroutineScope()
                var showBottomSheet by remember { mutableStateOf(false) }
                val sheetState = rememberModalBottomSheetState(confirmValueChange = { uiState.showBottomSheet.not() })
                OnResume(owner = owner) {
                    viewModel.emitIntentOnIO(IndexUiIntent.OnPermissionsChanged(this, permissionsState))
                }
                LaunchedEffect(null) {
                    viewModel.launchOnIO {
                        if (PreferencesUtil.readRequestedRoot()) RemoteRootService.initService()
                        viewModel.emitIntent(IndexUiIntent.InitRootService(this@LibPickYouActivity))
                        viewModel.emitIntent(IndexUiIntent.UpdatePathList(this@LibPickYouActivity))
                    }
                }
                LaunchedEffect(uiState.showBottomSheet) {
                    if (uiState.showBottomSheet) {
                        showBottomSheet = true
                    } else {
                        sheetState.hide()
                        showBottomSheet = false
                    }
                }
                if (showBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = {},
                        sheetState = sheetState
                    ) {
                        TitleLargeText(modifier = Modifier.paddingHorizontal(SizeTokens.Level24), text = stringResource(id = R.string.necessary_permissions))
                        if (Launcher.permissionType.isRoot()) {
                            PermissionCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .paddingHorizontal(SizeTokens.Level24)
                                    .paddingTop(SizeTokens.Level24),
                                content = "Root",
                            )
                        }

                        if (Launcher.permissionType.isStorage()) {
                            PermissionCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .paddingHorizontal(SizeTokens.Level24)
                                    .paddingTop(SizeTokens.Level24),
                                content = stringResource(id = R.string.storage),
                            )
                        }

                        AnimatedVisibility(visible = uiState.grantTimes != 0) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .paddingHorizontal(SizeTokens.Level24)
                                    .paddingTop(SizeTokens.Level8),
                                horizontalArrangement = Arrangement.End
                            ) {
                                LabelSmallText(
                                    text = stringResource(id = R.string.failed_to_grant_permission),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(SizeTokens.Level24))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .paddingHorizontal(SizeTokens.Level24)
                                .paddingBottom(SizeTokens.Level16),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(text = stringResource(id = R.string.cancel), onClick = {
                                finish()
                            })
                            Spacer(modifier = Modifier.width(SizeTokens.Level8))
                            Button(text = stringResource(id = R.string.confirm), onClick = {
                                scope.launch {
                                    viewModel.emitIntent(IndexUiIntent.GrantPermissions(this@LibPickYouActivity, permissionsState))
                                }
                            })
                        }
                    }
                }
                PickYouScaffold(
                    title = uiState.title,
                    pathList = uiState.pathList,
                    pathPrefixHiddenNum = uiState.pathPrefixHiddenNum,
                    isLoading = uiState.isLoading,
                    onResult = { finish() },
                    onAdding = { viewModel.emitIntentOnIO(IndexUiIntent.OnCreatingDir(this, dialogState)) },
                    onPathChanged = {
                        viewModel.emitIntentOnIO(IndexUiIntent.JumpToList(this, it))
                    },
                    onFabClick = if (Launcher.pickerType == PickerType.DIRECTORY) {
                        {
                            scope.launch {
                                val (dismissState, _) = dialogState.openConfirm(
                                    title = getString(R.string.pick),
                                    icon = ImageVector.vectorResource(theme = theme, res = resources, resId = R.drawable.ic_rounded_folder_open),
                                    text = uiState.pathString,
                                )
                                if (dismissState.isConfirm) {
                                    viewModel.emitIntent(IndexUiIntent.OnResult(this@LibPickYouActivity, null))
                                }
                            }
                        }
                    } else {
                        null
                    },
                    content = { innerPadding ->
                        val context = LocalContext.current
                        val exceptionMessageState by viewModel.exceptionMessageState.collectAsState()

                        BackHandler(uiState.canUp) {
                            viewModel.emitIntentOnIO(IndexUiIntent.Exit(context = context))
                        }

                        Box(modifier = Modifier.fillMaxSize()) {
                            AnimatedVisibility(
                                uiState.isLoading.not(), label = AnimationToken.ANIMATED_VISIBILITY_LABEL,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                if (exceptionMessageState != null) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .paddingBottom(SizeTokens.Level64),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .wrapContentHeight()
                                                .paddingHorizontal(SizeTokens.Level16),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(SizeTokens.Level16)
                                        ) {
                                            Icon(
                                                imageVector = ImageVector.vectorResource(id = R.drawable.ic_rounded_error),
                                                contentDescription = null,
                                                modifier = Modifier.size(SizeTokens.Level64)
                                            )
                                            LabelSmallText(text = exceptionMessageState!!, textAlign = TextAlign.Center)
                                        }
                                    }
                                } else {
                                    AnimatedContent(targetState = uiState, label = AnimationToken.ANIMATED_CONTENT_LABEL) {
                                        LazyVerticalStaggeredGrid(
                                            modifier = Modifier.paddingHorizontal(SizeTokens.Level24),
                                            columns = StaggeredGridCells.Fixed(2),
                                            horizontalArrangement = Arrangement.spacedBy(SizeTokens.Level8),
                                        ) {
                                            if (it.canUp)
                                                item {
                                                    AssistChip(
                                                        onClick = {
                                                            viewModel.emitIntentOnIO(IndexUiIntent.Exit(context = context))
                                                        },
                                                        label = {
                                                            Text("..")
                                                        },
                                                        leadingIcon = {
                                                            Icon(
                                                                ImageVector.vectorResource(id = R.drawable.ic_rounded_undo),
                                                                contentDescription = null,
                                                                Modifier.size(AssistChipDefaults.IconSize)
                                                            )
                                                        },
                                                        border = null
                                                    )
                                                }
                                            items(items = it.children.directories) {
                                                AssistChip(
                                                    onClick = {
                                                        viewModel.emitIntentOnIO(IndexUiIntent.Enter(context, it))
                                                    },
                                                    label = {
                                                        Text(modifier = Modifier.paddingVertical(SizeTokens.Level8), text = it.name)
                                                    },
                                                    leadingIcon = {
                                                        Icon(
                                                            ImageVector.vectorResource(id = R.drawable.ic_rounded_folder_open),
                                                            contentDescription = null,
                                                            Modifier.size(AssistChipDefaults.IconSize)
                                                        )
                                                    },
                                                    border = null
                                                )
                                            }
                                            items(items = it.children.files) {
                                                AssistChip(
                                                    onClick = {
                                                        scope.launch {
                                                            if (Launcher.pickerType == PickerType.FILE) {
                                                                val (dismissState, _) = dialogState.openConfirm(
                                                                    title = getString(R.string.pick),
                                                                    icon = ImageVector.vectorResource(theme = context.theme, res = context.resources, resId = R.drawable.ic_rounded_document),
                                                                    text = it.name,
                                                                )
                                                                if (dismissState.isConfirm) {
                                                                    viewModel.emitIntent(IndexUiIntent.OnResult(this@LibPickYouActivity, it.name))
                                                                }
                                                            }
                                                        }
                                                    },
                                                    label = {
                                                        Text(modifier = Modifier.paddingVertical(SizeTokens.Level8), text = it.name)
                                                    },
                                                    leadingIcon = {
                                                        Icon(
                                                            ImageVector.vectorResource(id = R.drawable.ic_rounded_document),
                                                            contentDescription = null,
                                                            Modifier.size(AssistChipDefaults.IconSize)
                                                        )
                                                    },
                                                    border = null
                                                )
                                            }

                                            item {
                                                Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding()))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}
