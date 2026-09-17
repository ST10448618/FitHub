package com.example.fithub.ui.screens.session

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fithub.ui.theme.*

@Composable
fun SessionPausedOverlay(
    onResume: () -> Unit,
    onSaveAndExit: () -> Unit,
    onDiscard: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .clip(RoundedCornerShape(24.dp))
                .background(CardWhite)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Close (resume) button top-right
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = onResume,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BackgroundGray)
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Resume",
                        tint = FitHubPrimary
                    )
                }
            }

            Text("⏸", style = MaterialTheme.typography.displayLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                "SESSION PAUSED!",
                style = MaterialTheme.typography.headlineMedium,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Clocking out early? Or just catching your breath?",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(FitHubLightBlue)
            )
            Spacer(Modifier.height(20.dp))

            Text(
                "WHAT ACTION WOULD YOU LIKE TO TAKE?",
                style = MaterialTheme.typography.labelMedium,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Your progress will be saved!",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )

            Spacer(Modifier.height(16.dp))

            PausedActionRow(
                emoji = "💾",
                title = "SAVE PROGRESS & END",
                subtitle = "You can continue this workout at any time.",
                onClick = onSaveAndExit
            )

            Spacer(Modifier.height(12.dp))

            PausedActionRow(
                emoji = "🗑",
                title = "DISCARD SESSION",
                subtitle = "This session's progress will not be recorded.",
                onClick = onDiscard
            )
        }
    }
}

@Composable
private fun PausedActionRow(
    emoji: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(FitHubLightBlue.copy(alpha = 0.4f))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(FitHubPrimary),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.labelLarge,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}