package com.example.fithub.ui.screens.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fithub.R
import com.example.fithub.core.LogTags
import com.example.fithub.core.ServiceLocator
import com.example.fithub.core.SessionManager
import com.example.fithub.core.notifications.NotificationHelper
import com.example.fithub.domain.calculator.NutritionProgressCalculator
import com.example.fithub.domain.calculator.StreakCalculator
import com.example.fithub.domain.calculator.WeeklyChallengeCalculator
import com.example.fithub.domain.calculator.WorkoutProgressCalculator
import com.example.fithub.domain.catalog.AchievementCatalog
import com.example.fithub.domain.model.UserAchievement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class DashboardViewModel : ViewModel() {

    private val uid: String = SessionManager.currentUserId ?: ""

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val userRepo = ServiceLocator.userRepository
    private val goalRepo = ServiceLocator.goalRepository
    private val weightRepo = ServiceLocator.weightRepository
    private val nutritionGoalsRepo = ServiceLocator.nutritionGoalsRepository
    private val workoutGoalsRepo = ServiceLocator.workoutGoalsRepository
    private val foodLogRepo = ServiceLocator.foodLogRepository
    private val sessionRepo = ServiceLocator.workoutSessionRepository
    private val planRepo = ServiceLocator.workoutPlanRepository
    private val rewardRepo = ServiceLocator.rewardRepository
    private val prefs = ServiceLocator.preferencesManager

    init {
        observeProfile()
        observeGoalsAndWeight()
        observeNutrition()
        observeWorkouts()
        loadRecommendations()
        observeWeeklyCalories()
        observeStreakAndChallenge()
        loadWeeklyInsights()
    }

    private fun observeWeeklyCalories() {
        val today = LocalDate.now()
        val start = today.minusDays(6)

        viewModelScope.launch {
            foodLogRepo.observeRecent(uid, limit = 200).collect { logs ->
                val goals = nutritionGoalsRepo.getCurrent(uid)
                val target = goals?.userDailyCalories ?: 0
                val byDay = logs
                    .filter { it.logDate in start..today }
                    .groupBy { it.logDate }
                    .mapValues { (_, dayLogs) -> dayLogs.sumOf { it.calories }.toInt() }
                val bars = (0..6).map { offset ->
                    val date = start.plusDays(offset.toLong())
                    val cals = byDay[date] ?: 0
                    DailyCalorieBar(
                        date = date,
                        calories = cals,
                        isOverTarget = target > 0 && cals > target
                    )
                }
                _uiState.update {
                    it.copy(weeklyCalories = bars, dailyCalorieTarget = target)
                }
            }
        }
    }

    private fun observeProfile() {
        viewModelScope.launch {
            userRepo.observeProfile(uid).collect { profile ->
                _uiState.update { it.copy(profile = profile, isLoading = false) }
            }
        }
    }

    private fun observeGoalsAndWeight() {
        viewModelScope.launch {
            goalRepo.observeCurrent(uid).collect { goal ->
                _uiState.update { it.copy(targetWeightKg = goal?.targetWeightKg) }
            }
        }
        viewModelScope.launch {
            weightRepo.observeLatest(uid).collect { w ->
                _uiState.update { it.copy(currentWeightKg = w?.weightKg) }
            }
        }
    }

    private fun observeNutrition() {
        val today = LocalDate.now()
        viewModelScope.launch {
            var goals = nutritionGoalsRepo.getCurrent(uid)
            if (goals == null) {
                val profile = userRepo.getProfile(uid)
                val goal = goalRepo.getCurrent(uid)
                val direction = goal?.direction
                    ?: com.example.fithub.domain.model.GoalDirection.MAINTAIN
                val recommended = if (profile != null) {
                    com.example.fithub.domain.calculator.CalorieEngine.calculate(
                        com.example.fithub.domain.calculator.CalorieEngine.Input(
                            age = profile.age,
                            gender = profile.gender,
                            heightCm = profile.heightCm,
                            weightKg = profile.currentWeightKg,
                            activityLevel = profile.activityLevel,
                            goalDirection = direction
                        )
                    ).recommendedDailyCalories
                } else 2000
                goals = com.example.fithub.domain.model.NutritionGoals(
                    id = com.example.fithub.util.IdGenerator.newId(),
                    userId = uid,
                    recommendedDailyCalories = recommended,
                    userDailyCalories = recommended,
                    mealTargets = com.example.fithub.domain.calculator.MacroCalculator
                        .starterMealTargets(recommended),
                    macroTargets = com.example.fithub.domain.calculator.MacroCalculator
                        .starterMacros(recommended)
                )
                nutritionGoalsRepo.save(goals)
            }
            val finalGoals = goals!!
            foodLogRepo.observeByDate(uid, today).collect { logs ->
                val snapshot = NutritionProgressCalculator.dashboardSnapshot(
                    todayLogs = logs,
                    mealTargets = finalGoals.mealTargets,
                    dailyCalorieTarget = finalGoals.userDailyCalories,
                    macroTargets = finalGoals.macroTargets
                )
                _uiState.update { it.copy(nutritionSnapshot = snapshot) }
            }
        }
    }

    private fun observeWorkouts() {
        val today = LocalDate.now()
        viewModelScope.launch {
            sessionRepo.observeCompleted(uid).collect { sessions ->
                val workoutGoals = workoutGoalsRepo.get(uid)
                val target = workoutGoals?.sessionsPerWeek ?: 4
                val thisWeek = WorkoutProgressCalculator.sessionsThisWeek(sessions, today)
                _uiState.update {
                    it.copy(
                        recentSessions = sessions.take(5),
                        weeklyWorkoutCompleted = thisWeek.size,
                        weeklyWorkoutTarget = target,
                        weeklyWorkoutProgressPercent = WorkoutProgressCalculator
                            .weeklyProgress(sessions, today, target)
                    )
                }
            }
        }
    }

    private fun loadRecommendations() {
        viewModelScope.launch {
            planRepo.observeVerified().collect { plans ->
                _uiState.update { it.copy(recommendedPlans = plans.take(4)) }
            }
        }
    }

    /**
     * Phase 6 (UDF 5): combines food-log dates + completed-session dates, feeds
     * both the streak and the weekly challenge calculators, and — the first
     * time a milestone (3/7/14/30) is observed — fires a notification and
     * awards particles + the matching achievement.
     */
    private fun observeStreakAndChallenge() {
        viewModelScope.launch {
            combine(
                foodLogRepo.observeRecent(uid, limit = 200),
                sessionRepo.observeCompleted(uid)
            ) { logs, sessions ->
                val foodDates = logs.map { it.logDate }
                val sessionDates = sessions.map { it.logDate }
                (foodDates + sessionDates).toSet() to sessions.map { it.logDate }
            }.collect { (allDates, sessionDates) ->
                val today = LocalDate.now()
                val streak = StreakCalculator.calculate(allDates, today)
                val challengeTarget = workoutGoalsRepo.get(uid)?.sessionsPerWeek ?: 4
                val challenge = WeeklyChallengeCalculator.calculate(
                    completedDates = sessionDates,
                    today = today,
                    targetSessionsPerWeek = challengeTarget
                )
                _uiState.update {
                    it.copy(
                        currentStreak = streak.current,
                        longestStreak = streak.longest,
                        todayDone = streak.todayDone,
                        streakAtRisk = streak.atRisk,
                        challengeCompleted = challenge.completed,
                        challengeTarget = challenge.target
                    )
                }
                checkStreakMilestones(streak.current, streak.longest)
            }
        }
    }

    private suspend fun checkStreakMilestones(current: Int, longest: Int) {
        val alreadyHit = prefs.streakMilestonesHit.first()
        val milestones = listOf(3, 7, 14, 30)
        for (m in milestones) {
            val reachedNow = current >= m || longest >= m
            if (reachedNow && m.toString() !in alreadyHit) {
                awardMilestone(m)
            }
        }
    }

    private suspend fun awardMilestone(days: Int) {
        prefs.markStreakMilestoneHit(days.toString())
        // Notification
        runCatching {
            NotificationHelper.showStreakMilestone(ServiceLocator.appContext(), days)
        }.onFailure { Log.e(LogTags.STREAK, "Milestone notification failed", it) }

        // Achievements — only the 3-day and 30-day have achievements.
        val achievementId = when (days) {
            3 -> AchievementCatalog.STREAK_3
            30 -> AchievementCatalog.STREAK_30
            else -> null
        }
        if (achievementId != null) {
            val existing = rewardRepo.getAchievementsForUser(uid)
                .firstOrNull { it.achievementId == achievementId }
            if (existing == null || !existing.isUnlocked) {
                rewardRepo.upsertAchievement(
                    uid,
                    UserAchievement(
                        achievementId = achievementId,
                        userId = uid,
                        progress = days,
                        isUnlocked = true,
                        isClaimed = false,
                        unlockedAt = java.time.LocalDateTime.now(),
                        claimedAt = null
                    )
                )
                Log.i(LogTags.STREAK, "Achievement unlocked: $achievementId at $days days")
            }
        }
    }

    /**
     * Phase 6B: fetches the server-side weekly insights once per ViewModel
     * lifetime. Server failure → EMPTY → the card silently hides. The
     * dashboard's own Room-backed cards still render, so no functional
     * regression when offline.
     */
    private fun loadWeeklyInsights() {
        viewModelScope.launch {
            _uiState.update { it.copy(insightsLoading = true) }
            val insights = ServiceLocator.insightsRepository.fetchWeeklyInsights(days = 7)
            _uiState.update {
                it.copy(
                    insights = if (insights === com.example.fithub.domain.model.WeeklyInsights.EMPTY) null else insights,
                    insightsLoading = false,
                )
            }
        }
    }
}