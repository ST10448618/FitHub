package com.example.fithub.ui.screens.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fithub.ui.charts.BarEntry
import com.example.fithub.ui.charts.SimpleBarChart
import com.example.fithub.ui.components.*
import com.example.fithub.ui.theme.*
import java.time.format.DateTimeFormatter

@Composable
fun ProgressOverviewScreen(
    onBack: () -> Unit,
    onNutritionOverview: () -> Unit,
    onBodyWeight: () -> Unit,
    onWorkoutsOverview: () -> Unit,
    viewModel: ProgressOverviewViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        AppHeader(title = "Progress Overview", onBack = onBack)

        if (state.isLoading) {
            LoadingState()
            return@Column
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // ---- Month selector ----
            MonthSelector(
                month = state.selectedMonth,
                onPrevious = viewModel::onPreviousMonth,
                onNext = viewModel::onNextMonth
            )

            Spacer(Modifier.height(16.dp))

            // ---- Top 4 metric cards ----
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricTile(
                        label = "Current weight",
                        value = state.currentWeightKg?.let { "${it.toInt()} kg" } ?: "—",
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        label = "Goal weight",
                        value = state.targetWeightKg?.let { "${it.toInt()} kg" } ?: "—",
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricTile(
                        label = "Average Intake",
                        value = "${state.averageDailyIntakeKcal} kcal",
                        modifier = Modifier.weight(1f)
                    )
                    MetricTile(
                        label = "Activity",
                        value = "${state.totalMonthlyActivityKcal} kcal",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ---- Overall Goal Progress bar ----
            RoundedCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Overall Goal Progress",
                    style = MaterialTheme.typography.titleSmall,
                    color = FitHubPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        state.startingWeightKg?.let { "${it.toInt()} kg" } ?: "—",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        state.currentWeightKg?.let { "${it.toInt()} kg" } ?: "—",
                        style = MaterialTheme.typography.labelMedium,
                        color = FitHubPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        state.targetWeightKg?.let { "${it.toInt()} kg" } ?: "—",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                }
                Spacer(Modifier.height(6.dp))
                RoundedProgressBar(
                    progress = state.weightProgressPercent / 100f,
                    height = 10.dp,
                    fillColor = FitHubPrimary
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "${state.weightProgressPercent}% toward goal",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(20.dp))

            // ---- Progress Overview by Category ----
            Text(
                "Progress Overview by Category",
                style = MaterialTheme.typography.titleMedium,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))

            // Weight & Body teaser
            CategoryTeaser(
                title = "Weight & Body",
                onViewClick = onBodyWeight
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        state.bmi?.let { String.format("%.2f", it) } ?: "—",
                        style = MaterialTheme.typography.headlineMedium,
                        color = FitHubPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(12.dp))
                    BmiBadge(state.bmiLabel)
                }
                Spacer(Modifier.height(10.dp))
                BmiScaleBar(state.bmi)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BmiLegendDot("Under weight", WarningOrange)
                    BmiLegendDot("Healthy", SuccessGreen)
                    BmiLegendDot("Overweight", Color(0xFFFFC107))
                    BmiLegendDot("Obese", ErrorRed)
                }
            }

            Spacer(Modifier.height(14.dp))

            // Nutrition teaser
            CategoryTeaser(
                title = "Nutrition Overview",
                onViewClick = onNutritionOverview
            ) {
                Text(
                    "Intake Analysis",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))

                if (state.weekBars.isEmpty()) {
                    Text(
                        "No intake logged this month.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                } else {
                    SimpleBarChart(
                        entries = state.weekBars.map { BarEntry(it.label, it.kcal.toFloat()) },
                        chartHeight = 130.dp,
                        barGradient = listOf(FitHubMidBlue, FitHubPrimary)
                    )
                }

                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Avg. Daily Intake",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Text(
                            "${state.averageDailyIntakeKcal} kcal",
                            style = MaterialTheme.typography.titleSmall,
                            color = FitHubPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Daily Calorie Goal",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Text(
                            "${state.dailyCalorieGoal} kcal",
                            style = MaterialTheme.typography.titleSmall,
                            color = FitHubPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Workout teaser
            CategoryTeaser(
                title = "Workout Overview",
                onViewClick = onWorkoutsOverview
            ) {
                if (state.weeksInMonth.isEmpty()) {
                    Text(
                        "No workout sessions this month.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                } else {
                    state.weeksInMonth.forEach { week ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                week.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                modifier = Modifier.width(56.dp)
                            )
                            Text(
                                "${week.completed} / ${week.target}",
                                style = MaterialTheme.typography.labelSmall,
                                color = FitHubPrimary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(48.dp)
                            )
                            RoundedProgressBar(
                                progress = if (week.target > 0)
                                    week.completed.toFloat() / week.target else 0f,
                                height = 6.dp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    val totalCompleted = state.weeksInMonth.sumOf { it.completed }
                    val totalTarget = state.weeksInMonth.sumOf { it.target }
                    val adherence = if (totalTarget > 0)
                        (totalCompleted * 100 / totalTarget) else 0

                    Text(
                        "$totalCompleted / $totalTarget planned sessions completed",
                        style = MaterialTheme.typography.labelSmall,
                        color = FitHubPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "$adherence% adherence rate.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun MonthSelector(
    month: java.time.YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    val canGoNext = month < java.time.YearMonth.now()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onPrevious,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
        ) {
            Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Previous", tint = FitHubPrimary)
        }
        Text(
            month.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
            style = MaterialTheme.typography.titleMedium,
            color = FitHubPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        IconButton(
            onClick = onNext,
            enabled = canGoNext,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
        ) {
            Icon(
                Icons.Filled.KeyboardArrowRight,
                contentDescription = "Next",
                tint = if (canGoNext) FitHubPrimary else SurfaceGray
            )
        }
    }
}

@Composable
private fun MetricTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(72.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(CardWhite)
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.align(Alignment.CenterStart)) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CategoryTeaser(
    title: String,
    onViewClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    RoundedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                "view detailed >",
                style = MaterialTheme.typography.labelSmall,
                color = FitHubPrimary,
                modifier = Modifier.clickable(onClick = onViewClick)
            )
        }
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun BmiBadge(label: String) {
    val (bg, fg) = when (label) {
        "Under weight" -> WarningOrange.copy(alpha = 0.15f) to WarningOrange
        "Healthy" -> SuccessGreen.copy(alpha = 0.15f) to SuccessGreen
        "Overweight" -> Color(0xFFFFC107).copy(alpha = 0.15f) to Color(0xFF996A00)
        "Obese" -> ErrorRed.copy(alpha = 0.15f) to ErrorRed
        else -> SurfaceGray to TextSecondary
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = fg,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun BmiScaleBar(bmi: Double?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        WarningOrange,
                        SuccessGreen,
                        Color(0xFFFFC107),
                        ErrorRed
                    )
                )
            )
    ) {
        if (bmi != null) {
            val frac = ((bmi - 15.0) / 25.0).coerceIn(0.0, 1.0).toFloat()
            Box(modifier = Modifier.fillMaxWidth(frac)) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(FitHubPrimary)
                )
            }
        }
    }
}

@Composable
private fun BmiLegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}