package com.example.fithub.domain.model

import java.time.LocalDate

/**
 * Phase 6B: domain-level representation of the server's `/me/summary` response.
 * Deliberately decoupled from the DTO so the DTO can change (e.g. snake_case →
 * camelCase) without touching the UI.
 */
data class WeeklyInsights(
    val windowDays: Int = 7,
    val avgCaloriesPerDay: Int = 0,
    val calorieTotal: Int = 0,
    val latestWeightKg: Double? = null,
    val weightChangeKg: Double? = null,
    val sessionsThisWeek: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val todayDone: Boolean = false,
    val atRisk: Boolean = false,
    val fromDate: LocalDate? = null,
    val toDate: LocalDate? = null,
) {
    companion object {
        /** Default "no data yet" state — the card renders placeholder text. */
        val EMPTY = WeeklyInsights()
    }
}