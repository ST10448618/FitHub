package com.example.fithub.domain.calculator

import com.example.fithub.domain.model.WorkoutCategory
import com.example.fithub.domain.model.WorkoutSession
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import kotlin.math.roundToInt

/**
 * Aggregations for the Workouts Overview and Dashboard workout sections.
 * Only real completed sessions count.
 */
object WorkoutProgressCalculator {

    // ---- WEEKLY ----

    fun startOfWeek(date: LocalDate): LocalDate =
        date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    fun endOfWeek(date: LocalDate): LocalDate =
        date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

    /** Completed sessions in the week containing [date]. */
    fun sessionsThisWeek(
        sessions: List<WorkoutSession>,
        date: LocalDate
    ): List<WorkoutSession> {
        val start = startOfWeek(date)
        val end = endOfWeek(date)
        return sessions.filter {
            it.logDate >= start && it.logDate <= end
        }
    }

    fun weeklyProgress(
        sessions: List<WorkoutSession>,
        date: LocalDate,
        weeklyTarget: Int
    ): Int {
        if (weeklyTarget <= 0) return 0
        val count = sessionsThisWeek(sessions, date).size
        return ((count.toDouble() / weeklyTarget) * 100.0)
            .roundToInt()
            .coerceIn(0, 100)
    }

    // ---- MONTHLY ----

    fun sessionsThisMonth(
        sessions: List<WorkoutSession>,
        monthAnchor: LocalDate
    ): List<WorkoutSession> {
        return sessions.filter {
            it.logDate.year == monthAnchor.year &&
                    it.logDate.month == monthAnchor.month
        }
    }

    fun monthlyActivity(sessions: List<WorkoutSession>, monthAnchor: LocalDate): Int =
        sessionsThisMonth(sessions, monthAnchor).sumOf { it.estimatedActivityKcal }

    fun monthlyActivityProgress(
        sessions: List<WorkoutSession>,
        monthAnchor: LocalDate,
        monthlyTarget: Int
    ): Int {
        if (monthlyTarget <= 0) return 0
        val total = monthlyActivity(sessions, monthAnchor)
        return ((total.toDouble() / monthlyTarget) * 100.0)
            .roundToInt()
            .coerceIn(0, 100)
    }

    // ---- WEEK VIEW (days Mon..Sun) ----

    data class DailyActivity(val date: LocalDate, val activityKcal: Int)

    fun weekGraph(sessions: List<WorkoutSession>, anchor: LocalDate): List<DailyActivity> {
        val start = startOfWeek(anchor)
        return (0..6).map { offset ->
            val day = start.plusDays(offset.toLong())
            val activity = sessions.filter { it.logDate == day }
                .sumOf { it.estimatedActivityKcal }
            DailyActivity(day, activity)
        }
    }

    // ---- MONTH VIEW (weeks 1..N of month) ----

    data class WeekActivity(val label: String, val activityKcal: Int)

    fun monthGraph(sessions: List<WorkoutSession>, monthAnchor: LocalDate): List<WeekActivity> {
        val firstOfMonth = monthAnchor.withDayOfMonth(1)
        val lastOfMonth = monthAnchor.with(TemporalAdjusters.lastDayOfMonth())
        val monthSessions = sessionsThisMonth(sessions, monthAnchor)

        val weeks = mutableListOf<WeekActivity>()
        var cursor = firstOfMonth
        var weekIndex = 1
        while (cursor <= lastOfMonth) {
            val weekEnd = minOf(cursor.plusDays(6), lastOfMonth)
            val activity = monthSessions
                .filter { it.logDate >= cursor && it.logDate <= weekEnd }
                .sumOf { it.estimatedActivityKcal }
            weeks.add(WeekActivity("Wk $weekIndex", activity))
            cursor = weekEnd.plusDays(1)
            weekIndex++
        }
        return weeks
    }

    // ---- YEAR VIEW (Jan..Dec) ----

    data class MonthActivity(val month: Int, val label: String, val activityKcal: Int)

    fun yearGraph(sessions: List<WorkoutSession>, year: Int): List<MonthActivity> {
        val labels = listOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
        return (1..12).map { month ->
            val activity = sessions
                .filter { it.logDate.year == year && it.logDate.monthValue == month }
                .sumOf { it.estimatedActivityKcal }
            MonthActivity(month, labels[month - 1], activity)
        }
    }

    // ---- HABITS ----

    data class Habits(
        val totalSessions: Int,
        val totalActiveMinutes: Int,
        val averageSessionMinutes: Int,
        val longestSessionMinutes: Int
    )

    fun habits(sessions: List<WorkoutSession>): Habits {
        if (sessions.isEmpty()) return Habits(0, 0, 0, 0)
        val totalMinutes = sessions.sumOf { it.durationMinutes }
        return Habits(
            totalSessions = sessions.size,
            totalActiveMinutes = totalMinutes,
            averageSessionMinutes = (totalMinutes.toDouble() / sessions.size).roundToInt(),
            longestSessionMinutes = sessions.maxOf { it.durationMinutes }
        )
    }

    // ---- CATEGORY BREAKDOWN ----

    data class CategoryCount(val category: WorkoutCategory, val count: Int)

    fun categoryBreakdown(sessions: List<WorkoutSession>): List<CategoryCount> {
        return sessions.groupBy { it.workoutCategory }
            .map { (cat, list) -> CategoryCount(cat, list.size) }
            .sortedByDescending { it.count }
    }
}