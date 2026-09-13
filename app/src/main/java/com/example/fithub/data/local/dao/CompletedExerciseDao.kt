package com.example.fithub.data.local.dao

import androidx.room.*
import com.example.fithub.data.local.entity.CompletedExerciseEntity

@Dao
interface CompletedExerciseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(exercises: List<CompletedExerciseEntity>)

    @Query("SELECT * FROM completed_exercise WHERE sessionId = :sessionId ORDER BY id ASC")
    suspend fun getForSession(sessionId: String): List<CompletedExerciseEntity>

    @Query("DELETE FROM completed_exercise WHERE sessionId = :sessionId")
    suspend fun deleteForSession(sessionId: String)
}