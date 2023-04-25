package com.xayah.libpickyou.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.xayah.libpickyou.ui.tokens.IconButtonTokens

@Composable
internal fun IconButton(icon: ImageVector, onClick: () -> Unit) {
    IconButton(modifier = Modifier.size(IconButtonTokens.StateLayerSize), onClick = onClick) {
        Icon(imageVector = icon, contentDescription = null)
    }
}

@Composable
internal fun ArrowBackIconButton(onClick: () -> Unit) {
    IconButton(icon = Icons.Rounded.ArrowBack, onClick = onClick)
}
