package com.example.fithub.ui.screens.plans.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fithub.ui.components.CreateWorkoutScaffold
import com.example.fithub.ui.components.RoundedCard
import com.example.fithub.ui.components.SuccessOverlay
import com.example.fithub.ui.theme.*

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
        title = "Review Workout",
        subtitle = "One final check that everything is to your liking. Edit if needed and then confirm to complete!",
        onCancel = {
            viewModel.cancelAndReset()
            onCancelAll()
        },
        onNext = { viewModel.createPlan() },
        nextEnabled = !state.isCreating,
        nextButtonText = if (state.isCreating) "Creating…" else "Create Plan"
    ) {
        // Plan Details
        RoundedCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                "Plan Details",
                style = MaterialTheme.typography.titleSmall,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))

            DetailField("Workout Name", state.draft.name.ifBlank { "—" })
            Spacer(Modifier.height(10.dp))
            DetailField("Description", state.draft.description.ifBlank { "—" })
            Spacer(Modifier.height(10.dp))
            DetailField(
                "Difficulty Level",
                state.draft.difficulty.name.lowercase()
                    .replaceFirstChar { it.uppercase() }
            )
            Spacer(Modifier.height(10.dp))
            DetailField("Equipment Required", state.draft.equipment)
        }

        Spacer(Modifier.height(14.dp))

        // Exercise details
        RoundedCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                "Exercise Details",
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
                            "${ex.targetDurationSeconds}s duration"
                        else
                            "${ex.targetReps} reps"
                        Text(
                            meta,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                    Text(
                        "~${ex.estimatedActivityKcal.toInt()} cal",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Estimates
        RoundedCard(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth()) {
                EstimateCell(
                    label = "Estimated Calories Burnt",
                    value = "${state.estimatedActivityKcal} calories",
                    modifier = Modifier.weight(1f)
                )
                EstimateCell(
                    label = "Estimated Time Spent",
                    value = "${state.estimatedDurationMinutes} minutes",
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
            title = "Workout Plan Created!",
            message = "Your plan has been successfully created and can be accessed from Created Plans",
            buttonText = "Confirm",
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