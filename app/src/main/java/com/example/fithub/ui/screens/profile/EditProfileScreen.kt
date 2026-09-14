package com.example.fithub.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.example.fithub.ui.components.*
import com.example.fithub.ui.theme.*
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: EditProfileViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.savedSuccessfully) {
        if (state.savedSuccessfully) onSaved()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        AppHeader(title = "Edit Profile", onBack = onBack)

        if (state.isLoading) {
            LoadingState()
            return@Column
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(listOf(FitHubMidBlue, FitHubPrimary))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(54.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "Account Details",
                style = MaterialTheme.typography.titleMedium,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))

            FitHubTextField(
                value = state.username,
                onValueChange = viewModel::onUsernameChange,
                label = "Username"
            )
            Spacer(Modifier.height(12.dp))
            FitHubTextField(
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                label = "Email Address",
                keyboardType = KeyboardType.Email
            )

            Spacer(Modifier.height(20.dp))

            Text(
                "Physical Details",
                style = MaterialTheme.typography.titleMedium,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))

            FitHubTextField(
                value = state.heightCm,
                onValueChange = viewModel::onHeightChange,
                label = "Height",
                keyboardType = KeyboardType.Number
            )
            Spacer(Modifier.height(12.dp))
            FitHubTextField(
                value = state.age,
                onValueChange = viewModel::onAgeChange,
                label = "Age",
                keyboardType = KeyboardType.Number
            )

            Spacer(Modifier.height(12.dp))

            DropdownField(
                label = "Gender",
                current = state.gender.name.lowercase().replaceFirstChar { it.uppercase() },
                options = listOf("Male", "Female", "Other"),
                onSelect = { selected ->
                    val gender = when (selected) {
                        "Male" -> com.example.fithub.domain.model.Gender.MALE
                        "Female" -> com.example.fithub.domain.model.Gender.FEMALE
                        else -> com.example.fithub.domain.model.Gender.OTHER
                    }
                    viewModel.onGenderChange(gender)
                }
            )

            Spacer(Modifier.height(12.dp))

            DropdownField(
                label = "Lifestyle",
                current = state.activityLevel.label,
                options = com.example.fithub.domain.model.ActivityLevel.entries.map { it.label },
                onSelect = { selected ->
                    val level = com.example.fithub.domain.model.ActivityLevel.entries
                        .firstOrNull { it.label == selected } ?: state.activityLevel
                    viewModel.onActivityChange(level)
                }
            )

            if (state.errorMessage != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    state.errorMessage!!,
                    style = MaterialTheme.typography.labelSmall,
                    color = ErrorRed
                )
            }

            Spacer(Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SecondaryButton(
                    text = "Cancel",
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                )
                PrimaryButton(
                    text = if (state.isSaving) "Saving..." else "Save Changes",
                    onClick = viewModel::save,
                    enabled = !state.isSaving,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}