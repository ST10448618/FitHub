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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fithub.R
import com.example.fithub.domain.model.ActivityLevel
import com.example.fithub.domain.model.Gender
import com.example.fithub.ui.components.*
import com.example.fithub.ui.theme.*
import com.example.fithub.ui.util.localized

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
        AppHeader(title = stringResource(R.string.edit_profile_title), onBack = onBack)

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
                stringResource(R.string.edit_profile_account_details),
                style = MaterialTheme.typography.titleMedium,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))

            FitHubTextField(
                value = state.username,
                onValueChange = viewModel::onUsernameChange,
                label = stringResource(R.string.edit_profile_username)
            )
            Spacer(Modifier.height(12.dp))
            FitHubTextField(
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                label = stringResource(R.string.edit_profile_email),
                keyboardType = KeyboardType.Email
            )

            Spacer(Modifier.height(20.dp))

            Text(
                stringResource(R.string.edit_profile_physical_details),
                style = MaterialTheme.typography.titleMedium,
                color = FitHubPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))

            FitHubTextField(
                value = state.heightCm,
                onValueChange = viewModel::onHeightChange,
                label = stringResource(R.string.edit_profile_height),
                keyboardType = KeyboardType.Number
            )
            Spacer(Modifier.height(12.dp))
            FitHubTextField(
                value = state.age,
                onValueChange = viewModel::onAgeChange,
                label = stringResource(R.string.edit_profile_age),
                keyboardType = KeyboardType.Number
            )

            Spacer(Modifier.height(12.dp))

            val genderEntries = Gender.entries
            val genderLabels = genderEntries.map { it.localized() }
            DropdownField(
                label = stringResource(R.string.edit_profile_gender),
                current = state.gender.localized(),
                options = genderLabels,
                onSelect = { selected ->
                    val idx = genderLabels.indexOf(selected)
                    if (idx >= 0) viewModel.onGenderChange(genderEntries[idx])
                }
            )

            Spacer(Modifier.height(12.dp))

            val activityEntries = ActivityLevel.entries
            val activityLabels = activityEntries.map { it.localized() }
            DropdownField(
                label = stringResource(R.string.edit_profile_lifestyle),
                current = state.activityLevel.localized(),
                options = activityLabels,
                onSelect = { selected ->
                    val idx = activityLabels.indexOf(selected)
                    if (idx >= 0) viewModel.onActivityChange(activityEntries[idx])
                }
            )

            state.errorMessage?.let { err ->
                Spacer(Modifier.height(12.dp))
                Text(
                    err,
                    style = MaterialTheme.typography.labelSmall,
                    color = ErrorRed
                )
            }

            Spacer(Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SecondaryButton(
                    text = stringResource(R.string.generic_cancel),
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                )
                PrimaryButton(
                    text = stringResource(
                        if (state.isSaving) R.string.edit_profile_saving
                        else R.string.edit_profile_save
                    ),
                    onClick = viewModel::save,
                    enabled = !state.isSaving,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}