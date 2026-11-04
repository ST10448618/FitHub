// AI-assisted (DeepSeek); reviewed and tested by the FitHub team.
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
import com.example.fithub.ui.components.SectionHeader
import com.example.fithub.ui.theme.FitHubPrimary
import com.example.fithub.ui.theme.TextSecondary

@Composable
fun WeeklyWorkoutGoalSection(
    weeklyCompleted: Int,
    weeklyTarget: Int,
    weeklyProgress: Int,
    onViewMoreClick: () -> Unit
) {
    RoundedCard(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(
            title = stringResource(R.string.dashboard_weekly_workout_title),
            actionText = stringResource(R.string.common_view_more),
            onAction = onViewMoreClick
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(
                    R.string.dashboard_weekly_workout_sessions_format,
                    weeklyCompleted,
                    weeklyTarget
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                stringResource(R.string.dashboard_percent_format, weeklyProgress),
                style = MaterialTheme.typography.titleSmall,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(8.dp))
        RoundedProgressBar(progress = weeklyProgress / 100f)
        Spacer(Modifier.height(6.dp))
        Text(
            stringResource(R.string.dashboard_weekly_workout_hint),
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}