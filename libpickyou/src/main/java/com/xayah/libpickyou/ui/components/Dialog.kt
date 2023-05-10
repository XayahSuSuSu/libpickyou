package com.xayah.libpickyou.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.stringResource
import com.xayah.libpickyou.R

@Composable
internal fun TextDialog(
    isOpen: MutableState<Boolean>,
    title: String,
    text: String,
    onConfirmClick: () -> Unit,
) {
    if (isOpen.value) {
        AlertDialog(
            onDismissRequest = {
                isOpen.value = false
            },
            icon = {
                Icon(imageVector = Icons.Rounded.Info, contentDescription = null)
            },
            title = { Text(text = title) },
            text = { LabelSmallText(text = text) },
            confirmButton = {
                TextButton(text = stringResource(id = R.string.confirm), onClick = onConfirmClick)
            },
            dismissButton = {
                TextButton(
                    text = stringResource(id = R.string.cancel),
                    onClick = { isOpen.value = false })
            },
        )
    }
}
