package com.example.fithub.ui.screens.journal.sections

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fithub.R
import com.example.fithub.ui.components.RoundedCard
import com.example.fithub.ui.components.SectionHeader
import com.example.fithub.ui.theme.FitHubPrimary
import com.example.fithub.ui.theme.TextSecondary
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun JournalBodyWeightCard(
    currentWeightKg: Double?,
    targetWeightKg: Double?,
    nextCheckpointDate: LocalDate?,
    onViewDetailsClick: () -> Unit,
    onManageCheckpointsClick: () -> Unit
) {
    val locale = Locale.getDefault()
    val dateFormatter = DateTimeFormatter
        .ofLocalizedDate(FormatStyle.MEDIUM)
        .withLocale(locale)
    val dash = "—"

    RoundedCard(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(
            title = stringResource(R.string.journal_body_title),
            actionText = stringResource(R.string.journal_body_action),
            onAction = onViewDetailsClick
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            InfoColumn(
                label = stringResource(R.string.journal_body_recent_weight),
                value = currentWeightKg?.let {
                    stringResource(R.string.journal_body_kg_upper_format, it.toInt())
                } ?: dash,
                modifier = Modifier.weight(1f)
            )
            InfoColumn(
                label = stringResource(R.string.journal_body_next_checkpoint),
                value = nextCheckpointDate?.format(dateFormatter) ?: dash,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(12.dp))

        Row {
            Text(
                stringResource(R.string.journal_body_goal_label),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Text(
                targetWeightKg?.let {
                    stringResource(R.string.journal_body_kg_upper_format, it.toInt())
                } ?: dash,
                style = MaterialTheme.typography.titleSmall,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun InfoColumn(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Spacer(Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleSmall,
            color = FitHubPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}