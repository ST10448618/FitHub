package com.example.fithub.data.local.dao

import androidx.room.*
import com.example.fithub.data.local.entity.WorkoutPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutPlanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(plan: WorkoutPlanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(plans: List<WorkoutPlanEntity>)

    @Query("SELECT * FROM workout_plan WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): WorkoutPlanEntity?

    @Query("SELECT * FROM workout_plan WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<WorkoutPlanEntity?>

    @Query("SELECT * FROM workout_plan WHERE isVerified = 1 ORDER BY name")
    fun observeVerified(): Flow<List<WorkoutPlanEntity>>

    @Query("SELECT * FROM workout_plan WHERE userId = :userId AND isVerified = 0 ORDER BY createdAt DESC")
    fun observeCreatedByUser(userId: String): Flow<List<WorkoutPlanEntity>>

    @Query("SELECT * FROM workout_plan WHERE userId = :userId AND isSaved = 1 ORDER BY name")
    fun observeSavedByUser(userId: String): Flow<List<WorkoutPlanEntity>>

    @Query("SELECT * FROM workout_plan WHERE isVerified = 1 AND category = :category ORDER BY name")
    suspend fun getVerifiedByCategory(category: String): List<WorkoutPlanEntity>

    @Query("DELETE FROM workout_plan WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT COUNT(*) FROM workout_plan WHERE isVerified = 1")
    suspend fun verifiedCount(): Int
}