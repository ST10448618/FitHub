package com.example.fithub.core

import android.content.Context
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
    }

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

    suspend fun setBiometric(enabled: Boolean) {
        context.dataStore.edit { it[Keys.BIOMETRIC_ENABLED] = enabled }
    }
}