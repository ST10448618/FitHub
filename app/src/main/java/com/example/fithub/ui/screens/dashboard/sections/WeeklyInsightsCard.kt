package com.example.fithub.ui.screens.dashboard.sections

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fithub.R
import com.example.fithub.domain.model.WeeklyInsights
import com.example.fithub.ui.components.RoundedCard
import com.example.fithub.ui.theme.FitHubPrimary
import com.example.fithub.ui.theme.SuccessGreen
import com.example.fithub.ui.theme.TextSecondary

/**
 * Phase 6B: server-side weekly insights tile.
 *
 * Shows avg daily calories, weight change, sessions this week and streak
 * length. All values come from the deployed REST API (`GET /api/v1/me/summary`),
 * not from the local Room DB — that's the "custom cloud-hosted REST API with
 * visible data round-trips" learning outcome, in the UI.
 *
 * Renders nothing when [insights] is null AND [isLoading] is false — the
 * caller decides whether to show the tile at all. When loading, shows a small
 * spinner. When loaded, shows the 4 stat rows.
 */
@Composable
fun WeeklyInsightsCard(
    insights: WeeklyInsights?,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    // Hide the card entirely when there's nothing to show and we're not loading.
    if (insights == null && !isLoading) return

    RoundedCard(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.dashboard_insights_title),
                style = MaterialTheme.typography.titleMedium,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Text(
                    stringResource(R.string.dashboard_insights_source),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        if (insights == null) {
            Text(
                stringResource(R.string.dashboard_insights_offline),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
            return@RoundedCard
        }

        InsightRow(
            label = stringResource(R.string.dashboard_insights_avg_calories_label),
            value = stringResource(
                R.string.dashboard_insights_avg_calories_value,
                insights.avgCaloriesPerDay,
            ),
        )
        Spacer(Modifier.height(6.dp))
        InsightRow(
            label = stringResource(R.string.dashboard_insights_weight_change_label),
            value = insights.weightChangeKg?.let { delta ->
                val sign = if (delta >= 0) "+" else ""
                stringResource(R.string.dashboard_insights_weight_change_value, sign, delta)
            } ?: stringResource(R.string.dashboard_insights_no_data),
        )
        Spacer(Modifier.height(6.dp))
        InsightRow(
            label = stringResource(R.string.dashboard_insights_sessions_label),
            value = stringResource(
                R.string.dashboard_insights_sessions_value,
                insights.sessionsThisWeek,
            ),
        )
        Spacer(Modifier.height(6.dp))
        InsightRow(
            label = stringResource(R.string.dashboard_insights_streak_label),
            value = stringResource(
                R.string.dashboard_insights_streak_value,
                insights.currentStreak,
                insights.longestStreak,
            ),
            valueColor = if (insights.currentStreak > 0) SuccessGreen else TextSecondary,
        )
    }
}

@Composable
private fun InsightRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = FitHubPrimary,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.weight(1f),
        )
        Text(
            value,
            style = MaterialTheme.typography.titleSmall,
            color = valueColor,
            fontWeight = FontWeight.Bold,
        )
    }
}