package com.example.recipegenerator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

@Composable
fun MinimalListItem(
    onClick : ( () -> Unit )? = null,
    content : @Composable BoxScope.() -> Unit,
) {
    val selectableContainerModifier = Modifier
        .fillMaxWidth()
        .defaultMinSize(Dp.Unspecified, 30.dp)
        .dropShadow(
            shape = MaterialTheme.shapes.large,
            shadow = Shadow(
                spread = 2.dp,
                radius = 6.dp,
                offset = DpOffset(0.dp, 7.dp),
                alpha = 0.3f
            )
        )
        .clip(MaterialTheme.shapes.medium)
        .background(MaterialTheme.colorScheme.onPrimary)
        .clickable(
            enabled = onClick != null,
            onClick = onClick ?: {},
        )


    Box(modifier = selectableContainerModifier, content = content)
}