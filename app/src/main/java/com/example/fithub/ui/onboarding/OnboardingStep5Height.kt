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
fun OnboardingStep5Height(
    onCancel: () -> Unit,
    onNext: () -> Unit
) {
    var height by remember { mutableStateOf(OnboardingSession.heightCm?.toInt()?.toString() ?: "") }

    OnboardingScaffold(
        stepNumber = 5,
        title = stringResource(R.string.onboarding_step5_title),
        subtitle = stringResource(R.string.onboarding_step5_subtitle),
        onCancel = onCancel,
        onNext = {
            OnboardingSession.heightCm = height.toDoubleOrNull()
            onNext()
        },
        nextEnabled = height.toIntOrNull()?.let { it in 80..250 } == true
    ) {
        Text(
            stringResource(R.string.onboarding_step5_question),
            style = MaterialTheme.typography.titleSmall,
            color = FitHubPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(14.dp))
        Text(
            stringResource(R.string.onboarding_step5_label),
            style = MaterialTheme.typography.labelMedium,
            color = FitHubPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        FitHubTextField(
            value = height,
            onValueChange = { if (it.all(Char::isDigit)) height = it },
            placeholder = stringResource(R.string.onboarding_step5_placeholder),
            keyboardType = KeyboardType.Number
        )
        Spacer(Modifier.height(28.dp))
        InfoHint()
    }
}