package com.example.fithub.data.local.dao

import androidx.room.*
import com.example.fithub.data.local.entity.WorkoutExerciseEntity

@Dao
interface WorkoutExerciseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(exercises: List<WorkoutExerciseEntity>)

    @Query("SELECT * FROM workout_exercise WHERE workoutPlanId = :planId ORDER BY orderIndex ASC")
    suspend fun getForPlan(planId: String): List<WorkoutExerciseEntity>

    @Query("DELETE FROM workout_exercise WHERE workoutPlanId = :planId")
    suspend fun deleteForPlan(planId: String)
}