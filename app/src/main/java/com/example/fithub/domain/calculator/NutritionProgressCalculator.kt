package com.example.fithub.domain.calculator

import com.example.fithub.domain.model.FoodLog
import com.example.fithub.domain.model.MacroTargets
import com.example.fithub.domain.model.MealTargets
import com.example.fithub.domain.model.MealType
import java.time.LocalDate
import kotlin.math.roundToInt

/**
 * Aggregations for Dashboard and Nutrition Overview.
 * All aggregation is done on real FoodLog records only.
 */
object NutritionProgressCalculator {

    data class DailyTotals(
        val calories: Double = 0.0,
        val proteinG: Double = 0.0,
        val carbsG: Double = 0.0,
        val fatG: Double = 0.0
    ) {
        fun rounded(): DailyTotals = DailyTotals(
            calories = calories,
            proteinG = proteinG,
            carbsG = carbsG,
            fatG = fatG
        )
    }

    data class MealBreakdown(
        val breakfast: Double = 0.0,
        val lunch: Double = 0.0,
        val dinner: Double = 0.0,
        val snack: Double = 0.0
    )

    fun dailyTotals(logs: List<FoodLog>): DailyTotals {
        var cal = 0.0; var p = 0.0; var c = 0.0; var f = 0.0
        logs.forEach {
            cal += it.calories
            p += it.proteinG
            c += it.carbsG
            f += it.fatG
        }
        return DailyTotals(cal, p, c, f)
    }

    fun mealBreakdown(logs: List<FoodLog>): MealBreakdown {
        val byMeal = logs.groupBy { it.mealType }
        fun sumFor(meal: MealType) = byMeal[meal]?.sumOf { it.calories } ?: 0.0
        return MealBreakdown(
            breakfast = sumFor(MealType.BREAKFAST),
            lunch = sumFor(MealType.LUNCH),
            dinner = sumFor(MealType.DINNER),
            snack = sumFor(MealType.SNACK)
        )
    }

    /**
     * Average daily intake across a date range.
     * IMPORTANT: only averages over days that actually have logs.
     * Missing days are NOT counted as zero.
     */
    fun averageDailyIntake(
        logs: List<FoodLog>,
        dateRange: ClosedRange<LocalDate>
    ): DailyTotals {
        if (logs.isEmpty()) return DailyTotals()
        val byDate = logs.groupBy { it.logDate }
        val loggedDays = byDate.keys.filter { it in dateRange }
        if (loggedDays.isEmpty()) return DailyTotals()

        val total = dailyTotals(logs)
        val days = loggedDays.size.toDouble()

        return DailyTotals(
            calories = total.calories / days,
            proteinG = total.proteinG / days,
            carbsG = total.carbsG / days,
            fatG = total.fatG / days
        )
    }

    /** Meal-by-meal average intake across the range (only over logged days). */
    fun averageMealBreakdown(
        logs: List<FoodLog>,
        dateRange: ClosedRange<LocalDate>
    ): MealBreakdown {
        if (logs.isEmpty()) return MealBreakdown()
        val byDate = logs.groupBy { it.logDate }
        val loggedDays = byDate.keys.filter { it in dateRange }
        if (loggedDays.isEmpty()) return MealBreakdown()

        val total = mealBreakdown(logs)
        val days = loggedDays.size.toDouble()

        return MealBreakdown(
            breakfast = total.breakfast / days,
            lunch = total.lunch / days,
            dinner = total.dinner / days,
            snack = total.snack / days
        )
    }

    /** Overall nutrition progress as % of daily calorie goal. Clamped 0..100. */
    fun calorieProgressPercent(averageDaily: Double, goal: Int): Int {
        if (goal <= 0) return 0
        return ((averageDaily / goal) * 100.0).roundToInt().coerceIn(0, 100)
    }

    /** Meal-specific percent of target. */
    fun mealPercent(actual: Double, target: Int): Int {
        if (target <= 0) return 0
        return ((actual / target) * 100.0).roundToInt().coerceIn(0, 100)
    }

    /** Macro percent of target. */
    fun macroPercent(actual: Double, target: Int): Int {
        if (target <= 0) return 0
        return ((actual / target) * 100.0).roundToInt().coerceIn(0, 100)
    }

    /** Convenience: dashboard "today" snapshot. */
    fun dashboardSnapshot(
        todayLogs: List<FoodLog>,
        mealTargets: MealTargets,
        dailyCalorieTarget: Int,
        macroTargets: MacroTargets
    ): DashboardNutrition {
        val totals = dailyTotals(todayLogs)
        val meals = mealBreakdown(todayLogs)

        return DashboardNutrition(
            consumedCalories = totals.calories,
            remainingCalories = (dailyCalorieTarget - totals.calories).coerceAtLeast(0.0),
            consumedProtein = totals.proteinG,
            consumedCarbs = totals.carbsG,
            consumedFat = totals.fatG,
            proteinTargetG = macroTargets.proteinG,
            carbsTargetG = macroTargets.carbsG,
            fatTargetG = macroTargets.fatG,
            breakfastActual = meals.breakfast,
            breakfastTarget = mealTargets.breakfastKcal,
            lunchActual = meals.lunch,
            lunchTarget = mealTargets.lunchKcal,
            dinnerActual = meals.dinner,
            dinnerTarget = mealTargets.dinnerKcal,
            snackActual = meals.snack,
            snackTarget = mealTargets.snackKcal
        )
    }

    data class DashboardNutrition(
        val consumedCalories: Double,
        val remainingCalories: Double,
        val consumedProtein: Double,
        val consumedCarbs: Double,
        val consumedFat: Double,
        val proteinTargetG: Int,
        val carbsTargetG: Int,
        val fatTargetG: Int,
        val breakfastActual: Double, val breakfastTarget: Int,
        val lunchActual: Double, val lunchTarget: Int,
        val dinnerActual: Double, val dinnerTarget: Int,
        val snackActual: Double, val snackTarget: Int
    )
}