package com.example.fithub.ui.screens.goals.sections

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource          // ← ADDED
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fithub.R                            // ← ADDED
import com.example.fithub.domain.calculator.GoalDirectionCalculator
import com.example.fithub.domain.model.Goal
import com.example.fithub.ui.components.GradientCard
import com.example.fithub.ui.components.SmallActionButton
import com.example.fithub.ui.theme.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color


@Composable
fun GoalJourneyCard(
    startingWeightKg: Double?,
    currentWeightKg: Double?,
    goal: Goal?,
    onManageClick: () -> Unit
) {
    Column {
        // Hero
        GradientCard(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Text("🎯", style = MaterialTheme.typography.titleLarge)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                stringResource(R.string.goals_journey_hero_title),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.goals_journey_hero_body),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f)
            )
        }

        Spacer(Modifier.height(16.dp))

        // My goal journey
        com.example.fithub.ui.components.RoundedCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.goals_my_goal),
                    style = MaterialTheme.typography.titleMedium,
                    color = FitHubPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                SmallActionButton(
                    text = stringResource(R.string.common_manage_arrow),
                    onClick = onManageClick
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = goal?.let { GoalDirectionCalculator.label(it.direction) }
                    ?: stringResource(R.string.goals_no_goal_set),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(Modifier.height(12.dp))

            // Journey row: start → current → goal
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                JourneyPoint(
                    label = stringResource(R.string.goals_journey_start),
                    value = startingWeightKg?.let {
                        stringResource(R.string.journal_body_kg_format, it.toInt())
                    } ?: stringResource(R.string.common_dash),
                    modifier = Modifier.weight(1f)
                )
                Text("→", color = TextSecondary, fontWeight = FontWeight.Bold)
                JourneyPoint(
                    label = stringResource(R.string.goals_journey_current),
                    value = currentWeightKg?.let {
                        stringResource(R.string.journal_body_kg_format, it.toInt())
                    } ?: stringResource(R.string.common_dash),
                    modifier = Modifier.weight(1f),
                    highlight = true
                )
                Text("→", color = TextSecondary, fontWeight = FontWeight.Bold)
                JourneyPoint(
                    label = stringResource(R.string.goals_journey_goal),
                    value = goal?.targetWeightKg?.let {
                        stringResource(R.string.journal_body_kg_format, it.toInt())
                    } ?: stringResource(R.string.common_dash),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun JourneyPoint(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            color = if (highlight) FitHubPrimary else TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}