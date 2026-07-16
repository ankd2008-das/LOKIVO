package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun LokivoCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    
    var cardModifier = modifier
        .fillMaxWidth()

    cardModifier = if (!isDark) {
        cardModifier.shadow(
            elevation = 16.dp,
            shape = RoundedCornerShape(24.dp),
            spotColor = Color(0x14000000),
            ambientColor = Color(0x0A000000)
        )
    } else {
        cardModifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline,
            shape = RoundedCornerShape(24.dp)
        )
    }

    cardModifier = cardModifier
        .clip(RoundedCornerShape(24.dp))
        .background(MaterialTheme.colorScheme.surface)

    if (onClick != null) {
        cardModifier = cardModifier.clickable(onClick = onClick)
    }

    Column(
        modifier = cardModifier.padding(contentPadding),
        content = content
    )
}
