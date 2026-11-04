package com.example.fithub.ui.screens.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fithub.R
import com.example.fithub.core.OnboardingSession
import com.example.fithub.ui.components.FitHubTextField
import com.example.fithub.ui.theme.FitHubPrimary

@Composable
fun OnboardingStep6Weight(
    onCancel: () -> Unit,
    onNext: () -> Unit
) {
    var weight by remember { mutableStateOf(OnboardingSession.weightKg?.toInt()?.toString() ?: "") }

    OnboardingScaffold(
        stepNumber = 6,
        title = stringResource(R.string.onboarding_step6_title),
        subtitle = stringResource(R.string.onboarding_step6_subtitle),
        onCancel = onCancel,
        onNext = {
            OnboardingSession.weightKg = weight.toDoubleOrNull()
            onNext()
        },
        nextEnabled = weight.toIntOrNull()?.let { it in 20..400 } == true
    ) {
        Text(
            stringResource(R.string.onboarding_step6_question),
            style = MaterialTheme.typography.titleSmall,
            color = FitHubPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(14.dp))
        Text(
            stringResource(R.string.onboarding_step6_label),
            style = MaterialTheme.typography.labelMedium,
            color = FitHubPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        FitHubTextField(
            value = weight,
            onValueChange = { if (it.all(Char::isDigit)) weight = it },
            placeholder = stringResource(R.string.onboarding_step6_placeholder),
            keyboardType = KeyboardType.Number
        )
        Spacer(Modifier.height(28.dp))
        InfoHint()
    }
}