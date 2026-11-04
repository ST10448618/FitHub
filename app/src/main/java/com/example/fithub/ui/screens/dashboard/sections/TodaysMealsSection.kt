package com.example.fithub.ui.screens.dashboard.sections

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.fithub.R
import com.example.fithub.domain.calculator.NutritionProgressCalculator
import com.example.fithub.ui.components.LabelledProgressBar
import com.example.fithub.ui.components.RoundedCard
import com.example.fithub.ui.components.SectionHeader
import com.example.fithub.ui.theme.*

@Composable
fun TodaysMealsSection(
    snapshot: NutritionProgressCalculator.DashboardNutrition?,
    onViewMoreClick: () -> Unit
) {
    RoundedCard(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(
            title = stringResource(R.string.dashboard_meals_title),
            actionText = stringResource(R.string.common_view_more),
            onAction = onViewMoreClick
        )

        MealRow(
            emoji = "🍳", label = stringResource(R.string.enum_meal_breakfast),
            actual = snapshot?.breakfastActual ?: 0.0,
            target = snapshot?.breakfastTarget ?: 0,
            color = MealBreakfast
        )
        Spacer(Modifier.height(10.dp))
        MealRow(
            emoji = "🥗", label = stringResource(R.string.enum_meal_lunch),
            actual = snapshot?.lunchActual ?: 0.0,
            target = snapshot?.lunchTarget ?: 0,
            color = MealLunch
        )
        Spacer(Modifier.height(10.dp))
        MealRow(
            emoji = "🍽", label = stringResource(R.string.enum_meal_dinner),
            actual = snapshot?.dinnerActual ?: 0.0,
            target = snapshot?.dinnerTarget ?: 0,
            color = MealDinner
        )
        Spacer(Modifier.height(10.dp))
        MealRow(
            emoji = "🍓", label = stringResource(R.string.enum_meal_snack),
            actual = snapshot?.snackActual ?: 0.0,
            target = snapshot?.snackTarget ?: 0,
            color = MealSnack
        )
    }
}

@Composable
private fun MealRow(
    emoji: String,
    label: String,
    actual: Double,
    target: Int,
    color: androidx.compose.ui.graphics.Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(emoji, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            LabelledProgressBar(
                label = label,
                actual = actual,
                target = target,
                fillColor = color,
                showPercent = false
            )
        }
    }
}