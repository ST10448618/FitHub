package com.example.fithub.domain.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class StreakCalculatorTest {
    private val today = LocalDate.of(2026, 11, 5)

    @Test
    fun `empty activity yields zero streak`() {
        val result = StreakCalculator.calculate(emptySet(), today)
        assertEquals(0, result.current)
        assertEquals(0, result.longest)
        assertFalse(result.todayDone)
        assertFalse(result.atRisk)
    }

    @Test
    fun `consecutive days build current streak`() {
        val dates = setOf(today, today.minusDays(1), today.minusDays(2))
        val result = StreakCalculator.calculate(dates, today)
        assertEquals(3, result.current)
        assertEquals(3, result.longest)
        assertTrue(result.todayDone)
        assertFalse(result.atRisk)
    }

    @Test
    fun `gap in days resets current streak`() {
        val dates = setOf(today, today.minusDays(1), today.minusDays(3))
        val result = StreakCalculator.calculate(dates, today)
        assertEquals(2, result.current)
        assertEquals(2, result.longest)
    }

    @Test
    fun `longest streak tracked independently`() {
        val dates = setOf(
            today.minusDays(10), today.minusDays(9), today.minusDays(8), today.minusDays(7),
            today.minusDays(1), today
        )
        val result = StreakCalculator.calculate(dates, today)
        assertEquals(2, result.current)
        assertEquals(4, result.longest)
    }

    @Test
    fun `streak alive but at risk if yesterday done`() {
        val dates = setOf(today.minusDays(1), today.minusDays(2))
        val result = StreakCalculator.calculate(dates, today)
        assertEquals(2, result.current)
        assertFalse(result.todayDone)
        assertTrue(result.atRisk)
    }
}