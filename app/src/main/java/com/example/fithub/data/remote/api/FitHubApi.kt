package com.example.fithub.data.remote.api

import com.example.fithub.data.remote.dto.FcmTokenRequest
import com.example.fithub.data.remote.dto.FcmTokenResponse
import com.example.fithub.data.remote.dto.FeedbackRequest
import com.example.fithub.data.remote.dto.FeedbackResponse
import com.example.fithub.data.remote.dto.SummaryResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Phase 6B: Retrofit interface for the FitHub REST API.
 *
 * Only the endpoints the Android client needs. Admin-only `/admin/broadcast`
 * is intentionally NOT here — it's called with curl for the demo, not by the app.
 *
 * All methods are `suspend` — Retrofit 3.0 supports Kotlin coroutines natively.
 *
 * Reference: https://square.github.io/retrofit/ (accessed February 2026)
 */
interface FitHubApi {

    /** GET /api/v1/me/summary?days=N — aggregated insights for the dashboard. */
    @GET("api/v1/me/summary")
    suspend fun getSummary(@Query("days") days: Int = 7): SummaryResponse

    /** POST /api/v1/me/fcm-token — register the push token. Called on app launch. */
    @POST("api/v1/me/fcm-token")
    suspend fun postFcmToken(@Body body: FcmTokenRequest): FcmTokenResponse

    /** POST /api/v1/me/feedback — one-shot feedback submission. */
    @POST("api/v1/me/feedback")
    suspend fun postFeedback(@Body body: FeedbackRequest): FeedbackResponse
}