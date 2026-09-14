package com.example.fithub.ui.screens.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fithub.core.OnboardingSession
import com.example.fithub.ui.components.FitHubTextField
import com.example.fithub.ui.theme.FitHubPrimary
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun OnboardingStep3Age(
    onCancel: () -> Unit,
    onNext: () -> Unit
) {
    var age by remember { mutableStateOf(OnboardingSession.age?.toString() ?: "") }

    OnboardingScaffold(
        stepNumber = 3,
        title = "Enter your age",
        subtitle = "They say 30 is the new 20. But we still need your actual age though.",
        onCancel = onCancel,
        onNext = {
            OnboardingSession.age = age.toIntOrNull()
            onNext()
        },
        nextEnabled = age.toIntOrNull()?.let { it in 1..120 } == true
    ) {
        Text(
            "ENTER YOUR CURRENT AGE",
            style = MaterialTheme.typography.labelMedium,
            color = FitHubPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(10.dp))
        FitHubTextField(
            value = age,
            onValueChange = { if (it.all(Char::isDigit)) age = it },
            placeholder = "20",
            keyboardType = KeyboardType.Number
        )
        Spacer(Modifier.height(28.dp))
        InfoHint()
    }
}

@Composable
internal fun InfoHint() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text("ⓘ", style = MaterialTheme.typography.titleLarge, color = FitHubPrimary)
        Spacer(Modifier.height(6.dp))
        Text(
            "This information is needed for us to personalize your experience.",
            style = MaterialTheme.typography.bodyMedium,
            color = FitHubPrimary,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}