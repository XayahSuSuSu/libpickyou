package com.xayah.libpickyou.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

@Composable
internal fun TopBarTitle(text: String) {
    Text(
        text = text,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.titleLarge,
    )
}

@Composable
internal fun TopBarSubTitle(modifier: Modifier, text: String) {
    Text(
        modifier = modifier,
        text = text,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.bodySmall,
    )
}

@Composable
internal fun BodyLargeText(text: String, color: Color = Color.Unspecified) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = color
    )
}

@Composable
internal fun BodyMediumText(modifier: Modifier = Modifier, text: String, fontWeight: FontWeight? = null, color: Color = Color.Unspecified) {
    Text(
        modifier = modifier,
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = fontWeight,
        color = color,
    )
}

@Composable
internal fun LabelSmallText(modifier: Modifier = Modifier, text: String, textAlign: TextAlign? = null) {
    Text(
        modifier = modifier,
        text = text,
        style = MaterialTheme.typography.labelSmall,
        textAlign = textAlign,
    )
}
