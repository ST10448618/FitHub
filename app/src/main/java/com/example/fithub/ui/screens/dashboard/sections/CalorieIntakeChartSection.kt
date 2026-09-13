package com.example.fithub.ui.screens.dashboard.sections

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fithub.domain.calculator.NutritionProgressCalculator
import com.example.fithub.ui.charts.BarEntry
import com.example.fithub.ui.charts.SimpleBarChart
import com.example.fithub.ui.components.RoundedCard
import com.example.fithub.ui.components.SectionHeader
import com.example.fithub.ui.theme.FitHubPrimary

// ============================================================
// OWNER: Track A (Perez) — nutrition chart
// ============================================================
@Composable
fun CalorieIntakeChartSection(
    snapshot: NutritionProgressCalculator.DashboardNutrition?,
    onViewMoreClick: () -> Unit
) {
    RoundedCard(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(
            title = "Your Calorie Intake",
            actionText = "view more",
            onAction = onViewMoreClick
        )
        Spacer(Modifier.height(8.dp))

        // Placeholder weekly chart — Track A will replace with real 7-day data
        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val entries = days.map { BarEntry(it, 0f) }

        SimpleBarChart(
            entries = entries,
            chartHeight = 140.dp
        )

        Spacer(Modifier.height(8.dp))
        Text(
            "Consumed: ${snapshot?.consumedCalories?.toInt() ?: 0} kcal",
            style = MaterialTheme.typography.bodySmall,
            color = FitHubPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}