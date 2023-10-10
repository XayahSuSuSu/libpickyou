package com.xayah.libpickyou.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.topjohnwu.superuser.Shell
import com.xayah.libpickyou.R
import com.xayah.libpickyou.parcelables.DirChildrenParcelable
import com.xayah.libpickyou.ui.activity.LibPickYouViewModel
import com.xayah.libpickyou.ui.activity.PickerType
import com.xayah.libpickyou.ui.animation.CrossFade
import com.xayah.libpickyou.ui.tokens.LibPickYouTokens
import com.xayah.libpickyou.util.DateUtil
import com.xayah.libpickyou.util.PathUtil
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
    val uiState by viewModel.uiState
    val limitation = uiState.limitation
    if (checked) {
        if (limitation == LibPickYouTokens.NoLimitation || viewModel.uiState.value.selection.size < limitation) {
            viewModel.addSelection(name)
            isChecked?.value = true
        } else {
            Toast.makeText(context, "${context.getString(R.string.max_limitation)}: $limitation", Toast.LENGTH_SHORT)
                .show()
        }
    } else {
        viewModel.removeSelection(name)
        isChecked?.value = false
    }
}

@ExperimentalAnimationApi
@Composable
internal fun ContentList(viewModel: LibPickYouViewModel) {
    val uiState by viewModel.uiState

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

                withContext(Dispatchers.Main) {
                    val children: DirChildrenParcelable
                    val path = Paths.get(uiState.path.toPath())
                    children = if (Shell.getShell().isRoot) {
                        viewModel.remoteRootService.traverse(path)
                    } else {
                        PathUtil.traverse(path)
                    }
                    viewModel.updateChildren(children)
                    progressVisible.value = false
                }
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
            if (target)
                LazyColumn {
                    if (uiState.isAtRoot.not())
                        item {
                            ChildReturnListItem {
                                viewModel.exit()
                            }
                        }
                    items(items = uiState.children.directories, key = { it.name }) {
                        val isChecked = when (viewModel.uiState.value.type) {
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
                                    viewModel.jumpPath(it.link!!)
                                else
                                    viewModel.enter(it.name)
                            }
                        )
                    }
                    items(items = uiState.children.files, key = { it.name }) {
                        val isChecked = when (viewModel.uiState.value.type) {
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
