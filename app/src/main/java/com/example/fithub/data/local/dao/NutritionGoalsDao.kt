package com.example.fithub.data.local.dao

import androidx.room.*
import com.example.fithub.data.local.entity.NutritionGoalsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NutritionGoalsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(goals: NutritionGoalsEntity)

    @Query("SELECT * FROM nutrition_goals WHERE userId = :userId ORDER BY effectiveDate DESC LIMIT 1")
    suspend fun getCurrent(userId: String): NutritionGoalsEntity?

    @Query("SELECT * FROM nutrition_goals WHERE userId = :userId ORDER BY effectiveDate DESC LIMIT 1")
    fun observeCurrent(userId: String): Flow<NutritionGoalsEntity?>

    @Query("DELETE FROM nutrition_goals WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)
}