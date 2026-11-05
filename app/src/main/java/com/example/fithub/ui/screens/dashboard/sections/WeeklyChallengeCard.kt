package com.example.fithub.ui.screens.dashboard.sections

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fithub.R
import com.example.fithub.ui.components.RoundedCard
import com.example.fithub.ui.components.RoundedProgressBar
import com.example.fithub.ui.theme.FitHubPrimary
import com.example.fithub.ui.theme.SuccessGreen
import com.example.fithub.ui.theme.TextSecondary

/**
 * Phase 6 (UDF 5): weekly challenge = "Complete N workouts this week",
 * where N comes from `WorkoutGoals.sessionsPerWeek`.
 */
@Composable
fun WeeklyChallengeCard(
    completed: Int,
    target: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (target > 0) (completed.toFloat() / target.toFloat()).coerceIn(0f, 1f) else 0f
    val isDone = target > 0 && completed >= target
    val remaining = (target - completed).coerceAtLeast(0)

    RoundedCard(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.dashboard_challenge_title),
                style = MaterialTheme.typography.titleMedium,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                if (isDone) stringResource(R.string.dashboard_challenge_done)
                else stringResource(R.string.dashboard_challenge_remaining, remaining),
                style = MaterialTheme.typography.labelSmall,
                color = if (isDone) SuccessGreen else TextSecondary,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.dashboard_challenge_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        Spacer(Modifier.height(10.dp))
        RoundedProgressBar(
            progress = progress,
            height = 10.dp,
            fillColor = if (isDone) SuccessGreen else FitHubPrimary
        )
        Spacer(Modifier.height(6.dp))
        Text(
            stringResource(
                R.string.dashboard_challenge_progress_format,
                completed,
                target
            ),
            style = MaterialTheme.typography.titleSmall,
            color = FitHubPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}