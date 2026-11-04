package com.example.fithub.ui.screens.plans.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fithub.R
import com.example.fithub.domain.model.WorkoutDifficulty
import com.example.fithub.ui.components.*
import com.example.fithub.ui.theme.*
import com.example.fithub.ui.util.localized

@Composable
fun CreateWorkoutStep1Screen(
    onBack: () -> Unit,
    onNext: () -> Unit,
    viewModel: CreateWorkoutViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CreateWorkoutScaffold(
        stepNumber = 1,
        title = stringResource(R.string.create_step1_title),
        subtitle = stringResource(R.string.create_step1_subtitle),
        onCancel = {
            viewModel.cancelAndReset()
            onBack()
        },
        onNext = onNext,
        nextEnabled = state.draft.name.isNotBlank()
    ) {
        Text(
            stringResource(R.string.create_step1_task_details),
            style = MaterialTheme.typography.titleMedium,
            color = FitHubPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))

        FitHubTextField(
            value = state.draft.name,
            onValueChange = viewModel::updateName,
            label = stringResource(R.string.create_step1_name_label),
            placeholder = stringResource(R.string.create_step1_name_placeholder)
        )

        Spacer(Modifier.height(14.dp))

        FitHubTextField(
            value = state.draft.description,
            onValueChange = viewModel::updateDescription,
            label = stringResource(R.string.create_step1_description_label),
            placeholder = stringResource(R.string.create_step1_description_placeholder),
            singleLine = false
        )

        Spacer(Modifier.height(14.dp))

        // Difficulty dropdown — display labels come from `.localized()`; on
        // select we map back via the enum index rather than a fragile
        // case-insensitive lookup across languages.
        val difficultyEntries = WorkoutDifficulty.entries
        val difficultyLabels = difficultyEntries.map { it.localized() }
        DropdownField(
            label = stringResource(R.string.create_step1_difficulty_label),
            current = state.draft.difficulty.localized(),
            options = difficultyLabels,
            onSelect = { label ->
                val idx = difficultyLabels.indexOf(label)
                if (idx >= 0) viewModel.updateDifficulty(difficultyEntries[idx])
            }
        )

        Spacer(Modifier.height(14.dp))

        // Equipment — display via localised labels; persisted as raw English
        // strings (equipment keys in the DB stay stable).
        val equipmentOptions = listOf(
            "None", "Dumbbells", "Barbell", "Bench", "Resistance Band", "Kettlebell"
        )
        val equipmentLabels = equipmentOptions.map { equipmentDisplayLabel(it) }
        DropdownField(
            label = stringResource(R.string.create_step1_equipment_label),
            current = equipmentDisplayLabel(state.draft.equipment),
            options = equipmentLabels,
            onSelect = { label ->
                val idx = equipmentLabels.indexOf(label)
                if (idx >= 0) viewModel.updateEquipment(equipmentOptions[idx])
            }
        )

        Spacer(Modifier.height(20.dp))

        Text(
            stringResource(R.string.create_step1_additional_info),
            style = MaterialTheme.typography.titleMedium,
            color = FitHubPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(10.dp))

        SecondaryButton(
            text = "📎  " + stringResource(R.string.create_step1_attach),
            onClick = { /* Phase 9: image picker */ }
        )
    }
}

@Composable
private fun equipmentDisplayLabel(raw: String): String = when (raw) {
    "None" -> stringResource(R.string.equipment_none)
    "Dumbbells" -> stringResource(R.string.equipment_dumbbells)
    "Barbell" -> stringResource(R.string.equipment_barbell)
    "Bench" -> stringResource(R.string.equipment_bench)
    "Resistance Band" -> stringResource(R.string.equipment_resistance_band)
    "Kettlebell" -> stringResource(R.string.equipment_kettlebell)
    else -> raw
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
    onNext: (() -> Unit)?,
    nextEnabled: Boolean = true,
    nextButtonText: String? = null,
    content: @Composable () -> Unit
) {
    val resolvedNextText = nextButtonText ?: stringResource(R.string.onboarding_next)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                stringResource(R.string.onboarding_step_counter, stepNumber, totalSteps),
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
                stringResource(R.string.create_title),
                style = MaterialTheme.typography.headlineMedium,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(FitHubSecondary, FitHubMidBlue, FitHubPrimary)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stepNumber.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            color = FitHubPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        title,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
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
                text = stringResource(R.string.generic_cancel),
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            )
            if (onNext != null) {
                PrimaryButton(
                    text = "$resolvedNextText  ▶",
                    onClick = { onNext() },
                    enabled = nextEnabled,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}