package com.xayah.libpickyou.ui.components

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.xayah.libpickyou.R
import com.xayah.libpickyou.ui.activity.LibPickYouViewModel

@ExperimentalMaterial3Api
@Composable
internal fun PickYouScaffold(
    viewModel: LibPickYouViewModel,
    onResult: () -> Unit,
    content: @Composable () -> Unit = {}
) {
    val uiState by viewModel.uiState
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val context = LocalContext.current
    val isDialogOpen = remember { mutableStateOf(false) }
    TextDialog(
        isOpen = isDialogOpen,
        title = stringResource(id = R.string.selected),
        text = uiState.selectedItemsInLine,
        onConfirmClick = onResult
    )
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            PickYouTopAppBar(
                scrollBehavior = scrollBehavior,
                title = uiState.title,
                subtitle = "${stringResource(R.string.selected)}: ${
                    if (viewModel.uiState.value.selection.isEmpty())
                        stringResource(R.string.none)
                    else
                        uiState.selectedItems
                }",
                path = uiState.path,
                pathPrefixHiddenNum = uiState.pathPrefixHiddenNum,
                onArrowBackPressed = onResult,
                onPathChanged = {
                    viewModel.jumpPath(it)
                }
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (uiState.selectedItems.isEmpty())
                        Toast.makeText(context, context.getString(R.string.selection_empty), Toast.LENGTH_SHORT)
                            .show()
                    else
                        isDialogOpen.value = true
                },
                icon = { Icon(imageVector = Icons.Rounded.Check, contentDescription = null) },
                text = { Text(text = stringResource(R.string.pick)) },
            )
        }
    ) { innerPadding ->
        Column {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(innerPadding.calculateTopPadding())
            )
            content()
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(innerPadding.calculateBottomPadding())
            )
        }
    }
}
