package com.example.fithub.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fithub.ui.theme.*

/**
 * Row:  [ − ]  value  [ + ]
 * value is shown with an optional unit suffix (e.g. "kg", "kcal").
 */
@Composable
fun NumberStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    step: Int = 1,
    min: Int = 0,
    max: Int = Int.MAX_VALUE,
    unit: String = "",
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(CardWhite)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { if (enabled) onValueChange((value - step).coerceAtLeast(min)) },
            enabled = enabled && value > min,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(FitHubPrimary)
        ) {
            Icon(
                Icons.Filled.Remove,
                contentDescription = "Decrease",
                tint = TextOnPrimary
            )
        }

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (unit.isEmpty()) "$value" else "$value $unit",
                style = MaterialTheme.typography.headlineMedium,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        IconButton(
            onClick = { if (enabled) onValueChange((value + step).coerceAtMost(max)) },
            enabled = enabled && value < max,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(FitHubPrimary)
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "Increase",
                tint = TextOnPrimary
            )
        }
    }
}