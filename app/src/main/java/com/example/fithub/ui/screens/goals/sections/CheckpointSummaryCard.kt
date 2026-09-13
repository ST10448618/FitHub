package com.example.fithub.ui.screens.goals.sections

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fithub.ui.components.RoundedCard
import com.example.fithub.ui.components.SmallActionButton
import com.example.fithub.ui.theme.FitHubPrimary
import com.example.fithub.ui.theme.TextSecondary
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ============================================================
// OWNER: Track B (Muhammad Akeel)
// ============================================================
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
                "Weigh In Checkpoints",
                style = MaterialTheme.typography.titleMedium,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            SmallActionButton(text = "Manage →", onClick = onManageClick)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Next Weigh In Scheduled For : " +
                    (nextCheckpointDate?.format(
                        DateTimeFormatter.ofPattern("d MMMM yyyy")
                    ) ?: "—"),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Checkpoint Frequency : every 2 weeks",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}