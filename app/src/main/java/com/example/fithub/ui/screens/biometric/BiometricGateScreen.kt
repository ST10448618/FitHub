package com.example.fithub.ui.screens.biometric

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fithub.R
import com.example.fithub.core.LogTags
import com.example.fithub.ui.components.FitHubLogo
import com.example.fithub.ui.components.PrimaryButton
import com.example.fithub.ui.components.SecondaryButton
import com.example.fithub.ui.theme.FitHubMidBlue
import com.example.fithub.ui.theme.FitHubPrimary
import com.example.fithub.ui.theme.FitHubSecondary

/**
 * Reference: https://developer.android.com/training/sign-in/biometric-auth
 *            https://developer.android.com/reference/androidx/biometric/BiometricPrompt
 * (accessed February 2026)
 */
@Composable
fun BiometricGateScreen(
    onAuthenticated: () -> Unit,
    onUsePassword: () -> Unit,
    viewModel: BiometricGateViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    LaunchedEffect(Unit) {
        Log.d(LogTags.BIOMETRIC, "BiometricGateScreen composed; evaluating availability")
        when (val availability = checkAvailability(context)) {
            Availability.NoneEnrolled -> viewModel.setError(R.string.biometric_gate_not_enrolled)
            Availability.NoHardware, Availability.Unavailable ->
                viewModel.setError(R.string.biometric_gate_not_available)
        }
    }

    LaunchedEffect(state.authenticated) {
        if (state.authenticated) onAuthenticated()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(FitHubSecondary, FitHubMidBlue, FitHubPrimary))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FitHubLogo(iconSize = 140.dp)
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.biometric_gate_title),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.biometric_gate_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))

            if (state.isSyncing) {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
            }

            state.errorMessageResId?.let { resId ->
                Text(
                    text = stringResource(resId),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(12.dp))
            }

            PrimaryButton(
                text = stringResource(R.string.biometric_gate_prompt),
                onClick = {
                    Log.i(LogTags.BIOMETRIC, "User tapped 'Use fingerprint'")
                },
                enabled = !state.isSyncing
            )
            Spacer(Modifier.height(12.dp))
            SecondaryButton(
                text = stringResource(R.string.biometric_gate_fallback),
                onClick = {
                    viewModel.onUsePassword()
                    onUsePassword()
                }
            )
        }
    }
}

private enum class Availability { Available, NoneEnrolled, NoHardware, Unavailable }

private fun checkAvailability(context: android.content.Context): Availability {
    val manager = BiometricManager.from(context)
    return when (manager.canAuthenticate(
    )) {
        BiometricManager.BIOMETRIC_SUCCESS -> Availability.Available
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> Availability.NoneEnrolled
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> Availability.NoHardware
        else -> Availability.Unavailable
    }
}

private fun launchPrompt(
    activity: FragmentActivity?,
    onSuccess: () -> Unit
) {
    if (activity == null) {
        Log.w(LogTags.BIOMETRIC, "No FragmentActivity host — cannot show BiometricPrompt")
        return
    }
    val executor = ContextCompat.getMainExecutor(activity)
    val prompt = BiometricPrompt(
        activity,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                Log.i(LogTags.BIOMETRIC, "Biometric authentication succeeded")
                onSuccess()
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                Log.w(LogTags.BIOMETRIC, "Biometric auth error code=$errorCode msg=$errString")
            }
            override fun onAuthenticationFailed() {
                Log.w(LogTags.BIOMETRIC, "Biometric authentication failed (finger mismatch)")
            }
        }
    )
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(activity.getString(R.string.biometric_gate_title))
        .setSubtitle(activity.getString(R.string.biometric_gate_subtitle))
        .setNegativeButtonText(activity.getString(R.string.biometric_gate_cancel))
        .setAllowedAuthenticators(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.BIOMETRIC_WEAK
        )
        .build()
    prompt.authenticate(promptInfo)
}