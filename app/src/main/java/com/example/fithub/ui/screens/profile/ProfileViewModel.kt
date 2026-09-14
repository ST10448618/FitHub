package com.example.fithub.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fithub.core.ServiceLocator
import com.example.fithub.core.SessionManager
import com.example.fithub.domain.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

data class ProfileUiState(
    val isLoading: Boolean = true,
    val profile: UserProfile? = null,
    val errorMessage: String? = null
)

class ProfileViewModel : ViewModel() {

    private val uid = SessionManager.currentUserId

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val userRepo = ServiceLocator.userRepository

    init {
        val id = SessionManager.currentUserId
        if (id == null) {
            _uiState.value = ProfileUiState(
                isLoading = false,
                errorMessage = "No active session. Please log in again."
            )
        } else {
            observeProfile(id)
            trySyncIfEmpty(id)
        }
    }

    private fun observeProfile(uid: String) {
        viewModelScope.launch {
            userRepo.observeProfile(uid).collect { profile ->
                _uiState.update { it.copy(profile = profile, isLoading = false) }
            }
        }
    }

    private fun trySyncIfEmpty(uid: String) {
        viewModelScope.launch {
            // Give Room a moment for the observation to emit first
            kotlinx.coroutines.delay(300)
            if (_uiState.value.profile == null) {
                // Nothing local — pull from Firebase
                val result = userRepo.syncFromRemote(uid)
                android.util.Log.d("FitHubProfile", "syncFromRemote result: $result")
            }
        }
    }


}