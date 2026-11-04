package com.example.fithub.ui.screens.settings

import android.content.Context
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fithub.core.BiometricAvailability
import com.example.fithub.core.BiometricHelper
import com.example.fithub.core.LogTags
import com.example.fithub.core.ServiceLocator
import com.example.fithub.core.SessionManager
import com.example.fithub.core.await
import com.example.fithub.core.i18n.LocaleCodes
import com.example.fithub.core.i18n.LocaleManager
import com.example.fithub.core.notifications.NotificationHelper
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class SettingsUiState(
    val pushNotifications: Boolean = true,
    val language: String = LocaleCodes.DEFAULT,
    val languageNativeName: String = LocaleCodes.nativeName(LocaleCodes.DEFAULT),
    val biometricEnabled: Boolean = false,
    val biometricAvailable: Boolean = true,
    val lastActionMessage: String? = null,
    val fcmToken: String? = null
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
            prefs.language.collect { code ->
                val normalised = LocaleCodes.normalise(code)
                _uiState.value = _uiState.value.copy(
                    language = normalised,
                    languageNativeName = LocaleCodes.nativeName(normalised)
                )
            }
        }
        viewModelScope.launch {
            prefs.biometricEnabled.collect { enabled ->
                _uiState.value = _uiState.value.copy(biometricEnabled = enabled)
            }
        }
        viewModelScope.launch {
            prefs.fcmToken.collect { token ->
                _uiState.value = _uiState.value.copy(fcmToken = token)
            }
        }
    }

    fun refreshAvailability(context: Context) {
        val availability = BiometricHelper.canAuthenticate(context)
        _uiState.value = _uiState.value.copy(
            biometricAvailable = availability == BiometricAvailability.Available
        )
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setPushNotifications(enabled)
            val langCode = LocaleCodes.normalise(prefs.language.first())
            try {
                val messaging = FirebaseMessaging.getInstance()
                if (enabled) {
                    messaging.subscribeToTopic("fithub_all").await()
                    messaging.subscribeToTopic("lang_$langCode").await()
                    Log.i(LogTags.PUSH, "Subscribed to topics: fithub_all, lang_$langCode")
                } else {
                    messaging.unsubscribeFromTopic("fithub_all").await()
                    messaging.unsubscribeFromTopic("lang_$langCode").await()
                    Log.i(LogTags.PUSH, "Unsubscribed from topics")
                }
            } catch (e: Exception) {
                Log.e(LogTags.PUSH, "Topic subscription failed", e)
            }
        }
    }

    /**
     * Change the app language. Persists synchronously + DataStore asynchronously
     * (via [LocaleManager.apply]) and then recreates the activity so resources
     * reload from the new locale.
     */
    fun applyLocale(activity: FragmentActivity, code: String) {
        Log.i(LogTags.LOCALE, "SettingsViewModel.applyLocale($code)")
        LocaleManager.apply(activity, code)
    }

    fun setBiometric(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setBiometric(enabled)
            val uid = SessionManager.currentUserId
            if (uid != null) {
                val profile = ServiceLocator.userRepository.getProfile(uid)
                if (profile != null) {
                    ServiceLocator.userRepository.updateProfile(profile.copy(biometricEnabled = enabled))
                }
            }
        }
    }

    fun sendTestNotification(context: Context) {
        NotificationHelper.showNotification(
            context = context,
            notificationId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
            title = "Test Notification",
            body = "This is a test notification from the FitHub diagnostics card.",
            route = com.example.fithub.ui.navigation.Screen.DASHBOARD
        )
    }
}