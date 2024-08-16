package com.xayah.libpickyou.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.google.accompanist.permissions.ExperimentalPermissionsApi

@ExperimentalPermissionsApi
@ExperimentalMaterial3Api
@Composable
internal fun PickYouScaffold(
    title: String,
    pathList: List<String>,
    pathPrefixHiddenNum: Int,
    isLoading: Boolean,
    onResult: () -> Unit,
    onAdding: () -> Unit,
    onPathChanged: (newPath: List<String>) -> Unit,
    onFabClick: (() -> Unit)? = null,
    content: @Composable (innerPadding: PaddingValues) -> Unit = {},
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Column {
                PickYouTopAppBar(
                    scrollBehavior = scrollBehavior,
                    title = title,
                    path = pathList,
                    pathPrefixHiddenNum = pathPrefixHiddenNum,
                    onArrowBackPressed = onResult,
                    actions = {
                        AddIconButton(onAdding)
                    },
                    onPathChanged = onPathChanged
                )
                Divider()
                AnimatedVisibility(visible = isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            AnimatedVisibility(visible = onFabClick != null, enter = scaleIn(), exit = scaleOut()) {
                FloatingActionButton(
                    onClick = {
                        onFabClick?.invoke()
                    },
                ) {
                    Icon(Icons.Rounded.Check, null)
                }
            }
        }
    ) { innerPadding ->
        Column {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(innerPadding.calculateTopPadding())
            )
            content(innerPadding)
        }
    }
}
