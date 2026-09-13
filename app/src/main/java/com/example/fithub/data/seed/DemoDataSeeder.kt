package com.example.fithub.data.seed

import com.example.fithub.data.local.FitHubDatabase
import com.example.fithub.data.local.mapper.*
import com.example.fithub.domain.model.*
import com.example.fithub.util.IdGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime

import com.example.fithub.data.local.entity.CheckpointScheduleEntity
import com.example.fithub.data.local.entity.FoodLogEntity
import com.example.fithub.data.local.entity.GoalEntity
import com.example.fithub.data.local.entity.NutritionGoalsEntity
import com.example.fithub.data.local.entity.UserProfileEntity
import com.example.fithub.data.local.entity.WeightEntryEntity
import com.example.fithub.data.local.entity.WorkoutGoalsEntity
import com.example.fithub.data.local.entity.WorkoutSessionEntity

/**
 * Temporary demo data.
 * Track A will remove this once real authentication is wired.
 */
object DemoDataSeeder {

    const val ENABLED = true
    private const val DEMO_UID = "demo_user"

    suspend fun seedIfNeeded(db: FitHubDatabase) = withContext(Dispatchers.IO) {
        if (!ENABLED) return@withContext
        if (db.userProfileDao().getById(DEMO_UID) != null) return@withContext
        seedUser(db)
        seedGoals(db)
        seedNutritionGoals(db)
        seedWorkoutGoals(db)
        seedCheckpointSchedule(db)
        seedWeightEntries(db)
        seedFoodLogs(db)
        seedWorkoutSessions(db)
    }

    private suspend fun seedUser(db: FitHubDatabase) {
        db.userProfileDao().upsert(
            UserProfileEntity(
                id = DEMO_UID,
                firstName = "Alex",
                surname = "Hunter",
                email = "alex@fithub.com",
                gender = Gender.MALE,
                age = 20,
                heightCm = 169.0,
                startingWeightKg = 62.0,
                currentWeightKg = 67.0,
                activityLevel = ActivityLevel.MODERATELY_ACTIVE,
                profileImageUrl = null,
                biometricEnabled = false,
                onboardingComplete = true,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )
    }

    private suspend fun seedGoals(db: FitHubDatabase) {
        db.goalDao().upsert(
            GoalEntity(
                id = "goal_demo",
                userId = DEMO_UID,
                currentWeightKgAtGoalSet = 62.0,
                targetWeightKg = 80.0,
                direction = GoalDirection.GAIN,
                effectiveDate = LocalDate.now(),
                createdAt = LocalDateTime.now()
            )
        )
    }

    private suspend fun seedNutritionGoals(db: FitHubDatabase) {
        db.nutritionGoalsDao().upsert(
            NutritionGoalsEntity(
                id = "nutrition_demo",
                userId = DEMO_UID,
                recommendedDailyCalories = 3000,
                userDailyCalories = 3000,
                breakfastKcal = 720,
                lunchKcal = 780,
                dinnerKcal = 900,
                snackKcal = 600,
                proteinG = 150,
                carbsG = 375,
                fatG = 100,
                effectiveDate = LocalDate.now(),
                updatedAt = LocalDateTime.now()
            )
        )
    }

    private suspend fun seedWorkoutGoals(db: FitHubDatabase) {
        db.workoutGoalsDao().upsert(
            WorkoutGoalsEntity(
                id = "workout_goals_demo",
                userId = DEMO_UID,
                sessionsPerWeek = 4,
                monthlyActivityGoalKcal = 10000,
                updatedAt = LocalDateTime.now()
            )
        )
    }

    private suspend fun seedCheckpointSchedule(db: FitHubDatabase) {
        db.checkpointScheduleDao().upsert(
            CheckpointScheduleEntity(
                id = "checkpoint_demo",
                userId = DEMO_UID,
                frequencyDays = 14,
                nextCheckpointDate = LocalDate.now().plusDays(10),
                remindersEnabled = true,
                updatedAt = LocalDateTime.now()
            )
        )
    }

    private suspend fun seedWeightEntries(db: FitHubDatabase) {
        val entries = listOf(
            WeightEntryEntity(
                IdGenerator.newId(), DEMO_UID, 62.0,
                LocalDate.now().minusDays(42), null, LocalDateTime.now()
            ),
            WeightEntryEntity(
                IdGenerator.newId(), DEMO_UID, 63.5,
                LocalDate.now().minusDays(28), null, LocalDateTime.now()
            ),
            WeightEntryEntity(
                IdGenerator.newId(), DEMO_UID, 65.0,
                LocalDate.now().minusDays(14), null, LocalDateTime.now()
            ),
            WeightEntryEntity(
                IdGenerator.newId(), DEMO_UID, 67.0,
                LocalDate.now(), null, LocalDateTime.now()
            )
        )
        entries.forEach { db.weightEntryDao().upsert(it) }
    }

    private suspend fun seedFoodLogs(db: FitHubDatabase) {
        val today = LocalDate.now()
        val logs = listOf(
            demoFoodLog(MealType.BREAKFAST, "Bacon and Egg Supreme", 237.0, today, 8),
            demoFoodLog(MealType.LUNCH, "Thick Crust Cheese Pizza", 137.0, today, 13),
            demoFoodLog(MealType.DINNER, "Fillet Steak (300g)", 337.0, today, 19),
            demoFoodLog(MealType.SNACK, "Strawberry Shortcake", 107.0, today, 16)
        )
        logs.forEach { db.foodLogDao().upsert(it) }
    }

    private suspend fun seedWorkoutSessions(db: FitHubDatabase) {
        val today = LocalDate.now()
        // Two sessions this week (Mon & Wed) to show progress
        val monday = today.minusDays(((today.dayOfWeek.value - 1).toLong()))
        val sessions = listOf(
            WorkoutSessionEntity(
                id = IdGenerator.newId(),
                userId = DEMO_UID,
                workoutPlanId = "plan_home_workout_3b",
                workoutName = "Home Workout 3B",
                workoutCategory = WorkoutCategory.GENERAL_FITNESS,
                durationMinutes = 45,
                estimatedActivityKcal = 320,
                status = SessionStatus.COMPLETED,
                startedAt = LocalDateTime.now().minusDays(2),
                completedAt = LocalDateTime.now().minusDays(2),
                logDate = monday
            ),
            WorkoutSessionEntity(
                id = IdGenerator.newId(),
                userId = DEMO_UID,
                workoutPlanId = "plan_sweat_fest",
                workoutName = "Sweat Fest",
                workoutCategory = WorkoutCategory.CARDIO,
                durationMinutes = 40,
                estimatedActivityKcal = 300,
                status = SessionStatus.COMPLETED,
                startedAt = LocalDateTime.now().minusDays(1),
                completedAt = LocalDateTime.now().minusDays(1),
                logDate = monday.plusDays(2)
            )
        )
        sessions.forEach { db.workoutSessionDao().upsert(it) }
    }

    private fun demoFoodLog(
        meal: MealType,
        name: String,
        calories: Double,
        date: LocalDate,
        hour: Int
    ) = FoodLogEntity(
        id = IdGenerator.newId(),
        userId = DEMO_UID,
        foodId = null,
        foodName = name,
        brandName = null,
        imageUrl = null,
        mealType = meal,
        source = FoodSource.MANUAL,
        barcode = null,
        portionSize = 1.0,
        portionUnit = "serving",
        portionGrams = 200.0,
        calories = calories,
        proteinG = calories * 0.20 / 4.0,
        carbsG = calories * 0.50 / 4.0,
        fatG = calories * 0.30 / 9.0,
        logDate = date,
        loggedAt = LocalDateTime.now().withHour(hour)
    )
}