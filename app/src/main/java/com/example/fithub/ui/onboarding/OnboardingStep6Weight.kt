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
fun OnboardingStep6Weight(
    onCancel: () -> Unit,
    onNext: () -> Unit
) {
    var weight by remember { mutableStateOf(OnboardingSession.weightKg?.toInt()?.toString() ?: "") }

    OnboardingScaffold(
        stepNumber = 6,
        title = "Enter your current weight",
        subtitle = "Everyone loves a Before and After picture after all!",
        onCancel = onCancel,
        onNext = {
            OnboardingSession.weightKg = weight.toDoubleOrNull()
            onNext()
        },
        nextEnabled = weight.toIntOrNull()?.let { it in 20..400 } == true
    ) {
        Text(
            "How much do you currently weigh?",
            style = MaterialTheme.typography.titleSmall,
            color = FitHubPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(14.dp))
        Text(
            "ENTER YOUR CURRENT WEIGHT",
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
            placeholder = "60 kg",
            keyboardType = KeyboardType.Number
        )
        Spacer(Modifier.height(28.dp))
        InfoHint()
    }
}