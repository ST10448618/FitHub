package com.example.fithub.domain.repository

import com.example.fithub.domain.model.WeeklyInsights

/**
 * Phase 6B: server-side insights repository. Abstracts Retrofit so the
 * ViewModel can be tested without a network. The implementation is
 * [com.example.fithub.data.repository.InsightsRepositoryImpl].
 */
interface InsightsRepository {
    /**
     * Fetches the current 7-day summary. Returns [WeeklyInsights.EMPTY] on
     * any error (offline, 401, 500). The dashboard's own Room-backed cards
     * always render; this card degrades to "offline" silently.
     */
    suspend fun fetchWeeklyInsights(days: Int = 7): WeeklyInsights

    /**
     * Registers the current device's FCM token with the server. Called from
     * the FCM token-refresh callback and on app launch when a token already
     * exists in [com.example.fithub.core.PreferencesManager].
     */
    suspend fun registerFcmToken(token: String, language: String): Boolean

    /**
     * Submits one-shot feedback. Returns the created document id, or null
     * on failure.
     */
    suspend fun submitFeedback(rating: Int, message: String?, appVersion: String?): String?
}