package com.example.fithub.data.local.dao

import androidx.room.*
import com.example.fithub.data.local.entity.FoodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(food: FoodEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(foods: List<FoodEntity>)

    @Query("SELECT * FROM food WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): FoodEntity?

    @Query("SELECT * FROM food WHERE name LIKE '%' || :query || '%' ORDER BY name LIMIT :limit")
    suspend fun searchByName(query: String, limit: Int = 30): List<FoodEntity>

    @Query("SELECT * FROM food WHERE category = :category ORDER BY name LIMIT :limit")
    suspend fun getByCategory(category: String, limit: Int = 30): List<FoodEntity>

    @Query("DELETE FROM food WHERE cachedAt < :cutoff")
    suspend fun deleteOlderThan(cutoff: String)
}