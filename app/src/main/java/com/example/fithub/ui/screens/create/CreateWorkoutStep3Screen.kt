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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fithub.R
import com.example.fithub.domain.model.Exercise
import com.example.fithub.ui.components.RoundedCard
import com.example.fithub.ui.theme.*
import com.example.fithub.ui.util.localized

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
        title = stringResource(R.string.create_step3_title),
        subtitle = stringResource(R.string.create_step3_subtitle),
        onCancel = {
            viewModel.cancelAndReset()
            onBack()
        },
        onNext = onNext,
        nextEnabled = state.draft.exercises.isNotEmpty()
    ) {
        Text(
            stringResource(R.string.create_step2_your_workout),
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
                category = ex.category.localized(),
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

        RoundedCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "🔥  " + stringResource(R.string.create_step3_total_calories, state.estimatedActivityKcal),
                    style = MaterialTheme.typography.labelMedium,
                    color = FitHubPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "⏱  " + stringResource(R.string.create_step3_estimated_duration, state.estimatedDurationMinutes),
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
                    stringResource(R.string.workout_details_category_format, category),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onMoveUp,
                    enabled = canMoveUp,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Filled.KeyboardArrowUp,
                        contentDescription = stringResource(R.string.create_step3_move_up),
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
                        contentDescription = stringResource(R.string.create_step3_move_down),
                        tint = if (canMoveDown) FitHubPrimary else SurfaceGray
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        if (isTimeBased) {
            Text(
                stringResource(R.string.create_step3_duration),
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
                    stringResource(R.string.create_step3_duration_format, seconds / 60, seconds % 60),
                    style = MaterialTheme.typography.titleSmall,
                    color = FitHubPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Text(
                stringResource(R.string.create_step3_repetitions),
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
                    stringResource(R.string.create_step3_reps_format, reps),
                    style = MaterialTheme.typography.titleSmall,
                    color = FitHubPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        Text(
            "🔥  ~" + stringResource(R.string.create_step3_calories_format, activityKcal.toInt()),
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}