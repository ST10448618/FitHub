package com.example.fithub.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.fithub.ui.theme.*

@Composable
fun RoundedProgressBar(
    progress: Float,                          // 0f..1f
    modifier: Modifier = Modifier,
    height: Dp = 10.dp,
    backgroundColor: Color = SurfaceGray.copy(alpha = 0.5f),
    fillColor: Color = FitHubPrimary,
    gradient: List<Color>? = null,
    animated: Boolean = true
) {
    val clamped = progress.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = clamped,
        animationSpec = tween(durationMillis = if (animated) 600 else 0),
        label = "progress"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(backgroundColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .clip(RoundedCornerShape(height / 2))
                .background(
                    brush = if (gradient != null) {
                        Brush.horizontalGradient(gradient)
                    } else {
                        Brush.horizontalGradient(listOf(fillColor, fillColor))
                    }
                )
        )
    }
}

@Composable
fun LabelledProgressBar(
    label: String,
    actual: Double,
    target: Int,
    modifier: Modifier = Modifier,
    fillColor: Color = FitHubPrimary,
    showPercent: Boolean = true,
    topRightLabel: String? = null
) {
    val progress = if (target > 0) (actual / target).toFloat().coerceIn(0f, 1f) else 0f
    val percent = (progress * 100).toInt()

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            val rightText = topRightLabel
                ?: if (showPercent) "${actual.toInt()} / $target" else ""
            if (rightText.isNotEmpty()) {
                Text(
                    text = rightText,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        RoundedProgressBar(
            progress = progress,
            height = 10.dp,
            fillColor = fillColor
        )
        if (showPercent) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$percent%",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}