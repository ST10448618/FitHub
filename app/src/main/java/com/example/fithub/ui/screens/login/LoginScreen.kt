package com.example.fithub.ui.screens.login

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fithub.R
import com.example.fithub.core.BiometricAvailability
import com.example.fithub.core.BiometricHelper
import com.example.fithub.core.BiometricResult
import com.example.fithub.core.LogTags
import com.example.fithub.core.ServiceLocator
import com.example.fithub.ui.components.FitHubLogo
import com.example.fithub.ui.components.FitHubPasswordField
import com.example.fithub.ui.components.FitHubTextField
import com.example.fithub.ui.components.PrimaryButton
import com.example.fithub.ui.components.SecondaryButton
import com.example.fithub.ui.screens.auth.GoogleSignInOutcome
import com.example.fithub.ui.screens.auth.GoogleSignInViewModel
import com.example.fithub.ui.theme.*
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onBack: () -> Unit,
    onGoogleNeedsOnboarding: () -> Unit = {},
    viewModel: LoginViewModel = viewModel(),
    googleViewModel: GoogleSignInViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val googleState by googleViewModel.uiState.collectAsState()

    val lockEnabled: Boolean = androidx.compose.runtime.remember {
        ServiceLocator.preferencesManager.isBiometricLockEnabledSync()
    }
    val cachedUser: Boolean = androidx.compose.runtime.remember {
        FirebaseAuth.getInstance().currentUser != null
    }

    LaunchedEffect(state.successUserId) {
        if (state.successUserId != null) onLoginSuccess()
    }

    LaunchedEffect(googleState.outcome) {
        when (googleState.outcome) {
            GoogleSignInOutcome.ExistingUser -> {
                googleViewModel.consumeOutcome()
                onLoginSuccess()
            }
            GoogleSignInOutcome.NeedsOnboarding -> {
                googleViewModel.consumeOutcome()
                onGoogleNeedsOnboarding()
            }
            null -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(FitHubSecondary, FitHubMidBlue, FitHubPrimary)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FitHubLogo(iconSize = 160.dp)

            Spacer(Modifier.height(32.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .padding(20.dp)
            ) {
                Text(
                    text = stringResource(R.string.login_welcome_back),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(Modifier.height(16.dp))

                FitHubTextField(
                    value = state.identifier,
                    onValueChange = viewModel::onIdentifierChange,
                    label = stringResource(R.string.login_email_label),
                    placeholder = stringResource(R.string.login_email_placeholder),
                    isError = state.errorMessage != null
                )

                Spacer(Modifier.height(12.dp))

                FitHubPasswordField(
                    value = state.password,
                    onValueChange = viewModel::onPasswordChange,
                    isError = state.errorMessage != null
                )

                state.errorMessage?.let { msg ->
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFFCDD2)
                    )
                }
                googleState.error?.let { msg ->
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFFCDD2)
                    )
                }

                Spacer(Modifier.height(20.dp))

                PrimaryButton(
                    text = if (state.isLoading) {
                        stringResource(R.string.login_button_loading)
                    } else {
                        stringResource(R.string.login_button)
                    },
                    onClick = viewModel::submit,
                    enabled = !state.isLoading && !googleState.isWorking
                )
            }

            Spacer(Modifier.height(20.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.4f))
                Text(
                    "  " + stringResource(R.string.auth_or_divider) + "  ",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.4f))
            }

            Spacer(Modifier.height(20.dp))

            SecondaryButton(
                text = if (googleState.isWorking) {
                    stringResource(R.string.auth_google_progress)
                } else {
                    "G   " + stringResource(R.string.auth_continue_with_google)
                },
                onClick = {
                    Log.i(LogTags.AUTH, "LoginScreen: Continue with Google tapped")
                    googleViewModel.start(context)
                },
                enabled = !googleState.isWorking && !state.isLoading,
                borderColor = Color.White,
                contentColor = Color.White
            )

            if (lockEnabled && cachedUser) {
                Spacer(Modifier.height(20.dp))
                Text(
                    text = stringResource(R.string.login_fingerprint_section_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                        .clickable {
                            val activity = context as? FragmentActivity ?: return@clickable
                            Log.i(LogTags.BIOMETRIC, "LoginScreen: fingerprint tapped")
                            when (BiometricHelper.canAuthenticate(context)) {
                                BiometricAvailability.Available -> {
                                    BiometricHelper.authenticate(activity) { result ->
                                        when (result) {
                                            BiometricResult.Success -> onLoginSuccess()
                                            is BiometricResult.Error -> {
                                                if (result.kind == BiometricResult.ErrorKind.NO_BIOMETRICS) {
                                                    viewModel.showBiometricNeedsFirstLogin()
                                                } else {
                                                    viewModel.showBiometricUnavailable()
                                                }
                                            }
                                        }
                                    }
                                }
                                BiometricAvailability.NoneEnrolled,
                                BiometricAvailability.NoHardware,
                                BiometricAvailability.Unavailable -> {
                                    viewModel.showBiometricUnavailable()
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Fingerprint,
                        contentDescription = stringResource(R.string.login_fingerprint_content_desc),
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    }
}