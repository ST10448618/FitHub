package com.example.fithub.ui.screens.plans.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.fithub.core.WorkoutImageMapper
import com.example.fithub.ui.components.*
import com.example.fithub.ui.theme.*

@Composable
fun WorkoutDetailsScreen(
    onBack: () -> Unit,
    onStartSession: (String) -> Unit,
    viewModel: WorkoutDetailsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val plan = state.plan
    if (plan == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(state.errorMessage ?: "Workout not found.")
        }
        return
    }

    val localRes = WorkoutImageMapper.drawableRes(context, plan.id)
    val imageModel: Any? = plan.imageUrl ?: localRes.takeIf { it != 0 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            if (imageModel != null) {
                AsyncImage(
                    model = imageModel,
                    contentDescription = plan.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(FitHubMidBlue, FitHubPrimary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        plan.name.take(1).uppercase(),
                        style = MaterialTheme.typography.displayLarge,
                        color = Color.White.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            IconButton(
                onClick = onBack,
                modifier = Modifier.padding(8.dp)
            ) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (-24).dp)
                .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
                .background(BackgroundGray)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Text(
                plan.name,
                style = MaterialTheme.typography.headlineMedium,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                plan.description.ifBlank { "No description." },
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(Modifier.height(14.dp))

            // Difficulty + equipment chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChipLabel(difficultyLabel(plan.difficulty.name))
                ChipLabel(if (plan.equipment.isBlank()) "No Equipment" else plan.equipment)
            }

            Spacer(Modifier.height(16.dp))

            // Primary action
            if (!plan.isVerified || plan.isSaved) {
                PrimaryButton(
                    text = "▶  Start Plan",
                    onClick = { onStartSession(plan.id) }
                )
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PrimaryButton(
                        text = "▶  Start Plan",
                        onClick = { onStartSession(plan.id) },
                        modifier = Modifier.weight(1f)
                    )
                    SecondaryButton(
                        text = if (state.isSaving) "Saving…" else "Save",
                        onClick = viewModel::savePlan,
                        enabled = !state.isSaving,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Plan details grid
            RoundedCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Plan Details",
                    style = MaterialTheme.typography.titleSmall,
                    color = FitHubPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    DetailCell(
                        icon = { Icon(Icons.Filled.LocalFireDepartment, null, tint = FitHubPrimary) },
                        label = "Calories Burnt",
                        value = "${plan.estimatedActivityKcal} calories",
                        modifier = Modifier.weight(1f)
                    )
                    DetailCell(
                        icon = { Icon(Icons.Filled.Timer, null, tint = FitHubPrimary) },
                        label = "Session Length",
                        value = "${plan.estimatedDurationMinutes} mins",
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    DetailCell(
                        icon = { Icon(Icons.Filled.FitnessCenter, null, tint = FitHubPrimary) },
                        label = "Exercises",
                        value = "${plan.exercises.size} rounds",
                        modifier = Modifier.weight(1f)
                    )
                    DetailCell(
                        icon = { Icon(Icons.Filled.BookmarkBorder, null, tint = FitHubPrimary) },
                        label = "Requirements",
                        value = if (plan.equipment.isBlank()) "None" else plan.equipment,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "Rounds",
                style = MaterialTheme.typography.titleMedium,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(10.dp))

            plan.exercises.sortedBy { it.orderIndex }.forEachIndexed { idx, ex ->
                ExerciseRow(
                    index = idx + 1,
                    name = ex.exerciseName,
                    category = ex.category.name.lowercase()
                        .replaceFirstChar { it.uppercase() },
                    targetReps = ex.targetReps,
                    targetDurationSeconds = ex.targetDurationSeconds
                )
                Spacer(Modifier.height(8.dp))
            }

            if (state.errorMessage != null) {
                Spacer(Modifier.height(12.dp))
                Text(state.errorMessage!!, color = ErrorRed)
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ChipLabel(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(FitHubLightBlue)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            color = FitHubPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DetailCell(
    icon: @Composable () -> Unit,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(FitHubLightBlue),
            contentAlignment = Alignment.Center
        ) { icon() }
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Text(
                value,
                style = MaterialTheme.typography.titleSmall,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ExerciseRow(
    index: Int,
    name: String,
    category: String,
    targetReps: Int,
    targetDurationSeconds: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardWhite)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(FitHubLightBlue),
            contentAlignment = Alignment.Center
        ) {
            Text(
                index.toString(),
                style = MaterialTheme.typography.titleSmall,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                name,
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
        val meta = if (targetDurationSeconds > 0)
            "${targetDurationSeconds / 60} min"
        else
            "$targetReps reps"
        Text(
            meta,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )
    }
}

private fun difficultyLabel(name: String): String =
    name.lowercase().replaceFirstChar { it.uppercase() }