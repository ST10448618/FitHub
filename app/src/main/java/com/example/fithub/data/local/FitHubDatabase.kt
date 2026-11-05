// AI-assisted (DeepSeek); reviewed and tested by the FitHub team.
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
        PendingSyncEntity::class
    ],
    version = 3,
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
    abstract fun pendingSyncDao(): PendingSyncDao

    companion object {
        private const val DB_NAME = "fithub.db"

        @Volatile
        private var INSTANCE: FitHubDatabase? = null

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

        /**
         * Room migration v2 → v3: adds the optional `photoUrl` column to
         * `weight_entry` for the progress-photo timeline (Phase 6, UDF 4).
         *
         * The SQL matches [WeightEntryEntity] exactly: `photoUrl` is
         * nullable `TEXT` with no default. Room checks schema at runtime and
         * will throw if this drifts.
         *
         * Verification: install the Phase-5 build (DB v2 on disk), upgrade-
         * install the Phase-6 build without uninstalling. Logcat must show
         * `DB migrated to v3 (weight_entry.photoUrl)`.
         *
         * Reference: https://developer.android.com/training/data-storage/room/migrating-db-versions
         * (accessed February 2026)
         */
        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `weight_entry` ADD COLUMN `photoUrl` TEXT"
                )
                Log.i(LogTags.SYNC, "DB migrated to v3 (weight_entry.photoUrl)")
            }
        }

        fun getInstance(context: Context): FitHubDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    FitHubDatabase::class.java,
                    DB_NAME
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                    .also {
                        INSTANCE = it
                        Log.i(LogTags.SYNC, "FitHubDatabase instance created (version=3)")
                    }
            }
        }
    }
}