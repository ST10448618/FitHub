package com.example.fithub.ui.screens.body

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
import com.example.fithub.ui.components.*
import com.example.fithub.ui.theme.*

@Composable
fun CheckpointLoggerScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: CheckpointLoggerViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.saved) {
        if (state.saved) onSaved()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        AppHeader(title = "New Weigh In", onBack = onBack)

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

            // Hero banner
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
                    Text(
                        "◎",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Consistency brings results",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Every weigh in is a part of your fitness journey",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "Current Weight",
                style = MaterialTheme.typography.titleSmall,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(10.dp))

            NumberStepper(
                value = state.weight,
                onValueChange = viewModel::onWeightChange,
                step = 1,
                min = 20,
                max = 400,
                unit = state.unit
            )

            Spacer(Modifier.height(16.dp))

            // Progress banner
            viewModel.progressMessage()?.let { message ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(FitHubLightBlue, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "ⓘ",
                            style = MaterialTheme.typography.titleLarge,
                            color = FitHubPrimary
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "PROGRESS DETECTED!",
                                style = MaterialTheme.typography.labelMedium,
                                color = FitHubPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                message,
                                style = MaterialTheme.typography.bodySmall,
                                color = FitHubPrimary
                            )
                        }
                    }
                }
            }

            if (state.errorMessage != null) {
                Spacer(Modifier.height(12.dp))
                Text(state.errorMessage!!, color = ErrorRed)
            }

            Spacer(Modifier.height(28.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SecondaryButton(
                    text = "Cancel",
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                )
                PrimaryButton(
                    text = if (state.isSaving) "Logging..." else "Log Weight",
                    onClick = viewModel::save,
                    enabled = !state.isSaving,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}