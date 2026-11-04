package com.example.fithub.ui.screens.plans.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fithub.R
import com.example.fithub.ui.components.RoundedCard
import com.example.fithub.ui.components.SuccessOverlay
import com.example.fithub.ui.theme.*
import com.example.fithub.ui.util.localized

@Composable
fun CreateWorkoutStep4Screen(
    onBack: () -> Unit,
    onCancelAll: () -> Unit,
    onCreated: () -> Unit,
    viewModel: CreateWorkoutViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(state.created) {
        if (state.created) showSuccess = true
    }

    CreateWorkoutScaffold(
        stepNumber = 4,
        title = stringResource(R.string.create_step4_title),
        subtitle = stringResource(R.string.create_step4_subtitle),
        onCancel = {
            viewModel.cancelAndReset()
            onCancelAll()
        },
        onNext = { viewModel.createPlan() },
        nextEnabled = !state.isCreating,
        nextButtonText = stringResource(
            if (state.isCreating) R.string.create_step4_creating
            else R.string.create_step4_create_plan
        )
    ) {
        RoundedCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                stringResource(R.string.create_step4_plan_details),
                style = MaterialTheme.typography.titleSmall,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))

            DetailField(
                stringResource(R.string.create_step1_name_label),
                state.draft.name.ifBlank { "—" }
            )
            Spacer(Modifier.height(10.dp))
            DetailField(
                stringResource(R.string.create_step1_description_label),
                state.draft.description.ifBlank { "—" }
            )
            Spacer(Modifier.height(10.dp))
            DetailField(
                stringResource(R.string.create_step1_difficulty_label),
                state.draft.difficulty.localized()
            )
            Spacer(Modifier.height(10.dp))
            DetailField(
                stringResource(R.string.create_step1_equipment_label),
                state.draft.equipment
            )
        }

        Spacer(Modifier.height(14.dp))

        RoundedCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                stringResource(R.string.create_step4_exercise_details),
                style = MaterialTheme.typography.titleSmall,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))

            state.draft.exercises.forEach { ex ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(FitHubLightBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            ex.exerciseName.firstOrNull()?.toString() ?: "E",
                            style = MaterialTheme.typography.labelMedium,
                            color = FitHubPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            ex.exerciseName,
                            style = MaterialTheme.typography.titleSmall,
                            color = FitHubPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        val meta = if (ex.targetDurationSeconds > 0)
                            stringResource(
                                R.string.create_step4_duration_format,
                                ex.targetDurationSeconds
                            )
                        else
                            stringResource(R.string.create_step3_reps_format, ex.targetReps)
                        Text(
                            meta,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                    Text(
                        "~" + stringResource(R.string.create_step3_calories_format, ex.estimatedActivityKcal.toInt()),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        RoundedCard(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth()) {
                EstimateCell(
                    label = stringResource(R.string.create_step4_estimated_calories),
                    value = stringResource(
                        R.string.create_step4_calories_value_format,
                        state.estimatedActivityKcal
                    ),
                    modifier = Modifier.weight(1f)
                )
                EstimateCell(
                    label = stringResource(R.string.create_step4_estimated_time),
                    value = stringResource(
                        R.string.create_step4_minutes_value_format,
                        state.estimatedDurationMinutes
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (state.errorMessage != null) {
            Spacer(Modifier.height(12.dp))
            Text(state.errorMessage!!, color = ErrorRed)
        }
    }

    if (showSuccess) {
        SuccessOverlay(
            title = stringResource(R.string.create_step4_success_title),
            message = stringResource(R.string.create_step4_success_message),
            buttonText = stringResource(R.string.create_step4_success_confirm),
            onDismiss = {
                viewModel.cancelAndReset()
                onCreated()
            }
        )
    }
}

@Composable
private fun DetailField(label: String, value: String) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
        Spacer(Modifier.height(2.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleSmall,
            color = FitHubPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EstimateCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
        Spacer(Modifier.height(2.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleSmall,
            color = FitHubPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}