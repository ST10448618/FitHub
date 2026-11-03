package com.example.fithub.ui.screens.settings

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fithub.core.BiometricAvailability
import com.example.fithub.core.BiometricHelper
import com.example.fithub.core.LogTags
import com.example.fithub.core.ServiceLocator
import com.example.fithub.core.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val pushNotifications: Boolean = true,
    val language: String = "ENGLISH",
    val biometricEnabled: Boolean = false,
    val biometricAvailable: Boolean = true,
    val lastActionMessage: String? = null
)

class SettingsViewModel : ViewModel() {

    private val prefs = ServiceLocator.preferencesManager

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            prefs.pushNotificationsEnabled.collect { enabled ->
                _uiState.value = _uiState.value.copy(pushNotifications = enabled)
            }
        }
        viewModelScope.launch {
            prefs.language.collect { lang ->
                _uiState.value = _uiState.value.copy(language = lang)
            }
        }
        viewModelScope.launch {
            prefs.biometricEnabled.collect { enabled ->
                _uiState.value = _uiState.value.copy(biometricEnabled = enabled)
            }
        }
    }

    /** Called once by the screen on first composition. */
    fun refreshAvailability(context: Context) {
        val availability = BiometricHelper.canAuthenticate(context)
        Log.d(LogTags.BIOMETRIC, "Settings: biometric availability=$availability")
        _uiState.value = _uiState.value.copy(
            biometricAvailable = availability == BiometricAvailability.Available
        )
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch { prefs.setPushNotifications(enabled) }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch { prefs.setLanguage(language) }
    }

    /**
     * Persists the biometric preference in DataStore + synchronous mirror
     * AND mirrors it to the user's Firestore profile so it survives a
     * reinstall on a different device. Falls back to Room-only when offline.
     */
    fun setBiometric(enabled: Boolean) {
        viewModelScope.launch {
            Log.i(LogTags.BIOMETRIC, "Settings: setBiometric($enabled)")
            prefs.setBiometric(enabled)
            val uid = SessionManager.currentUserId
            if (uid != null) {
                val profile = ServiceLocator.userRepository.getProfile(uid)
                if (profile != null) {
                    val result = ServiceLocator.userRepository.updateProfile(
                        profile.copy(biometricEnabled = enabled)
                    )
                    when (result) {
                        is com.example.fithub.core.Resource.Error ->
                            Log.w(LogTags.SYNC, "Biometric flag Firestore push failed: ${result.message} (kept in Room)")
                        is com.example.fithub.core.Resource.Success ->
                            Log.i(LogTags.SYNC, "Biometric flag synced to Firestore")
                        com.example.fithub.core.Resource.Loading -> Unit
                    }
                }
            }
        }
    }

    fun clearLastActionMessage() {
        _uiState.value = _uiState.value.copy(lastActionMessage = null)
    }
}