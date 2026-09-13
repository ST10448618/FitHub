package com.example.fithub.ui.screens.dashboard.sections

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fithub.ui.components.RoundedCard
import com.example.fithub.ui.components.RoundedProgressBar
import com.example.fithub.ui.components.SectionHeader
import com.example.fithub.ui.theme.FitHubPrimary
import com.example.fithub.ui.theme.TextSecondary

// ============================================================
// OWNER: Track B (Muhammad Akeel)
// ============================================================
@Composable
fun WeeklyWorkoutGoalSection(
    weeklyCompleted: Int,
    weeklyTarget: Int,
    weeklyProgress: Int,
    onViewMoreClick: () -> Unit
) {
    RoundedCard(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(
            title = "Weekly Workout Goal",
            actionText = "view more",
            onAction = onViewMoreClick
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$weeklyCompleted / $weeklyTarget sessions completed",
                style = MaterialTheme.typography.bodyMedium,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$weeklyProgress%",
                style = MaterialTheme.typography.titleSmall,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(8.dp))
        RoundedProgressBar(progress = weeklyProgress / 100f)
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Keep going — you're building consistency.",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}