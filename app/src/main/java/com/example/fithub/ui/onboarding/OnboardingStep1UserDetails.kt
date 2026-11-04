package com.example.fithub.ui.screens.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.fithub.R
import com.example.fithub.core.OnboardingSession
import com.example.fithub.ui.components.FitHubPasswordField
import com.example.fithub.ui.components.FitHubTextField

@Composable
fun OnboardingStep1UserDetails(
    onCancel: () -> Unit,
    onNext: () -> Unit
) {
    val isGoogle = OnboardingSession.isGoogleUser
    var username by remember {
        mutableStateOf(OnboardingSession.username.ifBlank { OnboardingSession.googleDisplayName.orEmpty() })
    }
    var email by remember { mutableStateOf(OnboardingSession.email) }
    var password by remember { mutableStateOf(OnboardingSession.password) }

    val valid = username.isNotBlank() &&
            email.contains("@") &&
            (isGoogle || password.length >= 6)

    val displayFallback = stringResource(R.string.onboarding_username_fallback)
    OnboardingScaffold(
        stepNumber = 1,
        title = stringResource(
            R.string.onboarding_google_welcome_title,
            username.ifBlank { displayFallback }
        ),
        subtitle = if (isGoogle) {
            stringResource(R.string.onboarding_google_welcome_body)
        } else {
            stringResource(R.string.onboarding_step1_subtitle)
        },
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
            label = stringResource(R.string.onboarding_username_label),
            placeholder = stringResource(R.string.onboarding_username_placeholder)
        )
        Spacer(Modifier.height(14.dp))
        FitHubTextField(
            value = email,
            onValueChange = { email = it },
            label = stringResource(R.string.onboarding_email_label),
            placeholder = stringResource(R.string.onboarding_email_placeholder),
            keyboardType = KeyboardType.Email
        )
        if (!isGoogle) {
            Spacer(Modifier.height(14.dp))
            FitHubPasswordField(
                value = password,
                onValueChange = { password = it }
            )
        }
    }
}