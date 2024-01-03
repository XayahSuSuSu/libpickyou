package com.xayah.libpickyou.ui.components

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.xayah.libpickyou.R
import com.xayah.libpickyou.parcelables.DirChildrenParcelable
import com.xayah.libpickyou.ui.PickYouLauncher
import com.xayah.libpickyou.ui.activity.IndexUiEffect
import com.xayah.libpickyou.ui.activity.IndexUiIntent
import com.xayah.libpickyou.ui.activity.LibPickYouViewModel
import com.xayah.libpickyou.ui.animation.CrossFade
import com.xayah.libpickyou.ui.model.ImageVectorToken
import com.xayah.libpickyou.ui.model.PickerType
import com.xayah.libpickyou.ui.model.fromDrawable
import com.xayah.libpickyou.ui.model.isRoot
import com.xayah.libpickyou.ui.model.value
import com.xayah.libpickyou.ui.tokens.LibPickYouTokens
import com.xayah.libpickyou.ui.tokens.SizeTokens
import com.xayah.libpickyou.util.DateUtil
import com.xayah.libpickyou.util.PathUtil
import com.xayah.libpickyou.util.PreferencesUtil
import com.xayah.libpickyou.util.toPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.file.Paths
import java.util.concurrent.CountDownLatch

internal fun onCheckBoxClick(
    viewModel: LibPickYouViewModel,
    context: Context,
    name: String,
    isChecked: MutableState<Boolean>?,
    checked: Boolean
) {
    val uiState = viewModel.uiState.value
    val limitation = uiState.limitation
    if (checked) {
        if (limitation == LibPickYouTokens.NoLimitation || viewModel.uiState.value.selection.size < limitation) {
            viewModel.emitIntent(IndexUiIntent.JoinSelection(name))
            isChecked?.value = true
        } else {
            viewModel.emitEffect(IndexUiEffect.DismissSnackbar)
            viewModel.emitEffect(IndexUiEffect.ShowSnackbar("${context.getString(R.string.max_limitation)}: $limitation"))
        }
    } else {
        viewModel.emitIntent(IndexUiIntent.RemoveSelection(name))
        isChecked?.value = false
    }
}

@ExperimentalAnimationApi
@Composable
internal fun ContentList(viewModel: LibPickYouViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    BackHandler(uiState.canUp) {
        viewModel.emitIntent(IndexUiIntent.Exit)
    }

    val progressVisible = remember { mutableStateOf(true) }
    val contentVisible = remember { mutableStateOf(false) }
    val progressLatch = remember { mutableStateOf(CountDownLatch(1)) }
    val contentLatch = remember { mutableStateOf(CountDownLatch(1)) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(uiState.path) {
        // Loading animation
        progressLatch.value = CountDownLatch(1)
        contentLatch.value = CountDownLatch(1)
        progressVisible.value = true
        contentVisible.value = false

        scope.launch {
            withContext(Dispatchers.IO) {
                // Wait for content animation
                contentLatch.value.await()

                runCatching {
                    val children: DirChildrenParcelable
                    val path = Paths.get(uiState.path.toPath())
                    children = PickYouLauncher.traverseBackend?.invoke(path)
                        ?: if (PickYouLauncher.permissionType.isRoot() && PreferencesUtil.readRequestedRoot()) {
                            viewModel.remoteRootService.traverse(path)
                        } else {
                            PathUtil.traverse(path)
                        }
                    viewModel.emitStateSuspend(uiState.copy(exceptionMessage = null))
                    viewModel.emitIntentSuspend(IndexUiIntent.UpdateChildren(children))
                }.onFailure {
                    viewModel.emitStateSuspend(uiState.copy(exceptionMessage = it.localizedMessage))
                }
                progressVisible.value = false
            }

            contentLatch.value = CountDownLatch(1)
            scope.launch {
                withContext(Dispatchers.IO) {
                    contentLatch.value.await()
                    contentVisible.value = true
                }
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        CrossFade(
            targetState = progressVisible,
            latch = progressLatch,
            modifier = Modifier.align(Alignment.Center)
        ) {
            if (it)
                CircularProgressIndicator()
        }

        CrossFade(targetState = contentVisible, latch = contentLatch) { target ->
            if (target) {
                if (uiState.exceptionMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .paddingBottom(SizeTokens.Level8),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(SizeTokens.Level3)
                        ) {
                            Icon(
                                imageVector = ImageVectorToken.fromDrawable(R.drawable.ic_rounded_error).value,
                                contentDescription = null,
                                modifier = Modifier.size(SizeTokens.Level8)
                            )
                            LabelSmallText(text = uiState.exceptionMessage!!)
                        }
                    }
                } else {
                    LazyColumn {
                        if (uiState.canUp)
                            item {
                                ChildReturnListItem {
                                    viewModel.emitIntent(IndexUiIntent.Exit)
                                }
                            }
                        items(items = uiState.children.directories, key = { it.name }) {
                            val isChecked = when (uiState.type) {
                                PickerType.DIRECTORY, PickerType.BOTH -> {
                                    remember { mutableStateOf(viewModel.isItemSelected(it.name)) }
                                }

                                else -> null
                            }
                            ChildDirListItem(
                                title = it.name,
                                subtitle = DateUtil.timestampToDateString(it.creationTime),
                                link = it.link,
                                isChecked = isChecked,
                                onCheckBoxClick = { checked ->
                                    onCheckBoxClick(
                                        viewModel = viewModel,
                                        context = context,
                                        name = it.name,
                                        isChecked = isChecked,
                                        checked = checked
                                    )
                                },
                                onItemClick = {
                                    if (it.link.isNullOrEmpty().not())
                                        viewModel.emitIntent(IndexUiIntent.JumpTo(it.link!!))
                                    else
                                        viewModel.emitIntent(IndexUiIntent.Enter(it.name))
                                }
                            )
                        }
                        items(items = uiState.children.files, key = { it.name }) {
                            val isChecked = when (uiState.type) {
                                PickerType.FILE, PickerType.BOTH -> {
                                    remember { mutableStateOf(viewModel.isItemSelected(it.name)) }
                                }

                                else -> null
                            }
                            ChildFileListItem(
                                title = it.name,
                                subtitle = DateUtil.timestampToDateString(it.creationTime),
                                link = it.link,
                                isChecked = isChecked,
                                onCheckBoxClick = { checked ->
                                    onCheckBoxClick(
                                        viewModel = viewModel,
                                        context = context,
                                        name = it.name,
                                        isChecked = isChecked,
                                        checked = checked
                                    )
                                },
                                onItemClick = {}
                            )
                        }
                    }
                }
            }
        }
    }
}
