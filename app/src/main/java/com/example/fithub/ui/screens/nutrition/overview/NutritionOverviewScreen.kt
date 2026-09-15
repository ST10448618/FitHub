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

            DaySelector(
                label = state.dateLabel,
                isToday = state.isToday,
                onPrev = { viewModel.shiftDay(-1) },
                onNext = { viewModel.shiftDay(1) }
            )

            Spacer(Modifier.height(14.dp))

            // ---------- Summary cards ----------
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SummaryCard(
                    label = "Daily Intake",
                    value = "${state.consumedCalories.toInt()} kcal",
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    label = "Daily Goal",
                    value = "${state.goals?.userDailyCalories ?: 0} kcal",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(14.dp))

            // ---------- Overall progress for the day ----------
            val target = state.goals?.userDailyCalories ?: 0
            val pct = if (target > 0)
                ((state.consumedCalories / target) * 100).toInt().coerceIn(0, 200) else 0
            val below = (target - state.consumedCalories).toInt()

            RoundedCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Daily Nutrition Progress",
                    style = MaterialTheme.typography.titleMedium,
                    color = FitHubPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(10.dp))
                RoundedProgressBar(
                    progress = (state.consumedCalories / target.coerceAtLeast(1)).toFloat()
                        .coerceIn(0f, 1f),
                    height = 12.dp
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (below > 0) "$below kcal remaining"
                        else if (below < 0) "${-below} kcal exceeded"
                        else "On target!",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (below < 0) ErrorRed else TextSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    PctPill("$pct% of goal", FitHubLightBlue, FitHubPrimary)
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---------- Macro goals ----------
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
                    actual = state.consumedProtein,
                    target = state.goals?.macroTargets?.proteinG ?: 0,
                    color = MacroProtein,
                    modifier = Modifier.weight(1f)
                )
                MacroDonutCard(
                    label = "CARBS",
                    actual = state.consumedCarbs,
                    target = state.goals?.macroTargets?.carbsG ?: 0,
                    color = MacroCarbs,
                    modifier = Modifier.weight(1f)
                )
                MacroDonutCard(
                    label = "FATS",
                    actual = state.consumedFat,
                    target = state.goals?.macroTargets?.fatG ?: 0,
                    color = MacroFats,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(18.dp))

            // ---------- Meals ----------
            Text(
                "Calories by Meal Type",
                style = MaterialTheme.typography.titleMedium,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))

            RoundedCard(modifier = Modifier.fillMaxWidth()) {
                MealBreakdownRow(
                    label = "Breakfast", color = MealBreakfast,
                    actual = state.mealBreakdown.breakfast,
                    target = state.goals?.mealTargets?.breakfastKcal ?: 0
                )
                MealBreakdownRow(
                    label = "Lunch", color = MealLunch,
                    actual = state.mealBreakdown.lunch,
                    target = state.goals?.mealTargets?.lunchKcal ?: 0
                )
                MealBreakdownRow(
                    label = "Dinner", color = MealDinner,
                    actual = state.mealBreakdown.dinner,
                    target = state.goals?.mealTargets?.dinnerKcal ?: 0
                )
                MealBreakdownRow(
                    label = "Snack", color = MealSnack,
                    actual = state.mealBreakdown.snack,
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

// ---------- Helpers ----------

@Composable
private fun PctPill(
    text: String,
    background: Color = FitHubLightBlue,
    contentColor: Color = FitHubPrimary
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DaySelector(
    label: String,
    isToday: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        IconButton(onClick = onPrev) {
            Icon(
                Icons.Filled.ChevronLeft,
                contentDescription = "Previous day",
                tint = FitHubPrimary
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                label,
                style = MaterialTheme.typography.titleMedium,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            if (isToday) {
                Text(
                    "Today",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }
        IconButton(
            onClick = onNext,
            enabled = !isToday
        ) {
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = "Next day",
                tint = if (isToday) TextHint else FitHubPrimary
            )
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
                progress = if (target > 0)
                    (actual / target).toFloat().coerceIn(0f, 1f) else 0f,
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

    Column(modifier = Modifier.padding(vertical = 10.dp)) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(50))
                    .background(color)
            )
            Spacer(Modifier.width(12.dp))

            Text(
                label,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            Text(
                "${actual.toInt()} kcal / $target kcal",
                style = MaterialTheme.typography.labelSmall,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(6.dp))

        Row(
            modifier = Modifier.padding(start = 44.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                when {
                    delta > 5 -> "$delta kcal under daily goal"
                    delta < -5 -> "${-delta} kcal over daily goal"
                    else -> "On Target!"
                },
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                modifier = Modifier.weight(1f)
            )
            PctPill(
                text = "$pct% of target",
                background = FitHubLightBlue,
                contentColor = color
            )
        }

        Spacer(Modifier.height(8.dp))

        Row(modifier = Modifier.padding(start = 44.dp)) {
            LinearProgressIndicator(
                progress = {
                    if (target > 0) (actual / target).toFloat().coerceIn(0f, 1f) else 0f
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = color,
                trackColor = SurfaceGray.copy(alpha = 0.4f)
            )
        }
    }
}