package com.example.fithub.ui.screens.journal.sections

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fithub.domain.model.WorkoutSession
import com.example.fithub.ui.components.EmptyState
import com.example.fithub.ui.components.RoundedCard
import com.example.fithub.ui.components.SectionHeader
import com.example.fithub.ui.theme.FitHubPrimary
import com.example.fithub.ui.theme.TextSecondary
import java.time.format.DateTimeFormatter

// ============================================================
// OWNER: Track B (Muhammad Akeel / Perez Seth Roy)
// ============================================================
@Composable
fun JournalWorkoutsCard(
    sessions: List<WorkoutSession>,
    onViewAllClick: () -> Unit
) {
    RoundedCard(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(
            title = "Completed Workouts",
            actionText = "view all",
            onAction = onViewAllClick
        )

        if (sessions.isEmpty()) {
            EmptyState(
                title = "No workouts yet",
                message = "Your completed sessions will show up here.",
                emoji = "🏋️"
            )
        } else {
            sessions.forEach { session ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            session.workoutName,
                            style = MaterialTheme.typography.titleSmall,
                            color = FitHubPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "${session.exercises.size} exercises • ${session.durationMinutes} minutes",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "${session.estimatedActivityKcal} kcal estimated",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                    Text(
                        session.completedAt.format(
                            DateTimeFormatter.ofPattern("d MMM, HH:mm")
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}