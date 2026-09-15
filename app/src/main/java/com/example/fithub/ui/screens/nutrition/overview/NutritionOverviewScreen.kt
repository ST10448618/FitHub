package com.example.fithub.ui.screens.nutrition.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fithub.ui.charts.DonutProgress
import com.example.fithub.ui.components.*
import com.example.fithub.ui.theme.*

@Composable
fun NutritionOverviewScreen(
    onBack: () -> Unit,
    onEditGoals: () -> Unit,
    viewModel: NutritionOverviewViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        AppHeader(title = "Nutrition Overview", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            MonthSelector(
                label = state.monthLabel,
                onPrev = { viewModel.shiftMonth(-1) },
                onNext = { viewModel.shiftMonth(1) }
            )

            Spacer(Modifier.height(14.dp))

            // Summary cards
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SummaryCard(
                    label = "Average Daily Intake",
                    value = "${state.avgDailyIntake.toInt()} kcal",
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    label = "Daily Goal",
                    value = "${state.goals?.userDailyCalories ?: 0} kcal",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(14.dp))

            // Overall progress
            val target = state.goals?.userDailyCalories ?: 0
            val pct = if (target > 0)
                ((state.avgDailyIntake / target) * 100).toInt().coerceIn(0, 200) else 0
            val below = (target - state.avgDailyIntake).toInt()

            RoundedCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Overall Nutrition Progress",
                    style = MaterialTheme.typography.titleMedium,
                    color = FitHubPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(10.dp))
                RoundedProgressBar(
                    progress = (state.avgDailyIntake / target.coerceAtLeast(1)).toFloat()
                        .coerceIn(0f, 1f),
                    height = 12.dp
                )
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        if (below > 0) "$below kcal below daily goal" else "${-below} kcal above daily goal",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "$pct% of target",
                        style = MaterialTheme.typography.labelSmall,
                        color = FitHubPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Macro goals
            Text(
                "Macro Goals",
                style = MaterialTheme.typography.titleMedium,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MacroDonutCard(
                    label = "PROTEIN",
                    actual = state.avgProtein,
                    target = state.goals?.macroTargets?.proteinG ?: 0,
                    color = MacroProtein,
                    modifier = Modifier.weight(1f)
                )
                MacroDonutCard(
                    label = "CARBS",
                    actual = state.avgCarbs,
                    target = state.goals?.macroTargets?.carbsG ?: 0,
                    color = MacroCarbs,
                    modifier = Modifier.weight(1f)
                )
                MacroDonutCard(
                    label = "FATS",
                    actual = state.avgFat,
                    target = state.goals?.macroTargets?.fatG ?: 0,
                    color = MacroFats,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(18.dp))

            // Meals
            Text(
                "Average Calories by Meal Type",
                style = MaterialTheme.typography.titleMedium,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))

            RoundedCard(modifier = Modifier.fillMaxWidth()) {
                MealBreakdownRow(
                    label = "Breakfast", color = MealBreakfast,
                    actual = state.avgMealBreakdown.breakfast,
                    target = state.goals?.mealTargets?.breakfastKcal ?: 0
                )
                MealBreakdownRow(
                    label = "Lunch", color = MealLunch,
                    actual = state.avgMealBreakdown.lunch,
                    target = state.goals?.mealTargets?.lunchKcal ?: 0
                )
                MealBreakdownRow(
                    label = "Dinner", color = MealDinner,
                    actual = state.avgMealBreakdown.dinner,
                    target = state.goals?.mealTargets?.dinnerKcal ?: 0
                )
                MealBreakdownRow(
                    label = "Snack", color = MealSnack,
                    actual = state.avgMealBreakdown.snack,
                    target = state.goals?.mealTargets?.snackKcal ?: 0
                )
            }

            Spacer(Modifier.height(16.dp))

            PrimaryButton(
                text = "✏  Edit Goals",
                onClick = onEditGoals
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun MonthSelector(label: String, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        IconButton(onClick = onPrev) {
            Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous month",
                tint = FitHubPrimary)
        }
        Text(
            label,
            style = MaterialTheme.typography.titleMedium,
            color = FitHubPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        IconButton(onClick = onNext) {
            Icon(Icons.Filled.ChevronRight, contentDescription = "Next month",
                tint = FitHubPrimary)
        }
    }
}

@Composable
private fun SummaryCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = FitHubLightBlue)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
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
private fun MacroDonutCard(
    label: String,
    actual: Double,
    target: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            DonutProgress(
                progress = if (target > 0) (actual / target).toFloat().coerceIn(0f, 1f) else 0f,
                size = 74.dp,
                strokeWidth = 10.dp,
                progressColor = color,
                centerValue = "${actual.toInt()}g",
                centerLabel = "/${target}g"
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Consumed",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun MealBreakdownRow(
    label: String,
    color: Color,
    actual: Double,
    target: Int
) {
    val delta = target - actual.toInt()
    val pct = if (target > 0)
        ((actual / target) * 100).toInt().coerceIn(0, 200) else 0

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color)
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    when {
                        delta > 5 -> "$delta kcal under daily goal"
                        delta < -5 -> "${-delta} kcal over daily goal"
                        else -> "On Target!"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
            Text(
                "${actual.toInt()} kcal / $target kcal",
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            LinearProgressIndicator(
                progress = {
                    if (target > 0) (actual / target).toFloat().coerceIn(0f, 1f) else 0f
                },
                modifier = Modifier.weight(1f).height(6.dp),
                color = color,
                trackColor = SurfaceGray.copy(alpha = 0.4f)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "$pct% of target",
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}