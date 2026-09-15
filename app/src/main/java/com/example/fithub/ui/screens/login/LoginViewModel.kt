package com.example.fithub.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fithub.core.Resource
import com.example.fithub.core.ServiceLocator
import com.example.fithub.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val identifier: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successUserId: String? = null
)

class LoginViewModel(
    private val auth: AuthRepository = ServiceLocator.authRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onIdentifierChange(v: String) =
        _uiState.update { it.copy(identifier = v, errorMessage = null) }

    fun onPasswordChange(v: String) =
        _uiState.update { it.copy(password = v, errorMessage = null) }

    fun showBiometricUnavailable() = _uiState.update {
        it.copy(errorMessage = "Biometric not available on this device.")
    }

    fun showBiometricNeedsFirstLogin() = _uiState.update {
        it.copy(errorMessage = "Please log in with your password once before using fingerprint.")
    }

    fun submit() {
        val state = _uiState.value
        if (state.identifier.isBlank() || state.password.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Please enter your username and password.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = auth.login(state.identifier.trim(), state.password)
            when (result) {
                is Resource.Success -> {
                    // Firebase Auth now holds the session.
                    // Pull the fresh profile from Firestore so Room is populated.
                    ServiceLocator.userRepository.syncFromRemote(result.data)
                    ServiceLocator.weightRepository.syncFromRemote(result.data)
                    ServiceLocator.goalRepository.syncFromRemote(result.data)
                    ServiceLocator.nutritionGoalsRepository.syncFromRemote(result.data)
                    ServiceLocator.workoutGoalsRepository.syncFromRemote(result.data)
                    ServiceLocator.checkpointRepository.syncFromRemote(result.data)

                    _uiState.update {
                        it.copy(isLoading = false, successUserId = result.data)
                    }
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
                Resource.Loading -> Unit
            }
        }
    }
}