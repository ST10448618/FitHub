package com.example.fithub.ui.screens.plans.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fithub.domain.model.Exercise
import com.example.fithub.ui.components.CreateWorkoutScaffold
import com.example.fithub.ui.components.RoundedCard
import com.example.fithub.ui.theme.*

@Composable
fun CreateWorkoutStep3Screen(
    onBack: () -> Unit,
    onNext: () -> Unit,
    viewModel: CreateWorkoutViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val catalog = state.exerciseCatalog.associateBy { it.id }

    CreateWorkoutScaffold(
        stepNumber = 3,
        title = "Configure Exercises",
        subtitle = "Now is where you make this your own. Adjust the Number of Reps or Minutes to your desired amount",
        onCancel = {
            viewModel.cancelAndReset()
            onBack()
        },
        onNext = onNext,
        nextEnabled = state.draft.exercises.isNotEmpty()
    ) {
        Text(
            "Your Workout",
            style = MaterialTheme.typography.titleMedium,
            color = FitHubPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(10.dp))

        state.draft.exercises.forEachIndexed { idx, ex ->
            val catalogEx: Exercise? = catalog[ex.exerciseId]

            ConfigureExerciseRow(
                index = idx + 1,
                exerciseName = ex.exerciseName,
                category = ex.category.name.lowercase()
                    .replaceFirstChar { it.uppercase() },
                isTimeBased = catalogEx?.isTimeBased == true,
                reps = ex.targetReps,
                seconds = ex.targetDurationSeconds,
                activityKcal = ex.estimatedActivityKcal,
                canMoveUp = idx > 0,
                canMoveDown = idx < state.draft.exercises.lastIndex,
                onRepsChange = { viewModel.setReps(ex.exerciseId, it) },
                onDurationChange = { viewModel.setDuration(ex.exerciseId, it) },
                onMoveUp = { viewModel.moveUp(ex.exerciseId) },
                onMoveDown = { viewModel.moveDown(ex.exerciseId) }
            )
            Spacer(Modifier.height(10.dp))
        }

        Spacer(Modifier.height(16.dp))

        // Totals card
        RoundedCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "🔥  ${state.estimatedActivityKcal} Total Calories Burnt",
                    style = MaterialTheme.typography.labelMedium,
                    color = FitHubPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "⏱  ~${state.estimatedDurationMinutes} minutes",
                    style = MaterialTheme.typography.labelMedium,
                    color = FitHubPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (state.errorMessage != null) {
            Spacer(Modifier.height(12.dp))
            Text(state.errorMessage!!, color = ErrorRed)
        }
    }
}

@Composable
private fun ConfigureExerciseRow(
    index: Int,
    exerciseName: String,
    category: String,
    isTimeBased: Boolean,
    reps: Int,
    seconds: Int,
    activityKcal: Double,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onRepsChange: (Int) -> Unit,
    onDurationChange: (Int) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardWhite)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(FitHubPrimary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    index.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextOnPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    exerciseName,
                    style = MaterialTheme.typography.titleSmall,
                    color = FitHubPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Category : $category",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }

            // Reorder controls
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onMoveUp,
                    enabled = canMoveUp,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Filled.KeyboardArrowUp,
                        contentDescription = "Move up",
                        tint = if (canMoveUp) FitHubPrimary else SurfaceGray
                    )
                }
                IconButton(
                    onClick = onMoveDown,
                    enabled = canMoveDown,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Move down",
                        tint = if (canMoveDown) FitHubPrimary else SurfaceGray
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        if (isTimeBased) {
            Text(
                "Duration",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Slider(
                    value = seconds.toFloat(),
                    onValueChange = { onDurationChange(it.toInt()) },
                    valueRange = 15f..300f,
                    steps = 9,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = FitHubPrimary,
                        activeTrackColor = FitHubPrimary
                    )
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "${seconds / 60}m ${seconds % 60}s",
                    style = MaterialTheme.typography.titleSmall,
                    color = FitHubPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Text(
                "Repetitions",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Slider(
                    value = reps.toFloat(),
                    onValueChange = { onRepsChange(it.toInt()) },
                    valueRange = 1f..50f,
                    steps = 48,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = FitHubPrimary,
                        activeTrackColor = FitHubPrimary
                    )
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "$reps reps",
                    style = MaterialTheme.typography.titleSmall,
                    color = FitHubPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        Text(
            "🔥  ~${activityKcal.toInt()} cal",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}