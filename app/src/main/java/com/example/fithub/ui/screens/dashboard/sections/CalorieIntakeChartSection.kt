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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.fithub.R
import com.example.fithub.ui.charts.BarEntry
import com.example.fithub.ui.charts.SimpleBarChart
import com.example.fithub.ui.components.RoundedCard
import com.example.fithub.ui.components.SectionHeader
import com.example.fithub.ui.screens.dashboard.DailyCalorieBar
import com.example.fithub.ui.theme.*
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun CalorieIntakeChartSection(
    weeklyCalories: List<DailyCalorieBar>,
    dailyTarget: Int,
    onViewMoreClick: () -> Unit
) {
    // Localised short-day formatter (e.g. "Mon" / "Ma" / "Mo" per system locale).
    val dayFormatter = DateTimeFormatter
        .ofLocalizedDate(FormatStyle.SHORT)
        .withLocale(Locale.getDefault())

    RoundedCard(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(
            title = stringResource(R.string.dashboard_calorie_intake_title),
            actionText = stringResource(R.string.common_view_more),
            onAction = onViewMoreClick
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            LegendDot(stringResource(R.string.dashboard_legend_achieved), FitHubPrimary)
            LegendDot(stringResource(R.string.dashboard_legend_goal), FitHubLightBlue)
            LegendDot(stringResource(R.string.dashboard_legend_exceeded), ErrorRed)
        }

        Spacer(Modifier.height(14.dp))

        val maxValue = maxOf(
            dailyTarget.toFloat(),
            weeklyCalories.maxOfOrNull { it.calories.toFloat() } ?: 0f
        ) * 1.15f

        val entries = weeklyCalories.map { bar ->
            BarEntry(
                label = bar.date.format(dayFormatter),
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
            stringResource(R.string.dashboard_daily_goal_format, dailyTarget),
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