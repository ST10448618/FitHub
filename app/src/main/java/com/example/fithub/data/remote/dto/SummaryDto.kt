// AI-assisted (DeepSeek); reviewed and tested by the FitHub team.
package com.example.fithub.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Phase 6B: mirrors the `Summary` schema in backend/openapi.yaml exactly.
 *
 * All fields nullable except the top-level arrays — if the server ever omits
 * a sub-object, the deserializer falls back to empty defaults rather than
 * crashing, and the card renders "—".
 */
@Serializable
data class SummaryResponse(
    @SerialName("fromDate") val fromDate: String = "",
    @SerialName("toDate") val toDate: String = "",
    @SerialName("calories") val calories: List<CaloriesPointDto> = emptyList(),
    @SerialName("weightTrend") val weightTrend: List<WeightPointDto> = emptyList(),
    @SerialName("sessionsThisWeek") val sessionsThisWeek: Int = 0,
    @SerialName("streak") val streak: StreakDto = StreakDto(),
)

@Serializable
data class CaloriesPointDto(
    @SerialName("date") val date: String = "",
    @SerialName("kcal") val kcal: Int = 0,
)

@Serializable
data class WeightPointDto(
    @SerialName("date") val date: String = "",
    @SerialName("weightKg") val weightKg: Double = 0.0,
)

@Serializable
data class StreakDto(
    @SerialName("current") val current: Int = 0,
    @SerialName("longest") val longest: Int = 0,
    @SerialName("todayDone") val todayDone: Boolean = false,
    @SerialName("atRisk") val atRisk: Boolean = false,
)