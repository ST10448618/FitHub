package com.example.fithub.domain.calculator

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Reference: https://developer.android.com/training/testing/local-tests
 *            https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.time/-duration/
 * (accessed February 2026)
 */
object StreakCalculator {

    data class Result(
        val current: Int,
        val longest: Int,
        val todayDone: Boolean,
        val atRisk: Boolean
    )

    fun calculate(
        activeDates: Set<LocalDate>,
        today: LocalDate
    ): Result {
        if (activeDates.isEmpty()) {
            return Result(current = 0, longest = 0, todayDone = false, atRisk = false)
        }

        val todayDone = today in activeDates

        // Longest streak across the whole history.
        val sorted = activeDates.sorted()
        var longest = 1
        var run = 1
        for (i in 1 until sorted.size) {
            if (ChronoUnit.DAYS.between(sorted[i - 1], sorted[i]) == 1L) {
                run++
                if (run > longest) longest = run
            } else {
                run = 1
            }
        }

        // Current streak: walk backwards from either today or yesterday.
        val anchor = if (todayDone) today else today.minusDays(1)
        var current = 0
        var cursor = anchor
        while (cursor in activeDates) {
            current++
            cursor = cursor.minusDays(1)
        }

        // At-risk only when the user has an active streak that depended on
        // yesterday and hasn't logged today.
        val atRisk = !todayDone && current >= 2

        return Result(
            current = current,
            longest = maxOf(longest, current),
            todayDone = todayDone,
            atRisk = atRisk
        )
    }
}