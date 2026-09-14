package com.example.fithub.ui.screens.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fithub.core.OnboardingSession
import com.example.fithub.ui.components.FitHubTextField
import com.example.fithub.ui.theme.FitHubPrimary
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun OnboardingStep5Height(
    onCancel: () -> Unit,
    onNext: () -> Unit
) {
    var height by remember { mutableStateOf(OnboardingSession.heightCm?.toInt()?.toString() ?: "") }

    OnboardingScaffold(
        stepNumber = 5,
        title = "What is your current height?",
        subtitle = "Not the one on your dating profile.",
        onCancel = onCancel,
        onNext = {
            OnboardingSession.heightCm = height.toDoubleOrNull()
            onNext()
        },
        nextEnabled = height.toIntOrNull()?.let { it in 80..250 } == true
    ) {
        Text(
            "How tall are you?",
            style = MaterialTheme.typography.titleSmall,
            color = FitHubPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(14.dp))
        Text(
            "ENTER YOUR CURRENT HEIGHT",
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
            placeholder = "169 cm",
            keyboardType = KeyboardType.Number
        )
        Spacer(Modifier.height(28.dp))
        InfoHint()
    }
}