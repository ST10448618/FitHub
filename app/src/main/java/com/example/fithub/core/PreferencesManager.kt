// AI-assisted (DeepSeek); reviewed and tested by the FitHub team.
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

/**
 * Reference: https://developer.android.com/topic/libraries/architecture/datastore
 *            https://developer.android.com/reference/android/content/SharedPreferences
 * (accessed February 2026)
 */
class PreferencesManager(private val context: Context) {

    private object Keys {
        val PUSH_NOTIFICATIONS = booleanPreferencesKey("push_notifications")
        val LANGUAGE = stringPreferencesKey("language")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")

        val CLAIMED_REWARDS = stringPreferencesKey("claimed_rewards")
    }

    /** Synchronous mirror file — tiny, only read at process start. */
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

    suspend fun setPushNotifications(enabled: Boolean) {
        context.dataStore.edit { it[Keys.PUSH_NOTIFICATIONS] = enabled }
    }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language }
    }

    /**
     * Persists the biometric preference in both DataStore and the synchronous
     * `SharedPreferences` mirror. Callers must go through here (never write the
     * mirror directly) so the two stay in sync.
     */
    suspend fun setBiometric(enabled: Boolean) {
        context.dataStore.edit { it[Keys.BIOMETRIC_ENABLED] = enabled }
        // Commit (synchronous) because this is read by the very next cold start
        // in Application.onCreate — a delayed apply() could miss it if the
        // process is killed immediately after the toggle.
        securityPrefs.edit().putBoolean(SECURITY_KEY_BIOMETRIC_LOCK, enabled).commit()
        Log.i(LogTags.BIOMETRIC, "Biometric preference saved: enabled=$enabled (mirror + DataStore)")
    }

    /**
     * Blocking read of the biometric-lock mirror. Safe from any thread; this is
     * the single tiny SharedPreferences read that the brief explicitly allows
     * on the main thread (called only from Application.onCreate / Splash /
     * LoginScreen).
     */
    fun isBiometricLockEnabledSync(): Boolean =
        securityPrefs.getBoolean(SECURITY_KEY_BIOMETRIC_LOCK, false)

    companion object {
        private const val SECURITY_PREFS_NAME = "fithub_security"
        private const val SECURITY_KEY_BIOMETRIC_LOCK = "biometric_lock_enabled"
    }
}