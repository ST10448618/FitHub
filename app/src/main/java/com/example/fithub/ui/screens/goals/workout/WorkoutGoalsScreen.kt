package com.example.fithub.ui.screens.goals.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource          // ← ADDED
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fithub.R                            // ← ADDED
import com.example.fithub.ui.components.*
import com.example.fithub.ui.theme.*

@Composable
fun WorkoutGoalsScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: WorkoutGoalsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.saveComplete) {
        if (state.saveComplete) onSaved()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        AppHeader(title = stringResource(R.string.workout_goals_title), onBack = onBack)

        if (state.isLoading) {
            LoadingState()
            return@Column
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Weekly session goal card
            RoundedCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    stringResource(R.string.dashboard_weekly_workout_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = FitHubPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    stringResource(R.string.workout_goals_weekly_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    (1..7).forEach { day ->
                        DayDot(
                            day = day,
                            selected = day <= state.sessionsPerWeek,
                            onClick = { viewModel.onSessionsChange(day) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Monthly activity goal card
            RoundedCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    stringResource(R.string.workout_goals_monthly_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = FitHubPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    stringResource(R.string.workout_goals_monthly_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(Modifier.height(14.dp))

                NumberStepper(
                    value = state.monthlyActivityGoalKcal,
                    onValueChange = viewModel::onMonthlyChange,
                    step = 500,
                    min = 500,
                    max = 100000,
                    unit = stringResource(R.string.common_unit_kcal)
                )
            }

            if (state.errorMessage != null) {
                Spacer(Modifier.height(12.dp))
                Text(state.errorMessage!!, color = ErrorRed)
            }

            Spacer(Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SecondaryButton(
                    text = stringResource(R.string.generic_cancel),
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                )
                PrimaryButton(
                    text = if (state.isSaving) stringResource(R.string.nutrition_goals_saving)
                    else stringResource(R.string.nutrition_goals_confirm),
                    onClick = viewModel::save,
                    enabled = !state.isSaving,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DayDot(day: Int, selected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(if (selected) FitHubPrimary else SurfaceGray.copy(alpha = 0.4f))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.titleSmall,
                color = if (selected) TextOnPrimary else TextSecondary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}