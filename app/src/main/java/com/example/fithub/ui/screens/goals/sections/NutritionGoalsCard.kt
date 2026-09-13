package com.example.fithub.ui.screens.goals.sections

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fithub.domain.model.NutritionGoals
import com.example.fithub.ui.components.RoundedCard
import com.example.fithub.ui.components.SmallActionButton
import com.example.fithub.ui.theme.FitHubPrimary
import com.example.fithub.ui.theme.TextSecondary

// ============================================================
// OWNER: Track A (Perez) — nutrition goals
// ============================================================
@Composable
fun NutritionGoalsCard(
    goals: NutritionGoals?,
    onManageClick: () -> Unit
) {
    RoundedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Nutrition Goals",
                style = MaterialTheme.typography.titleMedium,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            SmallActionButton(text = "Manage →", onClick = onManageClick)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "${goals?.userDailyCalories ?: 0} kcal",
            style = MaterialTheme.typography.headlineMedium,
            color = FitHubPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Avg. Daily Calories",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}