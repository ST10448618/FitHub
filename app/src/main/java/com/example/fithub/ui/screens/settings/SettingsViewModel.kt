package com.example.fithub.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fithub.core.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val pushNotifications: Boolean = true,
    val language: String = "ENGLISH",
    val biometricEnabled: Boolean = false
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

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch { prefs.setPushNotifications(enabled) }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch { prefs.setLanguage(language) }
    }

    fun setBiometric(enabled: Boolean) {
        viewModelScope.launch { prefs.setBiometric(enabled) }
    }
}