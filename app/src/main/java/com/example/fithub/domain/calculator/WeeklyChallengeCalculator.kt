package com.example.fithub.domain.calculator

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/**
 * Reference: https://developer.android.com/guide/background/workmanager
 * (accessed February 2026)
 */
object WeeklyChallengeCalculator {

    data class Progress(
        val completed: Int,
        val target: Int,
        val done: Boolean,
        val remaining: Int
    )

    /**
     * @param completedDates all days on which a workout was completed.
     * @param today used to compute the Mon–Sun window; typically `LocalDate.now()`.
     * @param targetSessionsPerWeek the weekly goal; if <= 0 the challenge is
     *   considered trivially complete (`done = true`, `remaining = 0`).
     */
    fun calculate(
        completedDates: Collection<LocalDate>,
        today: LocalDate,
        targetSessionsPerWeek: Int
    ): Progress {
        val target = targetSessionsPerWeek.coerceAtLeast(0)
        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        val inWeek = completedDates.count { it in weekStart..weekEnd }

        return if (target == 0) {
            Progress(completed = inWeek, target = 0, done = true, remaining = 0)
        } else {
            Progress(
                completed = inWeek,
                target = target,
                done = inWeek >= target,
                remaining = (target - inWeek).coerceAtLeast(0)
            )
        }
    }
}