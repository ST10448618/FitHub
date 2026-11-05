// AI-assisted (DeepSeek); reviewed and tested by the FitHub team.
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fithub.R
import com.example.fithub.core.i18n.LocaleManager
import com.example.fithub.ui.charts.DonutProgress
import com.example.fithub.ui.charts.WeightJourneyChart
import com.example.fithub.ui.components.*
import com.example.fithub.ui.theme.*
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun BodyWeightScreen(
    onBack: () -> Unit,
    onLogWeightClick: () -> Unit,
    onManageCheckpointsClick: () -> Unit,
    onOpenProgressGallery: () -> Unit = {},
    viewModel: BodyWeightViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val locale = Locale.forLanguageTag(LocaleManager.currentCode(context))
    val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        AppHeader(title = stringResource(R.string.body_title), onBack = onBack)

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

            RoundedCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    stringResource(R.string.body_weight_data),
                    style = MaterialTheme.typography.titleMedium,
                    color = FitHubPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    DonutProgress(
                        progress = state.progressPercent / 100f,
                        size = 150.dp,
                        strokeWidth = 18.dp,
                        centerValue = stringResource(
                            R.string.dashboard_percent_format,
                            state.progressPercent
                        ),
                        centerLabel = stringResource(R.string.body_achieved)
                    )
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = stringResource(
                        R.string.body_goal_format,
                        state.targetWeightKg?.toInt() ?: stringResource(R.string.common_dash)
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = stringResource(
                        R.string.body_current_weight_format,
                        state.currentWeightKg?.let {
                            stringResource(R.string.journal_body_kg_format, it.toInt())
                        } ?: stringResource(R.string.common_dash)
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    color = FitHubPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                state.deltaFromLastMeasureKg?.let { delta ->
                    val sign = if (delta >= 0) "+" else ""
                    Text(
                        text = stringResource(
                            R.string.body_delta_from_last,
                            "$sign${String.format(locale, "%.1f", delta)}"
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.body_journey_title),
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
                        title = stringResource(R.string.body_not_enough_data_title),
                        message = stringResource(R.string.body_not_enough_data_body),
                        emoji = "📉"
                    )
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.body_journey_note),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                PrimaryButton(
                    text = "⚖  " + stringResource(R.string.body_log_weight_button),
                    onClick = onLogWeightClick
                )
            }

            Spacer(Modifier.height(16.dp))

            // ---- Progress Photos tile (Phase 6 / UDF 4) ----
            if (state.photoCount > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardWhite)
                        .clickable(onClick = onOpenProgressGallery)
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(FitHubLightBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📸", style = MaterialTheme.typography.titleLarge)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.progress_gallery_entry_title),
                                style = MaterialTheme.typography.titleSmall,
                                color = FitHubPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                stringResource(
                                    R.string.progress_gallery_entry_subtitle,
                                    state.photoCount
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Text("›", color = FitHubPrimary, style = MaterialTheme.typography.titleLarge)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // ---- BMI Card ----
            RoundedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.body_bmi_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = FitHubPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        stringResource(R.string.body_bmi_source),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }

                Spacer(Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        state.bmi?.let { String.format(locale, "%.2f", it) }
                            ?: stringResource(R.string.common_dash),
                        style = MaterialTheme.typography.headlineLarge,
                        color = FitHubPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(12.dp))
                    state.bmiLabelRes?.let { BmiBadge(stringResource(it)) }
                }

                Spacer(Modifier.height(16.dp))

                BmiScaleBar(bmi = state.bmi)

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BmiLegendDot(stringResource(R.string.bmi_underweight), WarningOrange)
                    BmiLegendDot(stringResource(R.string.bmi_healthy), SuccessGreen)
                    BmiLegendDot(stringResource(R.string.bmi_overweight), Color(0xFFFFC107))
                    BmiLegendDot(stringResource(R.string.bmi_obese), ErrorRed)
                }
            }

            Spacer(Modifier.height(16.dp))

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
                            stringResource(
                                R.string.body_next_checkpoint_format,
                                state.nextCheckpointDate?.format(dateFormatter)
                                    ?: stringResource(R.string.common_dash)
                            ),
                            style = MaterialTheme.typography.titleSmall,
                            color = FitHubPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            stringResource(R.string.body_manage_checkpoints_cta),
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
    val under = stringResource(R.string.bmi_underweight)
    val healthy = stringResource(R.string.bmi_healthy)
    val overweight = stringResource(R.string.bmi_overweight)
    val obese = stringResource(R.string.bmi_obese)
    val (bg, fg) = when (label) {
        under -> WarningOrange.copy(alpha = 0.15f) to WarningOrange
        healthy -> SuccessGreen.copy(alpha = 0.15f) to SuccessGreen
        overweight -> Color(0xFFFFC107).copy(alpha = 0.15f) to Color(0xFF996A00)
        obese -> ErrorRed.copy(alpha = 0.15f) to ErrorRed
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
                    colors = listOf(WarningOrange, SuccessGreen, Color(0xFFFFC107), ErrorRed)
                )
            )
    ) {
        if (bmi != null) {
            val frac = ((bmi - 15.0) / 25.0).coerceIn(0.0, 1.0).toFloat()
            Box(modifier = Modifier.fillMaxWidth(frac)) {
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
private fun BmiLegendDot(label: String, color: Color) {
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