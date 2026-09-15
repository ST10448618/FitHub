package com.example.fithub.ui.screens.goals.weight

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fithub.domain.calculator.GoalDirectionCalculator
import com.example.fithub.domain.model.GoalDirection
import com.example.fithub.ui.components.*
import com.example.fithub.ui.theme.*

@Composable
fun WeightGoalScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: WeightGoalViewModel = viewModel()
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
        AppHeader(title = "Weight Goal", onBack = onBack)

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

            // Gradient hero
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(FitHubSecondary, FitHubMidBlue, FitHubPrimary)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎯", style = MaterialTheme.typography.headlineLarge)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Your journey begins with two numbers.",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Manage your current weight logged by the system as well as your desired target.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Current weight (read-only — comes from checkpoints)
            Text(
                "Current Weight",
                style = MaterialTheme.typography.titleSmall,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))

            if (state.currentWeightKg == null) {
                InfoBanner(backgroundColor = WarningOrange.copy(alpha = 0.15f)) {
                    Text(
                        "Log a weigh-in from Body & Weight before setting a goal.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = WarningOrange
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardWhite, RoundedCornerShape(28.dp))
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${state.currentWeightKg} kg",
                        style = MaterialTheme.typography.headlineMedium,
                        color = FitHubPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "Tracked from your latest weigh-in",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(24.dp))

            // Target weight (editable)
            Text(
                "Set Your Target Weight",
                style = MaterialTheme.typography.titleSmall,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))

            NumberStepper(
                value = state.targetWeightKg,
                onValueChange = viewModel::onTargetChange,
                step = 1,
                min = 20,
                max = 400,
                unit = "kg",
                enabled = state.currentWeightKg != null
            )

            Spacer(Modifier.height(20.dp))

            // Direction banner
            if (state.currentWeightKg != null) {
                DirectionBanner(
                    direction = state.direction,
                    differenceKg = state.differenceKg
                )
            }

            if (state.errorMessage != null) {
                Spacer(Modifier.height(12.dp))
                Text(state.errorMessage!!, color = ErrorRed)
            }

            Spacer(Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SecondaryButton(
                    text = "Cancel",
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                )
                PrimaryButton(
                    text = if (state.isSaving) "Saving..." else "Confirm Goals",
                    onClick = viewModel::save,
                    enabled = !state.isSaving && state.currentWeightKg != null,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DirectionBanner(direction: GoalDirection, differenceKg: Int) {
    val (bg, fg, label, message) = when (direction) {
        GoalDirection.LOSE -> Quad(
            FitHubLightBlue,
            FitHubPrimary,
            "GOAL DETECTED : LOSE WEIGHT",
            "You want to lose ${kotlin.math.abs(differenceKg)} kg"
        )
        GoalDirection.GAIN -> Quad(
            FitHubLightBlue,
            FitHubPrimary,
            "GOAL DETECTED : GAIN WEIGHT",
            "You want to gain $differenceKg kg"
        )
        GoalDirection.MAINTAIN -> Quad(
            FitHubLightBlue,
            FitHubPrimary,
            "GOAL DETECTED : MAINTAIN WEIGHT",
            "You want to stay around your current weight"
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("ⓘ", style = MaterialTheme.typography.titleLarge, color = fg)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    color = fg,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    message,
                    style = MaterialTheme.typography.bodySmall,
                    color = fg
                )
            }
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)