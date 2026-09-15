package com.example.fithub.ui.screens.goals.checkpoint

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fithub.ui.components.*
import com.example.fithub.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun CheckpointManagerScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: CheckpointManagerViewModel = viewModel()
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
        AppHeader(title = "Checkpoints", onBack = onBack)

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

            // Next weigh-in summary
            RoundedCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Next Weigh In",
                    style = MaterialTheme.typography.titleMedium,
                    color = FitHubPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            state.nextCheckpointDate.dayOfWeek.name.lowercase()
                                .replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Text(
                            state.nextCheckpointDate.format(
                                DateTimeFormatter.ofPattern("d MMMM yyyy")
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            color = FitHubPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Last Recorded weight",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Text(
                            state.lastRecordedWeightKg?.let { "$it kg" } ?: "—",
                            style = MaterialTheme.typography.titleMedium,
                            color = FitHubPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Frequency selector
            RoundedCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Checkpoint Frequency",
                    style = MaterialTheme.typography.titleMedium,
                    color = FitHubPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Manage how often you update your weight.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FrequencyChip(
                        label = "Weekly",
                        days = 7,
                        selected = state.frequencyDays == 7,
                        onClick = { viewModel.onFrequencyChange(7) }
                    )
                    FrequencyChip(
                        label = "Every 2 weeks",
                        days = 14,
                        selected = state.frequencyDays == 14,
                        onClick = { viewModel.onFrequencyChange(14) }
                    )
                    FrequencyChip(
                        label = "Monthly",
                        days = 30,
                        selected = state.frequencyDays == 30,
                        onClick = { viewModel.onFrequencyChange(30) }
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Reminders toggle
            RoundedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Checkpoint Reminders",
                            style = MaterialTheme.typography.titleSmall,
                            color = FitHubPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Get a notification on weigh-in day.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = state.remindersEnabled,
                        onCheckedChange = viewModel::onRemindersToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = FitHubPrimary,
                            checkedTrackColor = FitHubLightBlue
                        )
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Upcoming checkpoints
            RoundedCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Upcoming Checkpoints",
                    style = MaterialTheme.typography.titleMedium,
                    color = FitHubPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.upcomingDates.forEachIndexed { idx, date ->
                        UpcomingDateBox(
                            date = date,
                            highlight = idx == 0,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
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
                    enabled = !state.isSaving,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FrequencyChip(
    label: String,
    days: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                if (selected) FitHubPrimary else CardWhite,
                RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.dp,
                color = if (selected) FitHubPrimary else SurfaceGray,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) TextOnPrimary else FitHubPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun UpcomingDateBox(
    date: LocalDate,
    highlight: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                if (highlight) FitHubLightBlue else CardWhite,
                RoundedCornerShape(12.dp)
            )
            .border(
                1.dp,
                if (highlight) FitHubPrimary else SurfaceGray,
                RoundedCornerShape(12.dp)
            )
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            date.dayOfMonth.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = FitHubPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            date.month.name.take(3).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}