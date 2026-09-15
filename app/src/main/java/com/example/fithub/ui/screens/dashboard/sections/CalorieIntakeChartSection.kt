package com.example.fithub.ui.screens.dashboard.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fithub.ui.charts.BarEntry
import com.example.fithub.ui.charts.SimpleBarChart
import com.example.fithub.ui.components.RoundedCard
import com.example.fithub.ui.components.SectionHeader
import com.example.fithub.ui.screens.dashboard.DailyCalorieBar
import com.example.fithub.ui.theme.*

@Composable
fun CalorieIntakeChartSection(
    weeklyCalories: List<DailyCalorieBar>,
    dailyTarget: Int,
    onViewMoreClick: () -> Unit
) {
    RoundedCard(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(
            title = "Your Calorie Intake",
            actionText = "view more",
            onAction = onViewMoreClick
        )

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            LegendDot("Achieved Amount", FitHubPrimary)
            LegendDot("Goal Amount", FitHubLightBlue)
            LegendDot("Exceeded", ErrorRed)
        }

        Spacer(Modifier.height(14.dp))

        // Build bars — use max(daily target, highest bar) as scale
        val maxValue = maxOf(
            dailyTarget.toFloat(),
            weeklyCalories.maxOfOrNull { it.calories.toFloat() } ?: 0f
        ) * 1.15f

        val entries = weeklyCalories.map { bar ->
            BarEntry(
                label = bar.dayLabel,
                value = bar.calories.toFloat(),
                overrideColor = if (bar.isOverTarget) ErrorRed else null
            )
        }

        SimpleBarChart(
            entries = entries,
            chartHeight = 140.dp,
            maxValue = maxValue.coerceAtLeast(1f),
            goal = if (dailyTarget > 0) dailyTarget.toFloat() else null
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "Daily goal: $dailyTarget kcal",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun LegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(5.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}