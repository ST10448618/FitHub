package com.example.fithub.domain.calculator

import com.example.fithub.domain.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class CalculatorTests {

    // ---- Goal direction ----

    @Test
    fun `goal direction lose when target below current`() {
        val d = GoalDirectionCalculator.from(70.0, 60.0)
        assertEquals(GoalDirection.LOSE, d)
    }

    @Test
    fun `goal direction gain when target above current`() {
        val d = GoalDirectionCalculator.from(60.0, 70.0)
        assertEquals(GoalDirection.GAIN, d)
    }

    @Test
    fun `goal direction maintain when within tolerance`() {
        val d = GoalDirectionCalculator.from(70.0, 70.2)
        assertEquals(GoalDirection.MAINTAIN, d)
    }

    // ---- Weight progress ----

    @Test
    fun `weight progress gain mid-way`() {
        val r = WeightProgressCalculator.calculate(
            startingWeightKg = 60.0,
            currentWeightKg = 70.0,
            targetWeightKg = 80.0,
            direction = GoalDirection.GAIN
        )
        assertEquals(50, r.clampedPercent)
    }

    @Test
    fun `weight progress loss mid-way`() {
        val r = WeightProgressCalculator.calculate(
            startingWeightKg = 80.0,
            currentWeightKg = 70.0,
            targetWeightKg = 60.0,
            direction = GoalDirection.LOSE
        )
        assertEquals(50, r.clampedPercent)
    }

    @Test
    fun `weight progress maintain within tolerance yields 100`() {
        val r = WeightProgressCalculator.calculate(
            startingWeightKg = 70.0,
            currentWeightKg = 70.1,
            targetWeightKg = 70.2,
            direction = GoalDirection.MAINTAIN
        )
        assertEquals(100, r.clampedPercent)
    }

    // ---- Meal allocation ----

    @Test
    fun `meal allocation valid sums to daily target`() {
        val meals = MealTargets(720, 780, 900, 600)
        assertTrue(MacroCalculator.isMealAllocationValid(3000, meals))
    }

    @Test
    fun `meal allocation invalid when sum does not equal daily target`() {
        val meals = MealTargets(720, 780, 900, 750)
        assertFalse(MacroCalculator.isMealAllocationValid(3000, meals))
    }

    // ---- Macro starter ----

    @Test
    fun `starter macros for 3000 kcal`() {
        val m = MacroCalculator.starterMacros(3000)
        // 20% protein => 600 kcal / 4 = 150g
        assertEquals(150, m.proteinG)
        // 50% carbs => 1500 kcal / 4 = 375g
        assertEquals(375, m.carbsG)
        // 30% fat => 900 kcal / 9 = 100g
        assertEquals(100, m.fatG)
    }

    // ---- Calorie engine ----

    @Test
    fun `bmr mifflin st jeor male 20y 169cm 60kg`() {
        val bmr = CalorieEngine.calculateBmr(
            age = 20, gender = Gender.MALE, heightCm = 169.0, weightKg = 60.0
        )
        // (10*60)+(6.25*169)-(5*20)+5 = 600 + 1056.25 - 100 + 5 = 1561.25
        assertEquals(1561.25, bmr, 0.01)
    }

    @Test
    fun `under-18 does not receive goal adjustment`() {
        val out = CalorieEngine.calculate(
            CalorieEngine.Input(
                age = 15, gender = Gender.MALE, heightCm = 160.0,
                weightKg = 55.0, activityLevel = ActivityLevel.MODERATELY_ACTIVE,
                goalDirection = GoalDirection.LOSE
            )
        )
        assertEquals(0, out.goalAdjustmentApplied)
        assertFalse(out.adultAdjustmentApplied)
    }

    @Test
    fun `adult receives goal adjustment`() {
        val out = CalorieEngine.calculate(
            CalorieEngine.Input(
                age = 25, gender = Gender.MALE, heightCm = 175.0,
                weightKg = 75.0, activityLevel = ActivityLevel.MODERATELY_ACTIVE,
                goalDirection = GoalDirection.LOSE
            )
        )
        assertEquals(-250, out.goalAdjustmentApplied)
        assertTrue(out.adultAdjustmentApplied)
    }

    // ---- Nutrition progress ----

    @Test
    fun `average daily intake ignores unlogged days`() {
        val uid = "u1"
        val logs = listOf(
            foodLog(uid, LocalDate.of(2026, 1, 1), calories = 2000.0),
            foodLog(uid, LocalDate.of(2026, 1, 2), calories = 2400.0)
        )
        val range = LocalDate.of(2026, 1, 1)..LocalDate.of(2026, 1, 5)
        val avg = NutritionProgressCalculator.averageDailyIntake(logs, range)
        // (2000 + 2400) / 2 logged days = 2200 — not /5
        assertEquals(2200.0, avg.calories, 0.01)
    }

    // ---- Workout progress ----

    @Test
    fun `weekly progress counts only current week`() {
        val uid = "u1"
        val monday = LocalDate.of(2026, 1, 5)
        val sessions = listOf(
            session(uid, monday),
            session(uid, monday.plusDays(1)),
            session(uid, monday.plusDays(2)),
            session(uid, monday.minusDays(2)) // previous week
        )
        val progress = WorkoutProgressCalculator.weeklyProgress(
            sessions, monday, weeklyTarget = 4
        )
        // 3 of 4 = 75%
        assertEquals(75, progress)
    }

    // ---- Helpers ----

    private fun foodLog(uid: String, date: LocalDate, calories: Double) = FoodLog(
        id = "fl_${date}_$calories",
        userId = uid,
        foodId = null,
        foodName = "Test",
        brandName = null,
        imageUrl = null,
        mealType = MealType.BREAKFAST,
        source = FoodSource.MANUAL,
        barcode = null,
        portionSize = 1.0,
        portionUnit = "serving",
        portionGrams = 100.0,
        calories = calories,
        proteinG = 20.0,
        carbsG = 30.0,
        fatG = 10.0,
        logDate = date,
        loggedAt = LocalDateTime.now()
    )

    private fun session(uid: String, date: LocalDate) = WorkoutSession(
        id = "ws_$date",
        userId = uid,
        workoutPlanId = "p1",
        workoutName = "Test Workout",
        workoutCategory = WorkoutCategory.GENERAL_FITNESS,
        exercises = emptyList(),
        durationMinutes = 30,
        estimatedActivityKcal = 250,
        status = SessionStatus.COMPLETED,
        startedAt = LocalDateTime.now(),
        completedAt = LocalDateTime.now(),
        logDate = date
    )
}