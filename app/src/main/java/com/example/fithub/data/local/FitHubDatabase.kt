package com.example.fithub.data.local

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.fithub.core.LogTags
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
        ParticleBalanceEntity::class,
        // Phase 3: outbox table for offline-first writes
        PendingSyncEntity::class
    ],
    version = 2,
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

    /** Phase 3: outbox DAO used by SyncManager and SyncStatusBanner. */
    abstract fun pendingSyncDao(): PendingSyncDao

    companion object {
        private const val DB_NAME = "fithub.db"

        @Volatile
        private var INSTANCE: FitHubDatabase? = null

        /**
         * Room migration v1 → v2: adds the `pending_sync` outbox table.
         *
         * The CREATE TABLE below MUST match [PendingSyncEntity] exactly.
         * If you change the entity, change this SQL in the same commit or
         * Room's schema check will throw `IllegalStateException: Migration
         * didn't properly handle…` at runtime.
         *
         * Verification procedure: install the pre-Phase-3 build (v1 DB on
         * disk), upgrade-install the Phase-3 build without uninstalling —
         * the app must launch and, in `logcat`, show
         * `FitHub/Sync DB migrated to v2 (pending_sync ready)` without
         * crashing. If you uninstall first, you cannot observe the
         * migration path.
         *
         * Reference: https://developer.android.com/training/data-storage/room/migrating-db-versions
         * (accessed February 2026)
         */
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `pending_sync` (
                        `id` TEXT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `entityType` TEXT NOT NULL,
                        `entityId` TEXT NOT NULL,
                        `operation` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `attemptCount` INTEGER NOT NULL,
                        `lastError` TEXT,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                Log.i(LogTags.SYNC, "DB migrated to v2 (pending_sync ready)")
            }
        }

        fun getInstance(context: Context): FitHubDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    FitHubDatabase::class.java,
                    DB_NAME
                )
                    .addMigrations(MIGRATION_1_2)
                    // Only allow downgrade to nuke — forward versions must be
                    // explicit migrations. Never add fallbackToDestructiveMigration()
                    // here; that would silently lose outbox rows on upgrade.
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                    .also {
                        INSTANCE = it
                        Log.i(LogTags.SYNC, "FitHubDatabase instance created (version=2)")
                    }
            }
        }
    }
}