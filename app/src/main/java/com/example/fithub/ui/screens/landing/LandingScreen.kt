package com.example.fithub.ui.screens.landing

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fithub.R
import com.example.fithub.core.LogTags
import com.example.fithub.ui.components.FitHubLogo
import com.example.fithub.ui.components.PrimaryButton
import com.example.fithub.ui.components.SecondaryButton
import com.example.fithub.ui.screens.auth.GoogleSignInOutcome
import com.example.fithub.ui.screens.auth.GoogleSignInViewModel
import com.example.fithub.ui.theme.FitHubMidBlue
import com.example.fithub.ui.theme.FitHubPrimary
import com.example.fithub.ui.theme.FitHubSecondary

@Composable
fun LandingScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onGoogleNeedsOnboarding: () -> Unit = {},
    onGoogleExistingUser: () -> Unit = {},
    googleViewModel: GoogleSignInViewModel = viewModel()
) {
    val context = LocalContext.current
    val googleState by googleViewModel.uiState.collectAsState()

    LaunchedEffect(googleState.outcome) {
        when (googleState.outcome) {
            GoogleSignInOutcome.ExistingUser -> {
                googleViewModel.consumeOutcome()
                onGoogleExistingUser()
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
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1f))

            FitHubLogo(iconSize = 200.dp)

            Spacer(Modifier.weight(1f))

            PrimaryButton(
                text = stringResource(R.string.login_button),
                onClick = onLoginClick
            )

            Spacer(Modifier.height(14.dp))

            SecondaryButton(
                text = stringResource(R.string.landing_register),
                onClick = onRegisterClick,
                borderColor = Color.White,
                contentColor = Color.White
            )

            Spacer(Modifier.height(14.dp))

            SecondaryButton(
                text = if (googleState.isWorking) {
                    stringResource(R.string.auth_google_progress)
                } else {
                    "G   " + stringResource(R.string.auth_continue_with_google)
                },
                onClick = {
                    Log.i(LogTags.AUTH, "LandingScreen: Continue with Google tapped")
                    googleViewModel.start(context)
                },
                enabled = !googleState.isWorking,
                borderColor = Color.White,
                contentColor = Color.White
            )

            googleState.error?.let { msg ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text = msg,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFFFCDD2)
                )
            }
        }
    }
}