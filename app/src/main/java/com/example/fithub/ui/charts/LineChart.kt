package com.example.fithub.ui.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.fithub.ui.charts.BarEntry
import com.example.fithub.ui.theme.FitHubPrimary
import com.example.fithub.ui.theme.TextSecondary

@Composable
fun SimpleLineChart(
    entries: List<BarEntry>,
    modifier: Modifier = Modifier,
    lineColor: Color = FitHubPrimary,
    fillColor: Color = FitHubPrimary.copy(alpha = 0.15f),
    goal: Float? = null,
    goalColor: Color = FitHubPrimary.copy(alpha = 0.35f),
    chartHeight: Dp = 160.dp,
    lineWidth: Dp = 3.dp,
    showDots: Boolean = true
) {
    if (entries.isEmpty()) return
    val maxValue = (entries.maxOfOrNull { it.value } ?: 1f) * 1.15f
    val minValue = 0f

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
        ) {
            val width = size.width
            val height = size.height
            val stepX = if (entries.size > 1) width / (entries.size - 1) else width

            fun yFor(value: Float): Float {
                val frac = (value - minValue) / (maxValue - minValue)
                return height - (frac * height)
            }

            // Goal line
            if (goal != null) {
                val gy = yFor(goal)
                drawLine(
                    color = goalColor,
                    start = Offset(0f, gy),
                    end = Offset(width, gy),
                    strokeWidth = 2f,
                    pathEffect = androidx.compose.ui.graphics.PathEffect
                        .dashPathEffect(floatArrayOf(12f, 12f))
                )
            }

            // Build the path
            val linePath = Path()
            val fillPath = Path()
            entries.forEachIndexed { idx, entry ->
                val x = idx * stepX
                val y = yFor(entry.value)
                if (idx == 0) {
                    linePath.moveTo(x, y)
                    fillPath.moveTo(x, height)
                    fillPath.lineTo(x, y)
                } else {
                    linePath.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }
            }
            fillPath.lineTo((entries.size - 1) * stepX, height)
            fillPath.close()

            // Fill under line
            drawPath(path = fillPath, brush = Brush.verticalGradient(
                listOf(fillColor, Color.Transparent)
            ))

            // Line
            drawPath(
                path = linePath,
                color = lineColor,
                style = Stroke(width = lineWidth.toPx(), cap = StrokeCap.Round)
            )

            // Dots
            if (showDots) {
                entries.forEachIndexed { idx, entry ->
                    val x = idx * stepX
                    val y = yFor(entry.value)
                    drawCircle(
                        color = lineColor,
                        radius = 5f,
                        center = Offset(x, y)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.5f,
                        center = Offset(x, y)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            entries.forEach { entry ->
                Text(
                    text = entry.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}