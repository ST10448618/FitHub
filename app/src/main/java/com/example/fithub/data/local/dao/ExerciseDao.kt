package com.example.fithub.data.local.dao

import androidx.room.*
import com.example.fithub.data.local.entity.ExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(exercises: List<ExerciseEntity>)

    @Query("SELECT * FROM exercise ORDER BY name")
    fun observeAll(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercise WHERE category = :category ORDER BY name")
    suspend fun getByCategory(category: String): List<ExerciseEntity>

    @Query("SELECT * FROM exercise WHERE name LIKE '%' || :query || '%' ORDER BY name LIMIT :limit")
    suspend fun search(query: String, limit: Int = 30): List<ExerciseEntity>

    @Query("SELECT * FROM exercise WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ExerciseEntity?

    @Query("SELECT COUNT(*) FROM exercise")
    suspend fun count(): Int
}