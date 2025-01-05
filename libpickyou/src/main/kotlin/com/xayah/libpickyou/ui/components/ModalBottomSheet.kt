package com.xayah.libpickyou.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity


@ExperimentalMaterial3Api
@Composable
fun ModalBottomSheet(
    onDismissRequest: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(),
    content: @Composable ColumnScope.() -> Unit,
) {
    with(LocalDensity.current) {
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = sheetState,
            contentWindowInsets = { remember { WindowInsets(0, 0, 0, 0) } },
            properties = ModalBottomSheetDefaults.properties(shouldDismissOnBackPress = false)
        ) {
            content()
            Spacer(modifier = Modifier.height(WindowInsets.safeDrawing.getBottom(this@with).toDp()))
        }
    }
}
