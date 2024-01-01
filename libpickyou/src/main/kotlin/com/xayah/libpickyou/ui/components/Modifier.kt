package com.xayah.libpickyou.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal fun Modifier.paddingStart(start: Dp) =
    this.padding(start, 0.dp, 0.dp, 0.dp)

internal fun Modifier.paddingTop(top: Dp) =
    this.padding(0.dp, top, 0.dp, 0.dp)

internal fun Modifier.paddingEnd(end: Dp) =
    this.padding(0.dp, 0.dp, end, 0.dp)

internal fun Modifier.paddingBottom(bottom: Dp) =
    this.padding(0.dp, 0.dp, 0.dp, bottom)

internal fun Modifier.paddingHorizontal(horizontal: Dp) =
    this.padding(horizontal, 0.dp)

internal fun Modifier.paddingVertical(vertical: Dp) =
    this.padding(0.dp, vertical)
