package com.example.fithub.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fithub.core.Resource
import com.example.fithub.core.ServiceLocator
import com.example.fithub.core.SessionManager
import com.example.fithub.domain.model.ActivityLevel
import com.example.fithub.domain.model.Gender
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditProfileUiState(
    val isLoading: Boolean = true,
    val username: String = "",
    val email: String = "",
    val age: String = "",
    val heightCm: String = "",
    val gender: Gender = Gender.OTHER,
    val activityLevel: ActivityLevel = ActivityLevel.SEDENTARY,
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,
    val errorMessage: String? = null
)

class EditProfileViewModel : ViewModel() {

    private val uid: String? = SessionManager.currentUserId

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    private val userRepo = ServiceLocator.userRepository
    init {
        val uid = SessionManager.currentUserId
        if (uid == null) {
            _uiState.update {
                it.copy(isLoading = false, errorMessage = "No active session. Please log in again.")
            }
        } else {
            viewModelScope.launch {
                userRepo.observeProfile(uid).collect { profile ->
                    if (profile != null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                username = profile.firstName,
                                email = profile.email,
                                age = profile.age.toString(),
                                heightCm = profile.heightCm.toInt().toString(),
                                gender = profile.gender,
                                activityLevel = profile.activityLevel
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Profile not found. Please complete onboarding."
                            )
                        }
                    }
                }
            }
        }
    }

    fun onUsernameChange(v: String) = _uiState.update { it.copy(username = v, errorMessage = null) }
    fun onEmailChange(v: String) = _uiState.update { it.copy(email = v, errorMessage = null) }
    fun onAgeChange(v: String) = _uiState.update { it.copy(age = v, errorMessage = null) }
    fun onHeightChange(v: String) = _uiState.update { it.copy(heightCm = v, errorMessage = null) }
    fun onGenderChange(v: Gender) = _uiState.update { it.copy(gender = v) }
    fun onActivityChange(v: ActivityLevel) = _uiState.update { it.copy(activityLevel = v) }

    fun save() {
        val state = _uiState.value

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    errorMessage = null,
                    savedSuccessfully = false
                )
            }

            val userId = uid
            if (userId == null) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "No active session. Please log in again."
                    )
                }
                return@launch
            }

            val existing = userRepo.getProfile(userId)

            if (existing == null) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Profile not found."
                    )
                }
                return@launch
            }

            val updated = existing.copy(
                firstName = state.username.trim(),
                email = state.email.trim(),
                age = state.age.toIntOrNull() ?: existing.age,
                heightCm = state.heightCm.toDoubleOrNull() ?: existing.heightCm,
                gender = state.gender,
                activityLevel = state.activityLevel
            )

            when (val result = userRepo.updateProfile(updated)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            savedSuccessfully = true
                        )
                    }
                }

                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = result.message
                        )
                    }
                }

                Resource.Loading -> {
                    // Nothing to do
                }
            }
        }
    }

}