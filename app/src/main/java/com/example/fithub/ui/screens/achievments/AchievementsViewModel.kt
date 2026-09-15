package com.example.fithub.ui.screens.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fithub.core.Resource
import com.example.fithub.core.ServiceLocator
import com.example.fithub.core.SessionManager
import com.example.fithub.domain.catalog.AchievementCatalog
import com.example.fithub.domain.model.Achievement
import com.example.fithub.domain.model.AchievementCategory
import com.example.fithub.domain.model.UserAchievement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

data class AchievementCard(
    val definition: Achievement,
    val userState: UserAchievement
)

data class AchievementsUiState(
    val isLoading: Boolean = true,
    val lifetimeParticles: Int = 0,
    val currentLevel: Int = 1,
    val particlesToNextLevel: Int = 0,
    val levelProgress: Float = 0f,
    val nutritionAchievements: List<AchievementCard> = emptyList(),
    val workoutAchievements: List<AchievementCard> = emptyList(),
    val errorMessage: String? = null
)

class AchievementsViewModel : ViewModel() {

    private val uid = SessionManager.currentUserId ?: ""

    private val _uiState = MutableStateFlow(AchievementsUiState())
    val uiState: StateFlow<AchievementsUiState> = _uiState.asStateFlow()

    private val rewardRepo = ServiceLocator.rewardRepository
    private val foodLogRepo = ServiceLocator.foodLogRepository
    private val sessionRepo = ServiceLocator.workoutSessionRepository
    private val nutritionRepo = ServiceLocator.nutritionGoalsRepository
    private val workoutGoalsRepo = ServiceLocator.workoutGoalsRepository

    init {
        observeAndEvaluate()
    }

    private fun observeAndEvaluate() {
        viewModelScope.launch {
            // Evaluate achievements whenever state changes
            rewardRepo.observeAchievements(uid).collect { userList ->
                // Re-evaluate against data
                evaluateAchievements()
                val map = userList.associateBy { it.achievementId }

                val cards = AchievementCatalog.ALL.map { def ->
                    AchievementCard(
                        definition = def,
                        userState = map[def.id] ?: UserAchievement(
                            achievementId = def.id,
                            userId = uid
                        )
                    )
                }
                val balance = rewardRepo.observeBalance(uid)
                // Balance is collected separately below
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        nutritionAchievements = cards.filter {
                            it.definition.category == AchievementCategory.NUTRITION
                        },
                        workoutAchievements = cards.filter {
                            it.definition.category == AchievementCategory.WORKOUT
                        }
                    )
                }
            }
        }

        viewModelScope.launch {
            rewardRepo.observeBalance(uid).collect { balance ->
                val lifetime = balance?.lifetimeEarned ?: 0
                _uiState.update {
                    it.copy(
                        lifetimeParticles = lifetime,
                        currentLevel = AchievementCatalog.levelFromLifetimeParticles(lifetime),
                        particlesToNextLevel = AchievementCatalog.particlesToNextLevel(lifetime),
                        levelProgress = AchievementCatalog.progressWithinLevel(lifetime)
                    )
                }
            }
        }
    }

    /** Run through every achievement, update its progress. */
    private suspend fun evaluateAchievements() {
        val today = LocalDate.now()
        val monthStart = today.withDayOfMonth(1)
        val monthEnd = today.withDayOfMonth(today.lengthOfMonth())

        val logsAll = foodLogRepo.getBetween(uid, today.minusDays(60), today)
        val sessionsAll = sessionRepo.getBetween(uid, today.minusDays(90), today)
        val nutritionGoals = nutritionRepo.getCurrent(uid)
        val workoutGoals = workoutGoalsRepo.get(uid)

        // FIRST_LOG
        updateIfNeeded(
            id = AchievementCatalog.FIRST_LOG,
            progress = if (logsAll.isNotEmpty()) 1 else 0,
            unlocked = logsAll.isNotEmpty()
        )

        // HIT_GOAL: any day where total consumed >= user daily calories
        val dailyTarget = nutritionGoals?.userDailyCalories ?: 0
        val hitGoal = if (dailyTarget > 0) {
            logsAll.groupBy { it.logDate }.any { (_, dayLogs) ->
                dayLogs.sumOf { it.calories } >= dailyTarget
            }
        } else false
        updateIfNeeded(
            id = AchievementCatalog.HIT_GOAL,
            progress = if (hitGoal) 1 else 0,
            unlocked = hitGoal
        )

        // PERFECT_WEEK: 7 consecutive days with at least one log
        val uniqueDays = logsAll.map { it.logDate }.distinct().sorted()
        var longestStreak = 0
        var current = 0
        var prev: LocalDate? = null
        for (d in uniqueDays) {
            current = if (prev != null && prev!!.plusDays(1) == d) current + 1 else 1
            longestStreak = maxOf(longestStreak, current)
            prev = d
        }
        updateIfNeeded(
            id = AchievementCatalog.PERFECT_WEEK,
            progress = longestStreak.coerceAtMost(7),
            unlocked = longestStreak >= 7
        )

        // MONTHLY_STREAK: >= 20 days this month with logs
        val daysThisMonth = logsAll
            .filter { it.logDate >= monthStart && it.logDate <= monthEnd }
            .map { it.logDate }
            .distinct()
            .size
        updateIfNeeded(
            id = AchievementCatalog.MONTHLY_STREAK,
            progress = daysThisMonth.coerceAtMost(20),
            unlocked = daysThisMonth >= 20
        )

        // FIRST_SESSION
        updateIfNeeded(
            id = AchievementCatalog.FIRST_SESSION,
            progress = if (sessionsAll.isNotEmpty()) 1 else 0,
            unlocked = sessionsAll.isNotEmpty()
        )

        // WEEKLY_GOAL: any week with sessionsPerWeek sessions
        val weekTarget = workoutGoals?.sessionsPerWeek ?: 4
        val byWeek = sessionsAll.groupBy {
            it.logDate.minusDays((it.logDate.dayOfWeek.value - 1).toLong())
        }
        val hitWeekly = byWeek.values.any { it.size >= weekTarget }
        updateIfNeeded(
            id = AchievementCatalog.WEEKLY_GOAL,
            progress = if (hitWeekly) 1 else 0,
            unlocked = hitWeekly
        )

        // WORKOUT_STREAK: 4 consecutive weeks with >= 1 session
        val weeksWithSession = byWeek.keys.sorted()
        var best = 0; var cur = 0; var prevW: LocalDate? = null
        for (w in weeksWithSession) {
            cur = if (prevW != null && prevW!!.plusWeeks(1) == w) cur + 1 else 1
            best = maxOf(best, cur)
            prevW = w
        }
        updateIfNeeded(
            id = AchievementCatalog.WORKOUT_STREAK,
            progress = best.coerceAtMost(4),
            unlocked = best >= 4
        )

        // MONTHLY_MILESTONE: any month where activity >= monthly target
        val monthlyTarget = workoutGoals?.monthlyActivityGoalKcal ?: 10000
        val hitMonthly = sessionsAll
            .groupBy { it.logDate.withDayOfMonth(1) }
            .values
            .any { list -> list.sumOf { it.estimatedActivityKcal } >= monthlyTarget }
        updateIfNeeded(
            id = AchievementCatalog.MONTHLY_MILESTONE,
            progress = if (hitMonthly) 1 else 0,
            unlocked = hitMonthly
        )
    }

    private suspend fun updateIfNeeded(id: String, progress: Int, unlocked: Boolean) {
        val existing = rewardRepo.observeAchievements(uid).let { _ -> null } // placeholder — we fetch directly below
        // Simpler: read from current state
        val existingCard = _uiState.value.let {
            it.nutritionAchievements + it.workoutAchievements
        }.firstOrNull { it.definition.id == id }?.userState

        // If we haven't loaded yet, skip — the observe loop will re-evaluate.
        val currentUnlocked = existingCard?.isUnlocked ?: false
        val currentProgress = existingCard?.progress ?: 0

        if (unlocked == currentUnlocked && progress == currentProgress) return

        val updated = UserAchievement(
            achievementId = id,
            userId = uid,
            progress = progress,
            isUnlocked = unlocked,
            isClaimed = existingCard?.isClaimed ?: false,
            unlockedAt = if (unlocked && existingCard?.unlockedAt == null) LocalDateTime.now()
            else existingCard?.unlockedAt,
            claimedAt = existingCard?.claimedAt
        )
        rewardRepo.upsertAchievement(uid, updated)
    }

    fun claim(achievementId: String) {
        viewModelScope.launch {
            val def = AchievementCatalog.byId(achievementId) ?: return@launch
            when (val result = rewardRepo.claimAchievement(uid, achievementId)) {
                is Resource.Success -> {
                    rewardRepo.addParticles(uid, def.rewardParticles)
                }
                is Resource.Error -> _uiState.update {
                    it.copy(errorMessage = result.message)
                }
                Resource.Loading -> Unit
            }
        }
    }
}