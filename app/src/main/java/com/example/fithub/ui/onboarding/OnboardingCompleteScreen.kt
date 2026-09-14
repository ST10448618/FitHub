package com.example.fithub.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fithub.ui.components.PrimaryButton
import com.example.fithub.ui.theme.*

@Composable
fun OnboardingCompleteScreen(
    onFinish: () -> Unit,
    viewModel: OnboardingViewModel = viewModel()
) {
    val submitState by viewModel.submitState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.submit()
    }

    LaunchedEffect(submitState.isComplete) {
        if (submitState.isComplete) onFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardWhite, MaterialTheme.shapes.extraLarge)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(FitHubPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "SUCCESS!",
                style = MaterialTheme.typography.labelLarge,
                color = SuccessGreen,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Onboarding Completed",
                style = MaterialTheme.typography.headlineMedium,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))

            Text(
                "We have all the startup information we need. You can set your personal goals from the Goals section.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            if (submitState.errorMessage != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    submitState.errorMessage!!,
                    style = MaterialTheme.typography.labelSmall,
                    color = ErrorRed,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(24.dp))

            PrimaryButton(
                text = if (submitState.isSubmitting) "Setting up..." else "Confirm",
                onClick = { if (!submitState.isSubmitting) onFinish() },
                enabled = !submitState.isSubmitting
            )
        }
    }
}