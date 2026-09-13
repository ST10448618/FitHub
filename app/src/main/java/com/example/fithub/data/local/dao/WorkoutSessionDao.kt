package com.example.fithub.data.local.dao

import androidx.room.*
import com.example.fithub.data.local.entity.WorkoutSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(session: WorkoutSessionEntity)

    @Query("SELECT * FROM workout_session WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): WorkoutSessionEntity?

    @Query("SELECT * FROM workout_session WHERE userId = :userId AND status = 'COMPLETED' ORDER BY completedAt DESC")
    fun observeCompleted(userId: String): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_session WHERE userId = :userId AND logDate = :date AND status = 'COMPLETED' ORDER BY completedAt DESC")
    fun observeByDate(userId: String, date: String): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_session WHERE userId = :userId AND logDate BETWEEN :from AND :to AND status = 'COMPLETED' ORDER BY logDate ASC")
    suspend fun getBetween(userId: String, from: String, to: String): List<WorkoutSessionEntity>

    @Query("SELECT * FROM workout_session WHERE userId = :userId AND status = 'COMPLETED' ORDER BY completedAt DESC LIMIT :limit")
    fun observeRecent(userId: String, limit: Int = 20): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT COUNT(*) FROM workout_session WHERE userId = :userId AND status = 'COMPLETED' AND logDate BETWEEN :from AND :to")
    suspend fun countBetween(userId: String, from: String, to: String): Int

    @Query("DELETE FROM workout_session WHERE id = :id")
    suspend fun delete(id: String)
}