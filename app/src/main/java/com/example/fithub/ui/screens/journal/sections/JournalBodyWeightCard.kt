package com.example.fithub.ui.screens.journal.sections

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fithub.ui.components.RoundedCard
import com.example.fithub.ui.components.SectionHeader
import com.example.fithub.ui.theme.FitHubPrimary
import com.example.fithub.ui.theme.TextSecondary
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ============================================================
// OWNER: Track B (Muhammad Akeel)
// ============================================================
@Composable
fun JournalBodyWeightCard(
    currentWeightKg: Double?,
    targetWeightKg: Double?,
    nextCheckpointDate: LocalDate?,
    onViewDetailsClick: () -> Unit,
    onManageCheckpointsClick: () -> Unit
) {
    RoundedCard(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(
            title = "Body & Weight",
            actionText = "view details",
            onAction = onViewDetailsClick
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            InfoColumn(
                label = "Most Recent Weight",
                value = currentWeightKg?.let { "${it.toInt()} KG" } ?: "—",
                modifier = Modifier.weight(1f)
            )
            InfoColumn(
                label = "Next Checkpoint",
                value = nextCheckpointDate?.format(
                    DateTimeFormatter.ofPattern("d MMMM")
                ) ?: "—",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(12.dp))

        Row {
            Text("GOAL: ", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text(
                targetWeightKg?.let { "${it.toInt()} KG" } ?: "—",
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