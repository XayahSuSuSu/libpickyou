package com.xayah.libpickyou.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.xayah.libpickyou.ui.tokens.ChipTokens
import com.xayah.libpickyou.ui.tokens.IconButtonTokens
import com.xayah.libpickyou.ui.tokens.TopAppBarTokens
import com.xayah.libpickyou.util.subPath
import com.xayah.libpickyou.util.toPath

internal fun ColorScheme.applyTonalElevation(backgroundColor: Color, elevation: Dp): Color {
    return if (backgroundColor == surface) {
        surfaceColorAtElevation(elevation)
    } else {
        backgroundColor
    }
}

@Composable
internal fun containerColor(
    containerColor: Color, scrolledContainerColor: Color, colorTransitionFraction: Float
): Color {
    return lerp(
        containerColor,
        scrolledContainerColor,
        FastOutLinearInEasing.transform(colorTransitionFraction)
    )
}

@ExperimentalMaterial3Api
@Composable
internal fun PathChipGroup(
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    path: List<String>,
    pathPrefixHiddenNum: Int,
    onPathChanged: (newPath: List<String>) -> Unit,
) {
    // Ensure the last item is shown once recomposing
    LaunchedEffect(path) {
        lazyListState.animateScrollToItem(path.size - 1)
    }

    if (path.size <= pathPrefixHiddenNum) throw IllegalArgumentException("The path prefix hidden num is greater than total path length.")

    LazyRow(modifier = modifier, state = lazyListState) {
        item {
            Spacer(modifier = Modifier.width(TopAppBarTokens.Padding + IconButtonTokens.StateLayerSize + TopAppBarTokens.Padding - ChipTokens.noAvatarStartPadding - ChipTokens.HorizontalElementsPadding))
        }

        items(
            count = path.size - pathPrefixHiddenNum,
            key = {
                path.toPath(it)
            }
        ) {
            val pathIndex = it + pathPrefixHiddenNum

            val onPathChipClick = {
                onPathChanged(path.subPath(pathIndex))
            }

            if (it == 0 && path[pathIndex].isEmpty()) {
                InputChip(
                    selected = false,
                    border = null,
                    shape = CircleShape,
                    label = { Text(TopAppBarTokens.RootElementSymbol) },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = null,
                            modifier = Modifier.size(InputChipDefaults.AvatarSize)
                        )
                    },
                    onClick = onPathChipClick
                )
            } else if (it == path.size - 1) {
                // No trailing icon
                InputChip(
                    selected = false,
                    border = null,
                    shape = CircleShape,
                    label = { Text(path[pathIndex]) },
                    onClick = onPathChipClick
                )
            } else {
                InputChip(
                    selected = false,
                    border = null,
                    shape = CircleShape,
                    label = { Text(path[pathIndex]) },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = null,
                            modifier = Modifier.size(InputChipDefaults.AvatarSize)
                        )
                    },
                    onClick = onPathChipClick
                )
            }
        }

        item {
            Spacer(modifier = Modifier.width(TopAppBarTokens.Padding))
        }
    }
}

@ExperimentalMaterial3Api
@Composable
internal fun PickYouTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    title: String,
    subtitle: String,
    path: List<String>,
    pathPrefixHiddenNum: Int,
    onArrowBackPressed: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    onPathChanged: (newPath: List<String>) -> Unit,
) {
    // Sets the app bar's height offset to collapse the entire bar's height when content is
    // scrolled.
    val heightOffsetLimit = with(LocalDensity.current) { -TopAppBarTokens.ContainerHeight.toPx() }
    SideEffect {
        if (scrollBehavior.state.heightOffsetLimit != heightOffsetLimit) {
            scrollBehavior.state.heightOffsetLimit = heightOffsetLimit
        }
    }

    val containerColor = MaterialTheme.colorScheme.surface
    val scrolledContainerColor = MaterialTheme.colorScheme.applyTonalElevation(
        backgroundColor = containerColor,
        elevation = TopAppBarTokens.OnScrollContainerElevation
    )

    // Obtain the container color from the TopAppBarColors using the `overlapFraction`. This
    // ensures that the colors will adjust whether the app bar behavior is pinned or scrolled.
    // This may potentially animate or interpolate a transition between the container-color and the
    // container's scrolled-color according to the app bar's scroll state.
    val colorTransitionFraction = scrollBehavior.state.overlappedFraction
    val fraction = if (colorTransitionFraction > 0.01f) 1f else 0f
    val appBarContainerColor by animateColorAsState(
        targetValue = containerColor(containerColor, scrolledContainerColor, fraction),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "ColorAnimation"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        color = appBarContainerColor
    ) {
        Column(
            modifier = Modifier
                .windowInsetsPadding(TopAppBarDefaults.windowInsets)
                // clip after padding so we don't show the title over the inset area
                .clipToBounds()
        ) {
            Row(
                modifier = Modifier
                    .paddingTop(TopAppBarTokens.Padding)
                    .paddingHorizontal(TopAppBarTokens.Padding),
                horizontalArrangement = Arrangement.spacedBy(TopAppBarTokens.Padding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ArrowBackIconButton(onArrowBackPressed)
                Column(modifier = Modifier.weight(1f)) {
                    TopBarTitle(text = title)
                    TopBarSubTitle(
                        modifier = Modifier.horizontalScroll(rememberScrollState(0)),
                        text = subtitle
                    )
                }
                actions()
            }

            PathChipGroup(
                path = path,
                pathPrefixHiddenNum = pathPrefixHiddenNum,
                onPathChanged = onPathChanged
            )
        }
    }
}
