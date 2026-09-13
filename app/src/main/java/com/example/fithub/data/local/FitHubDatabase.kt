package com.example.fithub.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.fithub.data.local.converter.Converters
import com.example.fithub.data.local.dao.*
import com.example.fithub.data.local.entity.*

@Database(
    entities = [
        UserProfileEntity::class,
        WeightEntryEntity::class,
        GoalEntity::class,
        NutritionGoalsEntity::class,
        WorkoutGoalsEntity::class,
        CheckpointScheduleEntity::class,
        FoodEntity::class,
        FoodLogEntity::class,
        ExerciseEntity::class,
        WorkoutPlanEntity::class,
        WorkoutExerciseEntity::class,
        WorkoutSessionEntity::class,
        CompletedExerciseEntity::class,
        UserAchievementEntity::class,
        ParticleBalanceEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FitHubDatabase : RoomDatabase() {

    abstract fun userProfileDao(): UserProfileDao
    abstract fun weightEntryDao(): WeightEntryDao
    abstract fun goalDao(): GoalDao
    abstract fun nutritionGoalsDao(): NutritionGoalsDao
    abstract fun workoutGoalsDao(): WorkoutGoalsDao
    abstract fun checkpointScheduleDao(): CheckpointScheduleDao
    abstract fun foodDao(): FoodDao
    abstract fun foodLogDao(): FoodLogDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutPlanDao(): WorkoutPlanDao
    abstract fun workoutSessionDao(): WorkoutSessionDao
    abstract fun completedExerciseDao(): CompletedExerciseDao
    abstract fun userAchievementDao(): UserAchievementDao
    abstract fun particleBalanceDao(): ParticleBalanceDao
    abstract fun workoutExerciseDao(): WorkoutExerciseDao


    companion object {
        private const val DB_NAME = "fithub.db"

        @Volatile
        private var INSTANCE: FitHubDatabase? = null

        fun getInstance(context: Context): FitHubDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    FitHubDatabase::class.java,
                    DB_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}