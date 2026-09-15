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
        // 1. Evaluate once, up-front
        viewModelScope.launch {
            evaluateAchievements()
        }
        // 2. Then observe (pure render, no side effects)
        observeCards()
        observeBalance()
    }

    private fun observeCards() {
        viewModelScope.launch {
            rewardRepo.observeAchievements(uid).collect { userList ->
                renderCards(userList)
            }
        }
    }

    private fun observeBalance() {
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

    private fun renderCards(userList: List<UserAchievement>) {
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

    /** Evaluate all achievements against real data. Called once on init. */
    private suspend fun evaluateAchievements() {
        val today = LocalDate.now()
        val monthStart = today.withDayOfMonth(1)
        val monthEnd = today.withDayOfMonth(today.lengthOfMonth())

        val logsAll = foodLogRepo.getBetween(uid, today.minusDays(60), today)
        val sessionsAll = sessionRepo.getBetween(uid, today.minusDays(90), today)
        val nutritionGoals = nutritionRepo.getCurrent(uid)
        val workoutGoals = workoutGoalsRepo.get(uid)

        // ---- Fetch existing achievement states ONCE from DB ----
        val existingMap = rewardRepo.getAchievementsForUser(uid)
            .associateBy { it.achievementId }

        // FIRST_LOG
        upsertProgress(
            id = AchievementCatalog.FIRST_LOG,
            progress = if (logsAll.isNotEmpty()) 1 else 0,
            unlocked = logsAll.isNotEmpty(),
            existing = existingMap
        )

        // HIT_GOAL
        val dailyTarget = nutritionGoals?.userDailyCalories ?: 0
        val hitGoal = if (dailyTarget > 0) {
            logsAll.groupBy { it.logDate }.any { (_, dayLogs) ->
                dayLogs.sumOf { it.calories } >= dailyTarget
            }
        } else false
        upsertProgress(
            id = AchievementCatalog.HIT_GOAL,
            progress = if (hitGoal) 1 else 0,
            unlocked = hitGoal,
            existing = existingMap
        )

        // PERFECT_WEEK
        val uniqueDays = logsAll.map { it.logDate }.distinct().sorted()
        var longestStreak = 0
        var current = 0
        var prev: LocalDate? = null
        for (d in uniqueDays) {
            current = if (prev != null && prev!!.plusDays(1) == d) current + 1 else 1
            longestStreak = maxOf(longestStreak, current)
            prev = d
        }
        upsertProgress(
            id = AchievementCatalog.PERFECT_WEEK,
            progress = longestStreak.coerceAtMost(7),
            unlocked = longestStreak >= 7,
            existing = existingMap
        )

        // MONTHLY_STREAK
        val daysThisMonth = logsAll
            .filter { it.logDate >= monthStart && it.logDate <= monthEnd }
            .map { it.logDate }
            .distinct()
            .size
        upsertProgress(
            id = AchievementCatalog.MONTHLY_STREAK,
            progress = daysThisMonth.coerceAtMost(20),
            unlocked = daysThisMonth >= 20,
            existing = existingMap
        )

        // FIRST_SESSION
        upsertProgress(
            id = AchievementCatalog.FIRST_SESSION,
            progress = if (sessionsAll.isNotEmpty()) 1 else 0,
            unlocked = sessionsAll.isNotEmpty(),
            existing = existingMap
        )

        // WEEKLY_GOAL
        val weekTarget = workoutGoals?.sessionsPerWeek ?: 4
        val byWeek = sessionsAll.groupBy {
            it.logDate.minusDays((it.logDate.dayOfWeek.value - 1).toLong())
        }
        val hitWeekly = byWeek.values.any { it.size >= weekTarget }
        upsertProgress(
            id = AchievementCatalog.WEEKLY_GOAL,
            progress = if (hitWeekly) 1 else 0,
            unlocked = hitWeekly,
            existing = existingMap
        )

        // WORKOUT_STREAK
        val weeksWithSession = byWeek.keys.sorted()
        var best = 0; var cur = 0; var prevW: LocalDate? = null
        for (w in weeksWithSession) {
            cur = if (prevW != null && prevW!!.plusWeeks(1) == w) cur + 1 else 1
            best = maxOf(best, cur)
            prevW = w
        }
        upsertProgress(
            id = AchievementCatalog.WORKOUT_STREAK,
            progress = best.coerceAtMost(4),
            unlocked = best >= 4,
            existing = existingMap
        )

        // MONTHLY_MILESTONE
        val monthlyTarget = workoutGoals?.monthlyActivityGoalKcal ?: 10000
        val hitMonthly = sessionsAll
            .groupBy { it.logDate.withDayOfMonth(1) }
            .values
            .any { list -> list.sumOf { it.estimatedActivityKcal } >= monthlyTarget }
        upsertProgress(
            id = AchievementCatalog.MONTHLY_MILESTONE,
            progress = if (hitMonthly) 1 else 0,
            unlocked = hitMonthly,
            existing = existingMap
        )
    }

    private suspend fun upsertProgress(
        id: String,
        progress: Int,
        unlocked: Boolean,
        existing: Map<String, UserAchievement>
    ) {
        val current = existing[id]

        // No change → don't touch the row (protects claim state from churn)
        val sameUnlocked = unlocked == (current?.isUnlocked ?: false)
        val sameProgress = progress == (current?.progress ?: 0)
        if (sameUnlocked && sameProgress) return

        val updated = UserAchievement(
            achievementId = id,
            userId = uid,
            progress = progress,
            isUnlocked = unlocked,
            isClaimed = current?.isClaimed ?: false,       // ← preserved from DB
            unlockedAt = if (unlocked && current?.unlockedAt == null)
                LocalDateTime.now() else current?.unlockedAt,
            claimedAt = current?.claimedAt                  // ← preserved from DB
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