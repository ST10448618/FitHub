package com.example.fithub.domain.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * Pure-JVM unit tests for [StreakCalculator]. Runs in
 * `./gradlew :app:testDebugUnitTest`, no Android classes required.
 */
class StreakCalculatorTest {

    private val today = LocalDate.of(2026, 2, 15)

    @Test fun `empty set gives zero streak`() {
        val r = StreakCalculator.calculate(emptySet(), today)
        assertEquals(0, r.current)
        assertEquals(0, r.longest)
        assertFalse(r.todayDone)
        assertFalse(r.atRisk)
    }

    @Test fun `only today is a one-day streak`() {
        val r = StreakCalculator.calculate(setOf(today), today)
        assertEquals(1, r.current)
        assertEquals(1, r.longest)
        assertTrue(r.todayDone)
        assertFalse(r.atRisk)
    }

    @Test fun `yesterday and today is a two-day streak`() {
        val r = StreakCalculator.calculate(
            setOf(today.minusDays(1), today),
            today
        )
        assertEquals(2, r.current)
        assertEquals(2, r.longest)
        assertTrue(r.todayDone)
        assertFalse(r.atRisk)
    }

    @Test fun `yesterday only keeps streak alive but marks it at risk`() {
        val r = StreakCalculator.calculate(
            setOf(today.minusDays(2), today.minusDays(1)),
            today
        )
        assertEquals(2, r.current)
        assertFalse(r.todayDone)
        assertTrue(r.atRisk)
    }

    @Test fun `a missed day in the middle resets current`() {
        val r = StreakCalculator.calculate(
            setOf(today.minusDays(4), today.minusDays(3), today.minusDays(1), today),
            today
        )
        // Yesterday + today = 2 in a row (yesterday resets to 1 after the gap).
        assertEquals(2, r.current)
        // Longest was 2 (either the two early days or the two recent days).
        assertEquals(2, r.longest)
    }

    @Test fun `longest streak spans history`() {
        val r = StreakCalculator.calculate(
            setOf(
                today.minusDays(20),
                today.minusDays(19),
                today.minusDays(18),
                today.minusDays(17),
                today.minusDays(16),
                today
            ),
            today
        )
        assertEquals(1, r.current)
        assertEquals(5, r.longest)
        assertTrue(r.todayDone)
    }

    @Test fun `at risk requires an existing streak of 2 or more`() {
        // Only yesterday → current = 1, not at risk.
        val r1 = StreakCalculator.calculate(setOf(today.minusDays(1)), today)
        assertEquals(1, r1.current)
        assertFalse(r1.atRisk)

        // Yesterday + day-before → current = 2, at risk.
        val r2 = StreakCalculator.calculate(
            setOf(today.minusDays(2), today.minusDays(1)),
            today
        )
        assertEquals(2, r2.current)
        assertTrue(r2.atRisk)
    }

    @Test fun `no activity at all in future dates still works`() {
        // Edge case: user's clock moved backwards and 'today' is before the data.
        // Current stays 0 because anchor is not in the set.
        val r = StreakCalculator.calculate(setOf(today.plusDays(1)), today)
        assertEquals(0, r.current)
        assertEquals(1, r.longest)
        assertFalse(r.todayDone)
        assertFalse(r.atRisk)
    }
}