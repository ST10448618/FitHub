package com.example.fithub.ui.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.fithub.ui.theme.FitHubMidBlue
import com.example.fithub.ui.theme.FitHubPrimary
import com.example.fithub.ui.theme.TextSecondary

data class BarEntry(
    val label: String,
    val value: Float,
    val overrideColor: Color? = null
)

@Composable
fun SimpleBarChart(
    entries: List<BarEntry>,
    modifier: Modifier = Modifier,
    maxValue: Float? = null,
    barColor: Color = FitHubPrimary,
    barGradient: List<Color> = listOf(FitHubMidBlue, FitHubPrimary),
    goal: Float? = null,
    goalColor: Color = FitHubPrimary.copy(alpha = 0.3f),
    barWidth: Dp = 26.dp,
    chartHeight: Dp = 140.dp
) {
    val effectiveMax =
        maxValue ?: (entries.maxOfOrNull { it.value } ?: 1f) * 1.15f

    Column(modifier = modifier) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
        ) {

            // Goal line
            if (goal != null && effectiveMax > 0f) {
                val goalY = chartHeight * (1f - (goal / effectiveMax))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .offset(y = goalY)
                        .background(goalColor)
                )
            }

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {

                entries.forEach { entry ->

                    val frac =
                        if (effectiveMax > 0f) {
                            entry.value / effectiveMax
                        } else {
                            0f
                        }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {

                        Spacer(
                            modifier = Modifier.weight(1f)
                        )

                        Box(
                            modifier = Modifier
                                .width(barWidth)
                                .fillMaxHeight(frac)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 8.dp,
                                        topEnd = 8.dp
                                    )
                                )
                                .background(
                                    brush = if (entry.overrideColor != null) {
                                        Brush.verticalGradient(
                                            listOf(
                                                entry.overrideColor,
                                                entry.overrideColor
                                            )
                                        )
                                    } else {
                                        Brush.verticalGradient(barGradient)
                                    }
                                )
                        )
                    }
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
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

