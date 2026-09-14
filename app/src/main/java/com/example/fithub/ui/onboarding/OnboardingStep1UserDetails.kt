package com.example.fithub.ui.screens.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.fithub.core.OnboardingSession
import com.example.fithub.ui.components.FitHubPasswordField
import com.example.fithub.ui.components.FitHubTextField

@Composable
fun OnboardingStep1UserDetails(
    onCancel: () -> Unit,
    onNext: () -> Unit
) {
    var username by remember { mutableStateOf(OnboardingSession.username) }
    var email by remember { mutableStateOf(OnboardingSession.email) }
    var password by remember { mutableStateOf(OnboardingSession.password) }

    val valid = username.isNotBlank() &&
            email.contains("@") &&
            password.length >= 6

    OnboardingScaffold(
        stepNumber = 1,
        title = "What should we call you?",
        subtitle = "Enter your Username and Email, as well as set up your password.",
        onCancel = onCancel,
        onNext = {
            OnboardingSession.username = username.trim()
            OnboardingSession.email = email.trim()
            OnboardingSession.password = password
            onNext()
        },
        nextEnabled = valid
    ) {
        FitHubTextField(
            value = username,
            onValueChange = { username = it },
            label = "USERNAME",
            placeholder = "Enter your Username"
        )
        Spacer(Modifier.height(14.dp))
        FitHubTextField(
            value = email,
            onValueChange = { email = it },
            label = "EMAIL ADDRESS",
            placeholder = "Enter your Email Address",
            keyboardType = KeyboardType.Email
        )
        Spacer(Modifier.height(14.dp))
        FitHubPasswordField(
            value = password,
            onValueChange = { password = it }
        )
    }
}