package com.example.fithub.data.repository

import android.util.Log
import com.example.fithub.core.LogTags
import com.example.fithub.core.network.RetrofitClient
import com.example.fithub.data.remote.api.FitHubApi
import com.example.fithub.data.remote.dto.FcmTokenRequest
import com.example.fithub.data.remote.dto.FeedbackRequest
import com.example.fithub.data.remote.dto.SummaryResponse
import com.example.fithub.domain.model.WeeklyInsights
import com.example.fithub.domain.repository.InsightsRepository
import java.time.LocalDate
import java.time.format.DateTimeParseException

/**
 * Phase 6B: real implementation. Uses [RetrofitClient.api].
 *
 * ### Failure semantics
 * - `fetchWeeklyInsights`: returns [WeeklyInsights.EMPTY] on any error.
 *   Network failures never crash the dashboard; the card falls back to "offline".
 * - `registerFcmToken`: returns Boolean — the caller decides whether to retry.
 * - `submitFeedback`: returns document id, or null on failure.
 *
 * No exception escapes — that would take down the ViewModel.
 */
class InsightsRepositoryImpl(
    private val api: FitHubApi = RetrofitClient.api,
) : InsightsRepository {

    override suspend fun fetchWeeklyInsights(days: Int): WeeklyInsights {
        return try {
            val r = api.getSummary(days)
            mapToDomain(r, days)
        } catch (t: Throwable) {
            Log.w(LogTags.NETWORK, "fetchWeeklyInsights failed (returning EMPTY)", t)
            WeeklyInsights.EMPTY
        }
    }

    override suspend fun registerFcmToken(token: String, language: String): Boolean {
        return try {
            val res = api.postFcmToken(FcmTokenRequest(token = token, language = language))
            Log.i(LogTags.NETWORK, "FCM token registered (prefix=${res.tokenPrefix})")
            true
        } catch (t: Throwable) {
            Log.w(LogTags.NETWORK, "registerFcmToken failed", t)
            false
        }
    }

    override suspend fun submitFeedback(rating: Int, message: String?, appVersion: String?): String? {
        return try {
            val res = api.postFeedback(FeedbackRequest(rating = rating, message = message, appVersion = appVersion))
            res.id
        } catch (t: Throwable) {
            Log.w(LogTags.NETWORK, "submitFeedback failed", t)
            null
        }
    }

    // ---------- Mapping ----------

    private fun mapToDomain(r: SummaryResponse, days: Int): WeeklyInsights {
        val total = r.calories.sumOf { it.kcal }
        val latestWeight = r.weightTrend.lastOrNull()?.weightKg
        val earliestWeight = r.weightTrend.firstOrNull()?.weightKg
        val change = if (latestWeight != null && earliestWeight != null) {
            (latestWeight - earliestWeight).let { kotlin.math.round(it * 100.0) / 100.0 }
        } else null

        return WeeklyInsights(
            windowDays = days,
            avgCaloriesPerDay = if (days > 0) total / days else 0,
            calorieTotal = total,
            latestWeightKg = latestWeight,
            weightChangeKg = change,
            sessionsThisWeek = r.sessionsThisWeek,
            currentStreak = r.streak.current,
            longestStreak = r.streak.longest,
            todayDone = r.streak.todayDone,
            atRisk = r.streak.atRisk,
            fromDate = parseDate(r.fromDate),
            toDate = parseDate(r.toDate),
        )
    }

    private fun parseDate(iso: String): LocalDate? = try {
        if (iso.isBlank()) null else LocalDate.parse(iso)
    } catch (_: DateTimeParseException) {
        null
    }
}