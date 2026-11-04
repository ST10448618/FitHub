package com.example.fithub.ui.screens.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fithub.R
import com.example.fithub.core.OnboardingSession
import com.example.fithub.ui.components.PrimaryButton
import com.example.fithub.ui.components.RoundedCard
import com.example.fithub.ui.theme.FitHubPrimary
import com.example.fithub.ui.theme.TextSecondary

@Composable
fun OnboardingStep2Biometric(
    onCancel: () -> Unit,
    onNext: () -> Unit
) {
    var enabled by remember { mutableStateOf(OnboardingSession.biometricEnabled) }

    OnboardingScaffold(
        stepNumber = 2,
        title = stringResource(R.string.onboarding_step2_title),
        subtitle = stringResource(R.string.onboarding_step2_subtitle),
        onCancel = onCancel,
        onNext = {
            OnboardingSession.biometricEnabled = enabled
            onNext()
        }
    ) {
        RoundedCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                stringResource(R.string.onboarding_step2_card_title),
                style = MaterialTheme.typography.titleMedium,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.onboarding_step2_card_body),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(20.dp))
            PrimaryButton(
                text = stringResource(
                    if (enabled) R.string.onboarding_step2_btn_enabled
                    else R.string.onboarding_step2_btn_enable
                ),
                onClick = {
                    enabled = true
                    // TODO(Track A): prompt BiometricPrompt when API available
                },
                fullWidth = false
            )
        }
    }
}