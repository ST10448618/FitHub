package com.example.fithub.ui.screens.body

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fithub.ui.charts.DonutProgress
import com.example.fithub.ui.charts.WeightJourneyChart
import com.example.fithub.ui.components.*
import com.example.fithub.ui.theme.*
import java.time.format.DateTimeFormatter

@Composable
fun BodyWeightScreen(
    onBack: () -> Unit,
    onLogWeightClick: () -> Unit,
    onManageCheckpointsClick: () -> Unit,
    viewModel: BodyWeightViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        AppHeader(title = "Body & Weight", onBack = onBack)

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
            Spacer(Modifier.height(4.dp))

            // ---- Weight Data Card ----
            RoundedCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Weight Data",
                    style = MaterialTheme.typography.titleMedium,
                    color = FitHubPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(12.dp))

                // Donut progress toward goal
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    DonutProgress(
                        progress = state.progressPercent / 100f,
                        size = 150.dp,
                        strokeWidth = 18.dp,
                        centerValue = "${state.progressPercent}%",
                        centerLabel = "Achieved"
                    )
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "GOAL: ${state.targetWeightKg?.toInt() ?: "—"} kg",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Current Weight : ${state.currentWeightKg?.let { "${it.toInt()} kg" } ?: "—"}",
                    style = MaterialTheme.typography.titleLarge,
                    color = FitHubPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                if (state.deltaFromLastMeasureKg != null) {
                    val delta = state.deltaFromLastMeasureKg!!
                    val sign = if (delta >= 0) "+" else ""
                    Text(
                        text = "$sign${String.format("%.1f", delta)} kg from last measure",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Journey chart
                Text(
                    text = "Your Journey So Far",
                    style = MaterialTheme.typography.titleSmall,
                    color = FitHubPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))

                if (state.journey.size >= 2) {
                    WeightJourneyChart(
                        values = state.journey.map { it.weightKg },
                        goalKg = state.targetWeightKg,
                        chartHeight = 180.dp
                    )
                } else {
                    EmptyState(
                        title = "Not enough data yet",
                        message = "Log at least two weigh-ins to see your journey.",
                        emoji = "📉"
                    )
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Every point shows one of your recorded weigh-ins.",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                PrimaryButton(
                    text = "⚖  Log Current Weight",
                    onClick = onLogWeightClick
                )
            }

            Spacer(Modifier.height(16.dp))

            // ---- BMI Card ----
            RoundedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Your BMI",
                        style = MaterialTheme.typography.titleMedium,
                        color = FitHubPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "Based on your last weight in",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }

                Spacer(Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        state.bmi?.let { String.format("%.2f", it) } ?: "—",
                        style = MaterialTheme.typography.headlineLarge,
                        color = FitHubPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(12.dp))
                    BmiBadge(state.bmiLabel)
                }

                Spacer(Modifier.height(16.dp))

                BmiScaleBar(bmi = state.bmi)

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BmiLegendDot("Under weight", WarningOrange)
                    BmiLegendDot("Healthy", SuccessGreen)
                    BmiLegendDot("Overweight", Color(0xFFFFC107))
                    BmiLegendDot("Obese", ErrorRed)
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---- Next Checkpoint Banner ----
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(FitHubLightBlue)
                    .clickable(onClick = onManageCheckpointsClick)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(FitHubPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("i", color = TextOnPrimary, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Next Weight In Checkpoint : " +
                                    (state.nextCheckpointDate?.format(
                                        DateTimeFormatter.ofPattern("d MMMM yyyy")
                                    ) ?: "—"),
                            style = MaterialTheme.typography.titleSmall,
                            color = FitHubPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Click Here to Manage Checkpoints",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun BmiBadge(label: String) {
    val (bg, fg) = when (label) {
        "Under weight" -> WarningOrange.copy(alpha = 0.15f) to WarningOrange
        "Healthy" -> SuccessGreen.copy(alpha = 0.15f) to SuccessGreen
        "Overweight" -> Color(0xFFFFC107).copy(alpha = 0.15f) to Color(0xFF996A00)
        "Obese" -> ErrorRed.copy(alpha = 0.15f) to ErrorRed
        else -> SurfaceGray to TextSecondary
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = fg,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun BmiScaleBar(bmi: Double?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        WarningOrange,
                        SuccessGreen,
                        Color(0xFFFFC107),
                        ErrorRed
                    )
                )
            )
    ) {
        if (bmi != null) {
            // Position marker on the scale (very rough mapping: 15..40 BMI)
            val frac = ((bmi - 15.0) / 25.0).coerceIn(0.0, 1.0).toFloat()
            Box(
                modifier = Modifier
                    .fillMaxWidth(frac)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(FitHubPrimary)
                )
            }
        }
    }
}

@Composable
private fun BmiLegendDot(label: String, color: androidx.compose.ui.graphics.Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}