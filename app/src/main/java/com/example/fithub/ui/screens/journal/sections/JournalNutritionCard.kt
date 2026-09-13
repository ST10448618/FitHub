package com.example.fithub.ui.screens.journal.sections

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fithub.domain.model.FoodLog
import com.example.fithub.ui.components.EmptyState
import com.example.fithub.ui.components.RoundedCard
import com.example.fithub.ui.components.SectionHeader
import com.example.fithub.ui.theme.TextSecondary

// ============================================================
// OWNER: Track A (Perez) — nutrition
// ============================================================
@Composable
fun JournalNutritionCard(
    foodLogs: List<FoodLog>,
    onViewAllClick: () -> Unit
) {
    RoundedCard(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(
            title = "Nutrition Journal",
            actionText = "view all",
            onAction = onViewAllClick
        )

        if (foodLogs.isEmpty()) {
            EmptyState(
                title = "No meals logged",
                message = "Tap Add Meal from the Dashboard to log your first meal.",
                emoji = "🍽"
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                userScrollEnabled = false
            ) {
                items(foodLogs) { log ->
                    FoodLogMiniCard(log)
                }
            }
        }
    }
}

@Composable
private fun FoodLogMiniCard(log: FoodLog) {
    androidx.compose.material3.Card(
        modifier = Modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = log.mealType.name.lowercase()
                    .replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = log.foodName,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "${log.calories.toInt()} cal",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}