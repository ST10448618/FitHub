package com.example.fithub.data.local.dao

import androidx.room.*
import com.example.fithub.data.local.entity.WorkoutGoalsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutGoalsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(goals: WorkoutGoalsEntity)

    @Query("SELECT * FROM workout_goals WHERE userId = :userId LIMIT 1")
    suspend fun get(userId: String): WorkoutGoalsEntity?

    @Query("SELECT * FROM workout_goals WHERE userId = :userId LIMIT 1")
    fun observe(userId: String): Flow<WorkoutGoalsEntity?>
}