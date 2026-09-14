package com.example.fithub.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fithub.ui.components.PrimaryButton
import com.example.fithub.ui.components.RoundedProgressBar
import com.example.fithub.ui.components.SecondaryButton
import com.example.fithub.ui.theme.*

@Composable
fun OnboardingScaffold(
    stepNumber: Int,
    totalSteps: Int = 7,
    title: String,
    subtitle: String,
    onCancel: () -> Unit,
    onNext: (() -> Unit)?,
    nextButtonText: String = "Next Step",
    nextEnabled: Boolean = true,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        // Progress bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                "STEP $stepNumber OF $totalSteps",
                style = MaterialTheme.typography.labelSmall,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            RoundedProgressBar(
                progress = stepNumber.toFloat() / totalSteps.toFloat(),
                height = 6.dp,
                gradient = listOf(FitHubMidBlue, FitHubPrimary)
            )
        }

        // Body
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(12.dp))

            Text(
                "Onboarding",
                style = MaterialTheme.typography.headlineMedium,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            // Gradient hero card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(FitHubSecondary, FitHubMidBlue, FitHubPrimary)
                        )
                    )
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "$stepNumber",
                        style = MaterialTheme.typography.titleLarge,
                        color = FitHubPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(24.dp))

            content()

            Spacer(Modifier.height(24.dp))
        }

        // Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SecondaryButton(
                text = "Cancel",
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            )
            if (onNext != null) {
                PrimaryButton(
                    text = "$nextButtonText  ▶",
                    onClick = onNext,
                    enabled = nextEnabled,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}