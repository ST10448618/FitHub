package com.example.fithub.ui.screens.journal.sections

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fithub.R
import com.example.fithub.domain.model.WorkoutSession
import com.example.fithub.ui.components.EmptyState
import com.example.fithub.ui.components.RoundedCard
import com.example.fithub.ui.components.SectionHeader
import com.example.fithub.ui.theme.FitHubPrimary
import com.example.fithub.ui.theme.TextSecondary
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun JournalWorkoutsCard(
    sessions: List<WorkoutSession>,
    onViewAllClick: () -> Unit
) {
    val locale = Locale.getDefault()
    val completedFormatter = DateTimeFormatter
        .ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
        .withLocale(locale)

    RoundedCard(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(
            title = stringResource(R.string.journal_workouts_title),
            actionText = stringResource(R.string.common_view_all),
            onAction = onViewAllClick
        )

        if (sessions.isEmpty()) {
            EmptyState(
                title = stringResource(R.string.journal_workouts_empty_title),
                message = stringResource(R.string.journal_workouts_empty_body),
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
                            stringResource(
                                R.string.journal_workouts_exercises_minutes_format,
                                session.exercises.size,
                                session.durationMinutes
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            stringResource(
                                R.string.journal_workouts_kcal_format,
                                session.estimatedActivityKcal
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                    Text(
                        session.completedAt.format(completedFormatter),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}