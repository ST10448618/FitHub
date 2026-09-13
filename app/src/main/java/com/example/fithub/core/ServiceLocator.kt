package com.example.fithub.core

import android.content.Context
import com.example.fithub.data.local.FitHubDatabase
import com.example.fithub.data.repository.*
import com.example.fithub.data.seed.SeedManager
import com.example.fithub.domain.repository.*

object ServiceLocator {

    private lateinit var appContext: Context

    lateinit var database: FitHubDatabase
        private set

    val authRepository: AuthRepository by lazy { AuthRepositoryImpl() }
    val userRepository: UserRepository by lazy { UserRepositoryImpl(database.userProfileDao()) }
    val weightRepository: WeightRepository by lazy {
        WeightRepositoryImpl(database.weightEntryDao(), database.userProfileDao())
    }
    val goalRepository: GoalRepository by lazy { GoalRepositoryImpl(database.goalDao()) }
    val nutritionGoalsRepository: NutritionGoalsRepository by lazy {
        NutritionGoalsRepositoryImpl(database.nutritionGoalsDao())
    }
    val workoutGoalsRepository: WorkoutGoalsRepository by lazy {
        WorkoutGoalsRepositoryImpl(database.workoutGoalsDao())
    }
    val checkpointRepository: CheckpointRepository by lazy {
        CheckpointRepositoryImpl(database.checkpointScheduleDao())
    }
    val foodLogRepository: FoodLogRepository by lazy {
        FoodLogRepositoryImpl(database.foodLogDao())
    }
    val exerciseRepository: ExerciseRepository by lazy {
        ExerciseRepositoryImpl(database.exerciseDao())
    }
    val workoutPlanRepository: WorkoutPlanRepository by lazy {
        WorkoutPlanRepositoryImpl(database.workoutPlanDao(), database)
    }
    val workoutSessionRepository: WorkoutSessionRepository by lazy {
        WorkoutSessionRepositoryImpl(database.workoutSessionDao(), database)
    }
    val rewardRepository: RewardRepository by lazy {
        RewardRepositoryImpl(database.particleBalanceDao(), database.userAchievementDao())
    }

    val seedManager: SeedManager by lazy { SeedManager(database, exerciseRepository) }

    fun init(context: Context) {
        appContext = context.applicationContext
        database = FitHubDatabase.getInstance(appContext)
    }

    fun appContext(): Context = appContext
}