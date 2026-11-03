package com.example.fithub.core

import android.content.Context
import com.example.fithub.core.sync.FirestoreRemoteGateway
import com.example.fithub.core.sync.PendingSyncStoreImpl
import com.example.fithub.core.sync.RemoteGateway
import com.example.fithub.core.sync.SyncManager
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
        WeightRepositoryImpl(
            database.weightEntryDao(),
            database.userProfileDao(),
            database.pendingSyncDao()
        )
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
        FoodLogRepositoryImpl(
            database.foodLogDao(),
            database.pendingSyncDao()
        )
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

    val preferencesManager: PreferencesManager by lazy {
        PreferencesManager(appContext)
    }

    val seedManager: SeedManager by lazy { SeedManager(database, exerciseRepository) }

    val connectivityMonitor: ConnectivityMonitor by lazy {
        ConnectivityMonitor(appContext)
    }

    // ---------- Phase 3: sync pipeline ----------

    val pendingSyncStore: com.example.fithub.core.sync.PendingSyncStore by lazy {
        PendingSyncStoreImpl(database.pendingSyncDao())
    }

    /**
     * Firestore-backed remote gateway. Declared `by lazy` so construction is
     * deferred until first sync; if Firebase is not initialised yet, nothing
     * breaks at start-up.
     */
    val remoteGateway: RemoteGateway by lazy {
        FirestoreRemoteGateway(
            foodLogDao = database.foodLogDao(),
            weightDao = database.weightEntryDao(),
            foodLogRepository = foodLogRepository,
            weightRepository = weightRepository
        )
    }

    val syncManager: SyncManager by lazy {
        SyncManager(
            store = pendingSyncStore,
            remote = remoteGateway,
            isOnline = { connectivityMonitor.isOnline.value }
        )
    }

    val foodRepository: com.example.fithub.domain.repository.FoodRepository by lazy {
        com.example.fithub.data.repository.FoodRepositoryImpl(
            database.foodDao(),
            com.example.fithub.data.remote.api.OpenFoodFactsDataSource()
        )
    }

    fun init(context: Context) {
        appContext = context.applicationContext
        database = FitHubDatabase.getInstance(appContext)
    }

    fun appContext(): Context = appContext
}