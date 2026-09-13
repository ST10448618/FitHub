package com.example.fithub.data.local.dao

import androidx.room.*
import com.example.fithub.data.local.entity.FoodLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(log: FoodLogEntity)

    @Delete
    suspend fun delete(log: FoodLogEntity)

    @Query("SELECT * FROM food_log WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): FoodLogEntity?

    @Query("SELECT * FROM food_log WHERE userId = :userId AND logDate = :date ORDER BY loggedAt ASC")
    fun observeByDate(userId: String, date: String): Flow<List<FoodLogEntity>>

    @Query("SELECT * FROM food_log WHERE userId = :userId AND logDate = :date ORDER BY loggedAt ASC")
    suspend fun getByDate(userId: String, date: String): List<FoodLogEntity>

    @Query("SELECT * FROM food_log WHERE userId = :userId AND logDate BETWEEN :from AND :to ORDER BY logDate ASC")
    suspend fun getBetween(userId: String, from: String, to: String): List<FoodLogEntity>

    @Query("SELECT * FROM food_log WHERE userId = :userId ORDER BY loggedAt DESC LIMIT :limit")
    fun observeRecent(userId: String, limit: Int = 50): Flow<List<FoodLogEntity>>
}