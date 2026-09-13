package com.example.fithub.ui.screens.dashboard.sections

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fithub.domain.model.WorkoutPlan
import com.example.fithub.ui.components.SectionHeader
import com.example.fithub.ui.components.WorkoutCardHorizontal

// ============================================================
// OWNER: Track B (Muhammad Akeel)
// ============================================================
@Composable
fun RecommendedActivitiesSection(
    plans: List<WorkoutPlan>,
    onPlanClick: (WorkoutPlan) -> Unit,
    onViewPlansClick: () -> Unit
) {
    Column {
        SectionHeader(
            title = "Recommended Activities",
            actionText = "view plans",
            onAction = onViewPlansClick
        )
        if (plans.isEmpty()) {
            Text(
                "No recommendations yet.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(plans, key = { it.id }) { plan ->
                    WorkoutCardHorizontal(
                        plan = plan,
                        onClick = { onPlanClick(plan) }
                    )
                }
            }
        }
    }
}

@Composable
private fun Text(text: String, style: androidx.compose.ui.text.TextStyle) {
    androidx.compose.material3.Text(text = text, style = style)
}