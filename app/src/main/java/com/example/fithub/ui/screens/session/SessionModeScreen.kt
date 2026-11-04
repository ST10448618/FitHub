package com.example.fithub.ui.screens.session

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fithub.R
import com.example.fithub.ui.components.LoadingState
import com.example.fithub.ui.theme.*
import com.example.fithub.ui.util.localized
import kotlinx.coroutines.delay

@Composable
fun SessionModeScreen(
    onNavigateToCompleted: (String) -> Unit,
    onExit: () -> Unit,
    viewModel: SessionModeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isPaused, state.isLoading, state.isCompleting) {
        if (!state.isPaused && !state.isLoading && !state.isCompleting) {
            while (true) {
                delay(1000)
                viewModel.onTimerTick()
            }
        }
    }

    LaunchedEffect(state.navigateToCompletedSessionId) {
        state.navigateToCompletedSessionId?.let(onNavigateToCompleted)
    }
    LaunchedEffect(state.navigateBack) {
        if (state.navigateBack) onExit()
    }

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { LoadingState() }
        return
    }

    val plan = state.plan
    if (plan == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                state.errorMessage ?: stringResource(R.string.session_not_found),
                color = TextSecondary
            )
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundGray)) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(
                    R.string.session_exercise_progress_format,
                    state.currentIndex + 1,
                    plan.exercises.size
                ),
                style = MaterialTheme.typography.labelMedium,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                stringResource(R.string.session_mode_title),
                style = MaterialTheme.typography.headlineMedium,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(20.dp))

            TimerDonut(
                secondsRemaining = state.secondsRemaining,
                totalSeconds = state.totalSecondsForCurrent
            )

            Spacer(Modifier.height(28.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
                    .background(CardWhite)
                    .padding(20.dp)
            ) {
                Text(
                    plan.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = FitHubPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                state.currentExercise?.let { ex ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(FitHubLightBlue.copy(alpha = 0.4f))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(FitHubPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                (state.currentIndex + 1).toString(),
                                color = TextOnPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                ex.exerciseName,
                                style = MaterialTheme.typography.titleSmall,
                                color = FitHubPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                stringResource(
                                    R.string.workout_details_category_format,
                                    ex.category.localized()
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatPill(
                            emoji = "🔄",
                            label = stringResource(R.string.session_number_of_reps),
                            value = if (state.currentIsTimeBased)
                                "—"
                            else
                                stringResource(R.string.create_step3_reps_format, ex.targetReps),
                            modifier = Modifier.weight(1f)
                        )
                        StatPill(
                            emoji = "🔥",
                            label = stringResource(R.string.session_calories_burnt),
                            value = stringResource(
                                R.string.create_step3_calories_format,
                                ex.estimatedActivityKcal.toInt()
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircleAction(
                        icon = { Icon(Icons.Filled.KeyboardArrowLeft, null, tint = TextOnPrimary) },
                        label = stringResource(R.string.session_previous),
                        onClick = viewModel::previous,
                        enabled = state.currentIndex > 0
                    )
                    CircleAction(
                        icon = { Icon(Icons.Filled.Pause, null, tint = TextOnPrimary) },
                        label = stringResource(R.string.session_pause),
                        onClick = viewModel::pause,
                        primary = true
                    )
                    CircleAction(
                        icon = { Icon(Icons.Filled.KeyboardArrowRight, null, tint = TextOnPrimary) },
                        label = stringResource(R.string.session_next),
                        onClick = viewModel::next
                    )
                }

                if (state.errorMessage != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        state.errorMessage!!,
                        color = ErrorRed,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        if (state.isPaused) {
            SessionPausedOverlay(
                onResume = viewModel::resume,
                onSaveAndExit = viewModel::saveAndExit,
                onDiscard = viewModel::discardAndExit
            )
        }
    }
}

@Composable
private fun TimerDonut(secondsRemaining: Int, totalSeconds: Int) {
    val progress = if (totalSeconds > 0)
        (secondsRemaining.toFloat() / totalSeconds).coerceIn(0f, 1f)
    else 0f

    Box(modifier = Modifier.size(240.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 22.dp.toPx()
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(stroke / 2, stroke / 2)

            drawArc(
                color = SurfaceGray.copy(alpha = 0.4f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            drawArc(
                brush = Brush.sweepGradient(
                    listOf(FitHubSecondary, FitHubMidBlue, FitHubPrimary, FitHubSecondary)
                ),
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val mins = secondsRemaining / 60
            val secs = secondsRemaining % 60
            Text(
                "%02d:%02d".format(mins, secs),
                style = MaterialTheme.typography.displayLarge,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                stringResource(R.string.session_remaining),
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun StatPill(
    emoji: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(BackgroundGray)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.width(8.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text(value, style = MaterialTheme.typography.titleSmall, color = FitHubPrimary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CircleAction(
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit,
    primary: Boolean = false,
    enabled: Boolean = true
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .size(if (primary) 68.dp else 58.dp)
                .clip(CircleShape)
                .background(
                    when {
                        !enabled -> SurfaceGray
                        primary -> FitHubPrimary
                        else -> FitHubMidBlue
                    }
                )
        ) { icon() }
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}