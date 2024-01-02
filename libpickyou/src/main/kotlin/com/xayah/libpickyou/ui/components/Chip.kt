package com.xayah.libpickyou.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.xayah.libpickyou.ui.model.ImageVectorToken
import com.xayah.libpickyou.ui.model.StringResourceToken
import com.xayah.libpickyou.ui.model.value
import com.xayah.libpickyou.ui.tokens.SizeTokens

@ExperimentalMaterial3Api
@Composable
internal fun AssistChip(modifier: Modifier, icon: ImageVectorToken, text: StringResourceToken) {
    OutlinedCard(modifier = modifier, onClick = {}, content = {
        Row(
            modifier = Modifier.padding(SizeTokens.Level1),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SizeTokens.Level1)
        ) {
            Icon(
                imageVector = icon.value,
                contentDescription = null,
                modifier = Modifier.size(AssistChipDefaults.IconSize)
            )
            LabelSmallText(
                modifier = Modifier.weight(1f),
                text = text.value
            )
        }
    })
}