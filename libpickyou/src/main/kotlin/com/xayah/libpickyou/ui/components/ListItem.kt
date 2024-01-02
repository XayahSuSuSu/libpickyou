package com.xayah.libpickyou.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.xayah.libpickyou.R
import com.xayah.libpickyou.ui.model.ImageVectorToken
import com.xayah.libpickyou.ui.model.fromDrawable
import com.xayah.libpickyou.ui.model.value
import com.xayah.libpickyou.ui.tokens.ChildListItemTokens

@Composable
internal fun ChildListItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    link: String? = null,
    isChecked: MutableState<Boolean>? = null,
    onCheckBoxClick: ((Boolean) -> Unit)? = null,
    onItemClick: () -> Unit
) {
    Surface(modifier = modifier.clickable(onClick = onItemClick)) {
        Row(
            modifier = Modifier
                .heightIn(min = ChildListItemTokens.ContainerHeight)
                .paddingHorizontal(ChildListItemTokens.ContainerHorizontalPadding)
                .paddingVertical(ChildListItemTokens.ContainerVerticalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ChildListItemTokens.ContainerHorizontalPadding)
        ) {
            Box(modifier = Modifier.size(ChildListItemTokens.IconHolderSize)) {
                Icon(
                    modifier = Modifier
                        .size(ChildListItemTokens.IconSize)
                        .align(Alignment.Center),
                    imageVector = icon,
                    contentDescription = null,
                )
                if (link != null)
                    Icon(
                        modifier = Modifier
                            .size(ChildListItemTokens.IndicatorSize)
                            .align(Alignment.BottomEnd),
                        imageVector = ImageVector.vectorResource(R.drawable.ic_rounded_link),
                        contentDescription = null,
                    )
            }
            Column(modifier = Modifier.weight(1f)) {
                BodyLargeText(title)
                if (subtitle != null)
                    BodyMediumText(text = subtitle)
            }
            if (isChecked != null && onCheckBoxClick != null) {
                IconToggleButton(
                    checked = isChecked.value,
                    onCheckedChange = { onCheckBoxClick(isChecked.value.not()) }
                ) {
                    if (isChecked.value) {
                        Icon(ImageVectorToken.fromDrawable(R.drawable.ic_rounded_check_circle).value, contentDescription = null)
                    } else {
                        Icon(
                            ImageVectorToken.fromDrawable(R.drawable.ic_rounded_unchecked_circle).value,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun ChildFileListItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    link: String? = null,
    isChecked: MutableState<Boolean>?,
    onCheckBoxClick: (Boolean) -> Unit,
    onItemClick: () -> Unit
) {
    ChildListItem(
        modifier = modifier,
        icon = ImageVector.vectorResource(id = R.drawable.ic_rounded_document),
        title = title,
        subtitle = subtitle,
        link = link,
        isChecked = isChecked,
        onCheckBoxClick = onCheckBoxClick,
        onItemClick = onItemClick
    )
}

@Composable
internal fun ChildDirListItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    link: String? = null,
    isChecked: MutableState<Boolean>?,
    onCheckBoxClick: (Boolean) -> Unit,
    onItemClick: () -> Unit
) {
    ChildListItem(
        modifier = modifier,
        icon = ImageVector.vectorResource(id = R.drawable.ic_rounded_folder),
        title = title,
        subtitle = subtitle,
        link = link,
        isChecked = isChecked,
        onCheckBoxClick = onCheckBoxClick,
        onItemClick = onItemClick
    )
}

@Composable
internal fun ChildReturnListItem(modifier: Modifier = Modifier, onItemClick: () -> Unit) {
    ChildListItem(
        modifier = modifier,
        icon = ImageVector.vectorResource(id = R.drawable.ic_rounded_undo),
        title = "..",
        onItemClick = onItemClick
    )
}
