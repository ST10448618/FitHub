package com.example.fithub.ui.screens.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fithub.R
import com.example.fithub.core.LogTags
import com.example.fithub.core.PostLoginSync
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

    /** Called when biometric hardware is unavailable — error text via resources. */
    fun showBiometricUnavailable() {
        val msg = ServiceLocator.appContext().getString(R.string.login_biometric_unavailable)
        _uiState.update { it.copy(errorMessage = msg) }
    }

    /** Called when no cached Firebase user exists for biometric unlock. */
    fun showBiometricNeedsFirstLogin() {
        val msg = ServiceLocator.appContext().getString(R.string.login_biometric_need_first_login)
        _uiState.update { it.copy(errorMessage = msg) }
    }

    fun submit() {
        val state = _uiState.value
        if (state.identifier.isBlank() || state.password.isBlank()) {
            val msg = ServiceLocator.appContext().getString(R.string.login_missing_credentials)
            _uiState.update { it.copy(errorMessage = msg) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = auth.login(state.identifier.trim(), state.password)
            when (result) {
                is Resource.Success -> {
                    PostLoginSync.run(result.data)

                    runCatching { ServiceLocator.syncManager.flush(result.data) }
                        .onFailure { Log.e(LogTags.SYNC, "Login flush threw", it) }

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