package com.xayah.libpickyou.ui.components

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavHostController
import com.xayah.libpickyou.R
import com.xayah.libpickyou.ui.activity.LibPickYouViewModel
import com.xayah.libpickyou.ui.activity.PickYouRoutes
import com.xayah.libpickyou.ui.model.ImageVectorToken
import com.xayah.libpickyou.ui.model.StringResourceToken
import com.xayah.libpickyou.ui.model.fromString
import com.xayah.libpickyou.ui.model.fromStringId
import com.xayah.libpickyou.ui.model.fromVector
import com.xayah.libpickyou.ui.tokens.SizeTokens

@ExperimentalMaterial3Api
@Composable
internal fun PickYouScaffold(
    navController: NavHostController,
    viewModel: LibPickYouViewModel,
    onResult: () -> Unit,
    content: @Composable () -> Unit = {},
) {
    val uiState by viewModel.uiState
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val context = LocalContext.current
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
                    else navController.navigate(PickYouRoutes.Selection.route)
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

@ExperimentalMaterial3Api
@Composable
internal fun SelectionScaffold(selection: List<String>, onBack: () -> Unit, onConfirm: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.confirm_your_selections),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    ArrowBackIconButton(onBack)
                },
                scrollBehavior = scrollBehavior
            )
        },
    ) { innerPadding ->
        Column {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(innerPadding.calculateTopPadding())
            )

            LazyColumn(modifier = Modifier.weight(1f)) {
                item {
                    Spacer(modifier = Modifier.size(SizeTokens.Level0))
                }

                items(count = selection.size) {
                    AssistChip(
                        modifier = Modifier
                            .fillMaxWidth()
                            .paddingHorizontal(SizeTokens.Level3),
                        text = StringResourceToken.fromString("${it + 1}. ${selection[it]}"),
                        icon = ImageVectorToken.fromVector(Icons.Filled.Done),
                    )
                }

                item {
                    Spacer(modifier = Modifier.size(SizeTokens.Level0))
                }
            }

            Divider(color = MaterialTheme.colorScheme.primary)
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(SizeTokens.Level3)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .paddingHorizontal(SizeTokens.Level3),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SizeTokens.Level3, Alignment.End)
            ) {
                TextButton(text = StringResourceToken.fromStringId(R.string.cancel), onClick = onBack)
                Button(text = StringResourceToken.fromStringId(R.string.confirm), onClick = onConfirm)
            }
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(innerPadding.calculateBottomPadding())
            )
        }
    }
}
