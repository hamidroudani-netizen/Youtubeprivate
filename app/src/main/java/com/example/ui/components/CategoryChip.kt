package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CategoryChip(
    category: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        label = "chip_bg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "chip_text"
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        shadowElevation = if (isSelected) 4.dp else 0.dp,
        modifier = modifier
            .testTag("category_chip_$category")
            .clickable(onClick = onSelect)
    ) {
        Text(
            text = category,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                fontSize = 15.sp
            ),
            color = textColor,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
        )
    }
}
