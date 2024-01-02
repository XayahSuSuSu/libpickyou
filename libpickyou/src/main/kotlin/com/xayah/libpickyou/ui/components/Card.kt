package com.xayah.libpickyou.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.xayah.libpickyou.ui.model.StringResourceToken
import com.xayah.libpickyou.ui.model.value
import com.xayah.libpickyou.ui.tokens.SizeTokens

@ExperimentalMaterial3Api
@Composable
internal fun PermissionCard(modifier: Modifier = Modifier, content: StringResourceToken) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Column(modifier = Modifier.padding(SizeTokens.Level3)) {
            Icon(
                imageVector = Icons.Rounded.Star,
                contentDescription = null,
                modifier = Modifier
                    .size(SizeTokens.Level5)
                    .paddingBottom(SizeTokens.Level1)
            )
            BodyMediumText(
                modifier = Modifier.paddingTop(SizeTokens.Level1),
                text = content.value,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
