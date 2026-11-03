package com.example.fithub.core

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "fithub_prefs")

class PreferencesManager(private val context: Context) {

    private object Keys {
        val PUSH_NOTIFICATIONS = booleanPreferencesKey("push_notifications")
        val LANGUAGE = stringPreferencesKey("language")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val CLAIMED_REWARDS = stringPreferencesKey("claimed_rewards")
        // Phase 4
        val FCM_TOKEN = stringPreferencesKey("fcm_token")
        val NOTIFICATION_RATIONALE_SHOWN = booleanPreferencesKey("notification_rationale_shown")
    }

    private val securityPrefs = context.applicationContext
        .getSharedPreferences(SECURITY_PREFS_NAME, Context.MODE_PRIVATE)

    val claimedRewards: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[Keys.CLAIMED_REWARDS]
            ?.split(",")
            ?.filter { it.isNotBlank() }
            ?.toSet()
            ?: emptySet()
    }

    suspend fun addClaimedReward(rewardId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.CLAIMED_REWARDS]
                ?.split(",")
                ?.filter { it.isNotBlank() }
                .orEmpty()
            val updated = (current + rewardId).distinct()
            prefs[Keys.CLAIMED_REWARDS] = updated.joinToString(",")
        }
    }

    val pushNotificationsEnabled: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.PUSH_NOTIFICATIONS] ?: true }

    val language: Flow<String> =
        context.dataStore.data.map { it[Keys.LANGUAGE] ?: "ENGLISH" }

    val biometricEnabled: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.BIOMETRIC_ENABLED] ?: false }

    // Phase 4
    val fcmToken: Flow<String?> =
        context.dataStore.data.map { it[Keys.FCM_TOKEN] }

    val notificationRationaleShown: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.NOTIFICATION_RATIONALE_SHOWN] ?: false }

    suspend fun setPushNotifications(enabled: Boolean) {
        context.dataStore.edit { it[Keys.PUSH_NOTIFICATIONS] = enabled }
    }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language }
    }

    suspend fun setBiometric(enabled: Boolean) {
        context.dataStore.edit { it[Keys.BIOMETRIC_ENABLED] = enabled }
        securityPrefs.edit().putBoolean(SECURITY_KEY_BIOMETRIC_LOCK, enabled).commit()
        Log.i(LogTags.BIOMETRIC, "Biometric preference saved: enabled=$enabled")
    }

    fun isBiometricLockEnabledSync(): Boolean =
        securityPrefs.getBoolean(SECURITY_KEY_BIOMETRIC_LOCK, false)

    suspend fun setFcmToken(token: String) {
        context.dataStore.edit { it[Keys.FCM_TOKEN] = token }
    }

    suspend fun setNotificationRationaleShown(shown: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATION_RATIONALE_SHOWN] = shown }
    }

    companion object {
        private const val SECURITY_PREFS_NAME = "fithub_security"
        private const val SECURITY_KEY_BIOMETRIC_LOCK = "biometric_lock_enabled"
    }
}