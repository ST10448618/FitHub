package com.example.fithub.ui.screens.dashboard.sections

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fithub.domain.calculator.NutritionProgressCalculator
import com.example.fithub.ui.charts.DonutProgress
import com.example.fithub.ui.components.RoundedCard
import com.example.fithub.ui.theme.*

// ============================================================
// OWNER: Track A (Perez) — Nutrition metrics & Add Meal button
// SHARED: Add Workout button belongs to Track B
// ============================================================
@Composable
fun TodaysOverviewSection(
    snapshot: NutritionProgressCalculator.DashboardNutrition?,
    onAddMealClick: () -> Unit,
    onAddWorkoutClick: () -> Unit
) {
    RoundedCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Today's Overview",
            style = MaterialTheme.typography.titleMedium,
            color = FitHubPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Calories donut placeholder (uses current/remaining)
            val consumed = snapshot?.consumedCalories ?: 0.0
            val target = (consumed + (snapshot?.remainingCalories ?: 0.0))
            val progress = if (target > 0) (consumed / target).toFloat() else 0f

            Box(modifier = Modifier.size(120.dp)) {
                DonutProgress(
                    progress = progress,
                    size = 120.dp,
                    strokeWidth = 16.dp,
                    centerValue = "${consumed.toInt()}",
                    centerLabel = "of ${target.toInt()} kcal"
                )
            }

            Spacer(Modifier.width(16.dp))

            Column {
                MetricRow(
                    icon = "🔥",
                    label = "Estimated Burnt",
                    value = "0 kcal",
                    color = ErrorRed
                )
                Spacer(Modifier.height(8.dp))
                MetricRow(
                    icon = "🍽",
                    label = "Consumed",
                    value = "${consumed.toInt()} kcal",
                    color = MealDinner
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Macro bars
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MacroMiniBar(
                label = "Protein",
                actual = snapshot?.consumedProtein ?: 0.0,
                target = snapshot?.proteinTargetG ?: 0,
                color = MacroProtein,
                modifier = Modifier.weight(1f)
            )
            MacroMiniBar(
                label = "Carbs",
                actual = snapshot?.consumedCarbs ?: 0.0,
                target = snapshot?.carbsTargetG ?: 0,
                color = MacroCarbs,
                modifier = Modifier.weight(1f)
            )
            MacroMiniBar(
                label = "Fats",
                actual = snapshot?.consumedFat ?: 0.0,
                target = snapshot?.fatTargetG ?: 0,
                color = MacroFats,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onAddMealClick,
                modifier = Modifier.weight(1f).height(44.dp),
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(
                    containerColor = FitHubPrimary,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Filled.Restaurant, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Add Meal", fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onAddWorkoutClick,
                modifier = Modifier.weight(1f).height(44.dp),
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(
                    containerColor = FitHubSecondary,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Filled.FitnessCenter, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Add Workout", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun MetricRow(icon: String, label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(icon)
        Spacer(Modifier.width(8.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text(value, style = MaterialTheme.typography.titleSmall, color = color, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MacroMiniBar(
    label: String,
    actual: Double,
    target: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    val pct = if (target > 0) (actual / target).toFloat().coerceIn(0f, 1f) else 0f
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { pct },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = color,
            trackColor = SurfaceGray.copy(alpha = 0.4f)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "${actual.toInt()}/${target}g",
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}