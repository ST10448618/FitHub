package com.example.fithub.domain.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class WeeklyChallengeCalculatorTest {

    // 2026-02-15 is a Sunday. Week = Mon 2026-02-09 .. Sun 2026-02-15.
    private val sunday = LocalDate.of(2026, 2, 15)
    private val monday = LocalDate.of(2026, 2, 9)
    private val wednesday = LocalDate.of(2026, 2, 11)

    @Test fun `no sessions completed`() {
        val p = WeeklyChallengeCalculator.calculate(emptyList(), sunday, 4)
        assertEquals(0, p.completed)
        assertEquals(4, p.target)
        assertFalse(p.done)
        assertEquals(4, p.remaining)
    }

    @Test fun `some sessions this week`() {
        val p = WeeklyChallengeCalculator.calculate(
            listOf(monday, wednesday),
            sunday,
            4
        )
        assertEquals(2, p.completed)
        assertFalse(p.done)
        assertEquals(2, p.remaining)
    }

    @Test fun `all sessions complete - target met`() {
        val p = WeeklyChallengeCalculator.calculate(
            listOf(monday, monday, wednesday, sunday),
            sunday,
            4
        )
        assertEquals(4, p.completed)
        assertTrue(p.done)
        assertEquals(0, p.remaining)
    }

    @Test fun `sessions outside the week are ignored`() {
        // 7 days before today is Sun 2026-02-08, outside the current Mon..Sun.
        val p = WeeklyChallengeCalculator.calculate(
            listOf(sunday.minusDays(7), monday),
            sunday,
            4
        )
        assertEquals(1, p.completed)
    }

    @Test fun `zero target is trivially complete`() {
        val p = WeeklyChallengeCalculator.calculate(emptyList(), sunday, 0)
        assertTrue(p.done)
        assertEquals(0, p.remaining)
    }

    @Test fun `completed above target still clamped remaining to zero`() {
        val p = WeeklyChallengeCalculator.calculate(
            listOf(monday, monday, monday, monday, monday),
            sunday,
            4
        )
        assertEquals(5, p.completed)
        assertTrue(p.done)
        assertEquals(0, p.remaining)
    }
}