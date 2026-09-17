package com.example.fithub.ui.screens.plans.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fithub.domain.model.WorkoutDifficulty
import com.example.fithub.ui.components.*
import com.example.fithub.ui.theme.*

@Composable
fun CreateWorkoutStep1Screen(
    onBack: () -> Unit,
    onNext: () -> Unit,
    viewModel: CreateWorkoutViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CreateWorkoutScaffold(
        stepNumber = 1,
        title = "Enter Name & Details",
        subtitle = "Tell us all about your masterpiece!",
        onCancel = {
            viewModel.cancelAndReset()
            onBack()
        },
        onNext = onNext,
        nextEnabled = state.draft.name.isNotBlank()
    ) {
        Text(
            "Task Details",
            style = MaterialTheme.typography.titleMedium,
            color = FitHubPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))

        FitHubTextField(
            value = state.draft.name,
            onValueChange = viewModel::updateName,
            label = "Workout Name",
            placeholder = "Enter Workout Name"
        )

        Spacer(Modifier.height(14.dp))

        FitHubTextField(
            value = state.draft.description,
            onValueChange = viewModel::updateDescription,
            label = "Description",
            placeholder = "Enter Description (optional)",
            singleLine = false
        )

        Spacer(Modifier.height(14.dp))

        DropdownField(
            label = "Difficulty Level",
            current = state.draft.difficulty.name.lowercase()
                .replaceFirstChar { it.uppercase() },
            options = WorkoutDifficulty.entries.map {
                it.name.lowercase().replaceFirstChar { c -> c.uppercase() }
            },
            onSelect = { label ->
                val diff = WorkoutDifficulty.entries.firstOrNull {
                    it.name.equals(label, ignoreCase = true)
                } ?: WorkoutDifficulty.BEGINNER
                viewModel.updateDifficulty(diff)
            }
        )

        Spacer(Modifier.height(14.dp))

        DropdownField(
            label = "Equipment Required",
            current = state.draft.equipment,
            options = listOf("None", "Dumbbells", "Barbell", "Bench", "Resistance Band", "Kettlebell"),
            onSelect = viewModel::updateEquipment
        )

        Spacer(Modifier.height(20.dp))

        Text(
            "Additional Info",
            style = MaterialTheme.typography.titleMedium,
            color = FitHubPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(10.dp))

        SecondaryButton(
            text = "📎  Attach File",
            onClick = { /* Phase 9: image picker */ }
        )
    }
}

/**
 * Common scaffold for the 4 create-workout steps.
 * Matches the visual pattern of OnboardingScaffold.
 */
@Composable
fun CreateWorkoutScaffold(
    stepNumber: Int,
    totalSteps: Int = 4,
    title: String,
    subtitle: String,
    onCancel: () -> Unit,
    onNext: () -> Unit?,
    nextEnabled: Boolean = true,
    nextButtonText: String = "Next Step",
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        // Progress bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                "STEP $stepNumber OF $totalSteps",
                style = MaterialTheme.typography.labelSmall,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            RoundedProgressBar(
                progress = stepNumber.toFloat() / totalSteps.toFloat(),
                height = 6.dp,
                gradient = listOf(FitHubMidBlue, FitHubPrimary)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Text(
                "Create Workout",
                style = MaterialTheme.typography.headlineMedium,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            // Gradient hero
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                            listOf(FitHubSecondary, FitHubMidBlue, FitHubPrimary)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(20.dp),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Column(
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                androidx.compose.ui.graphics.Color.White,
                                androidx.compose.foundation.shape.CircleShape
                            ),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Text(
                            "$stepNumber",
                            style = MaterialTheme.typography.titleLarge,
                            color = FitHubPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        title,
                        style = MaterialTheme.typography.titleLarge,
                        color = androidx.compose.ui.graphics.Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            content()

            Spacer(Modifier.height(24.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SecondaryButton(
                text = "Cancel",
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            )
            if (onNext != null) {
                PrimaryButton(
                    text = "$nextButtonText  ▶",
                    onClick = { onNext() },
                    enabled = nextEnabled,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}