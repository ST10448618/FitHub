package com.example.fithub.ui.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.fithub.ui.theme.FitHubPrimary
import com.example.fithub.ui.theme.SuccessGreen
import com.example.fithub.ui.theme.TextOnPrimary
import com.example.fithub.ui.theme.TextSecondary

/**
 * Weight journey chart.
 *
 * @param values weights in kg, oldest → newest
 * @param startLabel the label under the first point (e.g. "Start")
 * @param currentLabel the label under the last point (e.g. "Current")
 * @param goalKg optional horizontal goal line
 * @param highlightLatest whether to show the "tooltip" pill on the newest point
 */
@Composable
fun WeightJourneyChart(
    values: List<Double>,
    startLabel: String = "START",
    currentLabel: String = "CURRENT",
    goalKg: Double? = null,
    highlightLatest: Boolean = true,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 180.dp,
    lineColor: Color = FitHubPrimary,
    fillColor: Color = FitHubPrimary.copy(alpha = 0.20f)
) {
    if (values.isEmpty()) return

    Column(modifier = modifier) {
        // Chart area + goal pill on top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val pad = 8f

                val minVal = values.minOrNull() ?: 0.0
                val maxVal = values.maxOrNull() ?: 1.0
                // Give the chart some breathing room
                val span = (maxVal - minVal).takeIf { it > 0.01 } ?: 1.0
                val yMin = minVal - span * 0.20
                val yMax = maxVal + span * 0.20

                // Also factor in the goal, so the goal line is visible
                val yFloor = minOf(yMin, goalKg ?: yMin)
                val yCeil = maxOf(yMax, goalKg ?: yMax)

                fun yFor(value: Double): Float {
                    val frac = ((value - yFloor) / (yCeil - yFloor)).toFloat()
                    return height - pad - frac * (height - pad * 2)
                }

                val stepX = if (values.size > 1) (width - pad * 2) / (values.size - 1) else 0f

                // Goal line
                if (goalKg != null) {
                    val gy = yFor(goalKg)
                    drawLine(
                        color = FitHubPrimary.copy(alpha = 0.35f),
                        start = Offset(0f, gy),
                        end = Offset(width, gy),
                        strokeWidth = 3f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 12f))
                    )
                }

                // Build fill + line paths
                val linePath = Path()
                val fillPath = Path()
                values.forEachIndexed { idx, v ->
                    val x = pad + idx * stepX
                    val y = yFor(v)
                    if (idx == 0) {
                        linePath.moveTo(x, y)
                        fillPath.moveTo(x, height)
                        fillPath.lineTo(x, y)
                    } else {
                        linePath.lineTo(x, y)
                        fillPath.lineTo(x, y)
                    }
                }
                fillPath.lineTo(pad + (values.size - 1) * stepX, height)
                fillPath.close()

                // Fill
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        listOf(fillColor, Color.Transparent)
                    )
                )

                // Line
                drawPath(
                    path = linePath,
                    color = lineColor,
                    style = Stroke(width = 4f, cap = StrokeCap.Round)
                )

                // Dots
                values.forEachIndexed { idx, v ->
                    val x = pad + idx * stepX
                    val y = yFor(v)
                    drawCircle(
                        color = lineColor,
                        radius = 6f,
                        center = Offset(x, y)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 3f,
                        center = Offset(x, y)
                    )
                }
            }

            // Floating pill above the latest point
            if (highlightLatest && values.isNotEmpty()) {
                val latest = values.last()
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset(x = (-4).dp, y = (-8).dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(FitHubPrimary)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${latest} KG",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextOnPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        // Bottom axis labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                startLabel,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Text(
                currentLabel,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}