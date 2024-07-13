package com.xayah.libpickyou.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.xayah.libpickyou.ui.model.ImageVectorToken
import com.xayah.libpickyou.ui.model.StringResourceToken
import com.xayah.libpickyou.ui.model.fromVector
import com.xayah.libpickyou.ui.model.value
import com.xayah.libpickyou.ui.tokens.IconButtonTokens

@Composable
internal fun Button(text: StringResourceToken, onClick: () -> Unit) {
    Button(onClick = onClick, content = { Text(text = text.value) })
}

@Composable
internal fun TextButton(text: StringResourceToken, onClick: () -> Unit) {
    TextButton(onClick = onClick, content = { Text(text = text.value) })
}

@Composable
internal fun IconButton(icon: ImageVectorToken, onClick: () -> Unit) {
    IconButton(modifier = Modifier.size(IconButtonTokens.StateLayerSize), onClick = onClick) {
        Icon(imageVector = icon.value, contentDescription = null)
    }
}

@Composable
internal fun ArrowBackIconButton(onClick: () -> Unit) {
    IconButton(
        icon = ImageVectorToken.fromVector(Icons.AutoMirrored.Rounded.ArrowBack),
        onClick = onClick
    )
}

@Composable
internal fun AddIconButton(onClick: () -> Unit) {
    IconButton(icon = ImageVectorToken.fromVector(Icons.Rounded.Add), onClick = onClick)
}
