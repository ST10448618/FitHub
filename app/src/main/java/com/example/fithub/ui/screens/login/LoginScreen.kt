package com.example.fithub.ui.screens.login

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fithub.ui.components.FitHubLogo
import com.example.fithub.ui.components.FitHubPasswordField
import com.example.fithub.ui.components.FitHubTextField
import com.example.fithub.ui.components.PrimaryButton
import com.example.fithub.ui.theme.*

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.successUserId) {
        if (state.successUserId != null) onLoginSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(FitHubSecondary, FitHubMidBlue, FitHubPrimary)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FitHubLogo(
                iconSize = 160.dp
            )

            Spacer(Modifier.height(32.dp))

            // Login card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .padding(20.dp)
            ) {
                Text(
                    text = "Welcome Back!",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(Modifier.height(16.dp))

                FitHubTextField(
                    value = state.identifier,
                    onValueChange = viewModel::onIdentifierChange,
                    label = "USERNAME",
                    placeholder = "Enter your Username",
                    isError = state.errorMessage != null
                )

                Spacer(Modifier.height(12.dp))

                FitHubPasswordField(
                    value = state.password,
                    onValueChange = viewModel::onPasswordChange,
                    isError = state.errorMessage != null
                )

                if (state.errorMessage != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = state.errorMessage!!,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFFCDD2)
                    )
                }

                Spacer(Modifier.height(20.dp))

                PrimaryButton(
                    text = if (state.isLoading) "Logging in..." else "LOGIN",
                    onClick = viewModel::submit,
                    enabled = !state.isLoading
                )
            }

            Spacer(Modifier.height(20.dp))

            // OR divider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color.White.copy(alpha = 0.4f)
                )
                Text(
                    "  OR  ",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color.White.copy(alpha = 0.4f)
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Sign - In with Fingerprint",
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
                        // TODO(Track A): trigger Android BiometricPrompt
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Fingerprint,
                    contentDescription = "Fingerprint login",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}