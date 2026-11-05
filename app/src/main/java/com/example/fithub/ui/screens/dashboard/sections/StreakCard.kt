package com.example.fithub.ui.screens.dashboard.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fithub.R
import com.example.fithub.ui.components.RoundedCard
import com.example.fithub.ui.theme.*

/**
 * Phase 6 (UDF 5): shows the user's current streak with a "at risk" hint when
 * today's activity hasn't been logged yet. Purely presentational — the caller
 * supplies the computed streak fields.
 */
@Composable
fun StreakCard(
    currentStreak: Int,
    longestStreak: Int,
    todayDone: Boolean,
    atRisk: Boolean,
    modifier: Modifier = Modifier
) {
    RoundedCard(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFFF8A00), Color(0xFFE65100))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("🔥", style = MaterialTheme.typography.headlineMedium)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pluralStringResource(
                        R.plurals.dashboard_streak_days_format,
                        currentStreak,
                        currentStreak
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    color = FitHubPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = when {
                        currentStreak == 0 -> stringResource(R.string.dashboard_streak_empty_hint)
                        atRisk -> stringResource(R.string.dashboard_streak_at_risk_hint)
                        todayDone -> stringResource(R.string.dashboard_streak_today_done_hint)
                        else -> stringResource(R.string.dashboard_streak_keep_going_hint)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    stringResource(R.string.dashboard_streak_best_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Text(
                    stringResource(R.string.dashboard_streak_best_format, longestStreak),
                    style = MaterialTheme.typography.titleSmall,
                    color = FitHubPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}