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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fithub.domain.model.WorkoutCategory
import com.example.fithub.ui.charts.BarEntry
import com.example.fithub.ui.charts.DonutChart
import com.example.fithub.ui.charts.DonutSegment
import com.example.fithub.ui.charts.SimpleLineChart
import com.example.fithub.ui.components.*
import com.example.fithub.ui.theme.*
import java.time.format.DateTimeFormatter

@Composable
fun WorkoutsOverviewScreen(
    onBack: () -> Unit,
    viewModel: WorkoutsOverviewViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        AppHeader(title = "Workouts Overview", onBack = onBack)

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

            // Month selector
            MonthSelector(
                month = state.selectedMonth,
                onPrevious = viewModel::onPreviousMonth,
                onNext = viewModel::onNextMonth
            )

            Spacer(Modifier.height(12.dp))

            // Top tiles
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricTile(
                    label = "Total Activity",
                    value = "${state.totalActivityKcal} kcal",
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    label = "Active Time",
                    value = formatMinutes(state.totalActiveMinutes),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Monthly Activity Goal
            RoundedCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Monthly Activity Goal",
                    style = MaterialTheme.typography.titleSmall,
                    color = FitHubPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${state.totalActivityKcal} kcal / ${state.monthlyGoalKcal} kcal",
                        style = MaterialTheme.typography.labelMedium,
                        color = FitHubPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "${state.monthlyProgressPercent}% of target",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                }
                Spacer(Modifier.height(8.dp))
                RoundedProgressBar(
                    progress = state.monthlyProgressPercent / 100f,
                    height = 10.dp,
                    fillColor = FitHubPrimary
                )
                Spacer(Modifier.height(6.dp))
                val remaining = (state.monthlyGoalKcal - state.totalActivityKcal).coerceAtLeast(0)
                Text(
                    "$remaining kcal below monthly goal",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(16.dp))

            // Graph card with mode toggle
            RoundedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        state.graphTitle,
                        style = MaterialTheme.typography.titleSmall,
                        color = FitHubPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    GraphModeToggle(
                        mode = state.graphMode,
                        onModeChange = viewModel::onGraphModeChange
                    )
                }

                Spacer(Modifier.height(16.dp))

                if (state.graphEntries.isEmpty()) {
                    Text(
                        "No workout activity to show.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                } else {
                    SimpleLineChart(
                        entries = state.graphEntries.map { BarEntry(it.first, it.second) },
                        chartHeight = 160.dp,
                        lineColor = FitHubPrimary,
                        fillColor = FitHubPrimary.copy(alpha = 0.18f)
                    )
                }

                Spacer(Modifier.height(6.dp))
                Text(
                    "Values are based on completed sessions • estimates",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(16.dp))

            // Workout Habits
            RoundedCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Your Workout Habits",
                    style = MaterialTheme.typography.titleSmall,
                    color = FitHubPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                // Donut showing category distribution
                if (state.categoryBreakdown.isNotEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        DonutChart(
                            segments = state.categoryBreakdown.map { item ->
                                DonutSegment(
                                    value = item.count.toFloat(),
                                    color = categoryColor(item.category),
                                    label = categoryLabel(item.category)
                                )
                            },
                            size = 180.dp,
                            strokeWidth = 30.dp
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // Training Summary
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(FitHubLightBlue.copy(alpha = 0.4f))
                        .padding(14.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Training Summary",
                            style = MaterialTheme.typography.labelMedium,
                            color = FitHubPrimary,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Total Sessions Completed : ${state.habits.totalSessions} Sessions",
                            style = MaterialTheme.typography.bodySmall,
                            color = FitHubPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Average Session Length : ${state.habits.averageSessionMinutes} minutes",
                            style = MaterialTheme.typography.bodySmall,
                            color = FitHubPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Longest Session Completed : ${state.habits.longestSessionMinutes} minutes",
                            style = MaterialTheme.typography.bodySmall,
                            color = FitHubPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                // Category list
                if (state.categoryBreakdown.isEmpty()) {
                    Text(
                        "No categories to display yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                } else {
                    state.categoryBreakdown.forEach { item ->
                        CategoryRow(
                            category = item.category,
                            count = item.count
                        )
                        Spacer(Modifier.height(8.dp))
                    }
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
            modifier = Modifier.size(36.dp).clip(CircleShape)
        ) {
            Icon(
                Icons.Filled.KeyboardArrowLeft,
                contentDescription = "Previous month",
                tint = FitHubPrimary
            )
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
            modifier = Modifier.size(36.dp).clip(CircleShape)
        ) {
            Icon(
                Icons.Filled.KeyboardArrowRight,
                contentDescription = "Next month",
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
            .height(76.dp)
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
            Spacer(Modifier.height(4.dp))
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
private fun GraphModeToggle(
    mode: GraphMode,
    onModeChange: (GraphMode) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(BackgroundGray)
            .padding(2.dp)
    ) {
        GraphMode.entries.forEach { m ->
            val selected = m == mode
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selected) FitHubPrimary else Color.Transparent)
                    .clickable { onModeChange(m) }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    m.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) TextOnPrimary else TextSecondary,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun CategoryRow(category: WorkoutCategory, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BackgroundGray)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(categoryColor(category))
        )
        Spacer(Modifier.width(10.dp))
        Text(
            categoryLabel(category),
            style = MaterialTheme.typography.titleSmall,
            color = FitHubPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Text(
            "$count sessions",
            style = MaterialTheme.typography.labelMedium,
            color = FitHubPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

// ---------- helpers ----------

private fun categoryLabel(category: WorkoutCategory): String = when (category) {
    WorkoutCategory.GENERAL_FITNESS -> "General"
    WorkoutCategory.CARDIO -> "Cardio"
    WorkoutCategory.STRENGTH -> "Strength"
    WorkoutCategory.ENDURANCE -> "Endurance"
    WorkoutCategory.MOBILITY -> "Mobility"
    WorkoutCategory.CREATED -> "Created"
}

private fun categoryColor(category: WorkoutCategory): Color = when (category) {
    WorkoutCategory.GENERAL_FITNESS -> CategoryGeneral
    WorkoutCategory.CARDIO -> CategoryCardio
    WorkoutCategory.STRENGTH -> CategoryStrength
    WorkoutCategory.ENDURANCE -> CategoryEndurance
    WorkoutCategory.MOBILITY -> CategoryMobility
    WorkoutCategory.CREATED -> CategoryCreated
}

private fun formatMinutes(totalMinutes: Int): String {
    if (totalMinutes < 60) return "${totalMinutes}m"
    val hours = totalMinutes / 60
    val mins = totalMinutes % 60
    return "${hours}h ${mins}m"
}