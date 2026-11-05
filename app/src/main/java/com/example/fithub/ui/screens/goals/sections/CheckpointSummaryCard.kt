package com.example.fithub.ui.screens.goals.sections

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource          // ← ADDED
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fithub.R                            // ← ADDED
import com.example.fithub.ui.components.RoundedCard
import com.example.fithub.ui.components.SmallActionButton
import com.example.fithub.ui.theme.FitHubPrimary
import com.example.fithub.ui.theme.TextSecondary
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun CheckpointSummaryCard(
    nextCheckpointDate: LocalDate?,
    onManageClick: () -> Unit
) {
    RoundedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.checkpoint_card_title),
                style = MaterialTheme.typography.titleMedium,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            SmallActionButton(text = stringResource(R.string.common_manage_arrow), onClick = onManageClick)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(
                R.string.checkpoint_card_next_scheduled,
                nextCheckpointDate?.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
                    ?: stringResource(R.string.common_dash)
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            stringResource(
                R.string.checkpoint_card_frequency_format,
                stringResource(R.string.checkpoint_frequency_biweekly)
            ),
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}