package com.example.fithub.data.local.dao

import androidx.room.*
import com.example.fithub.data.local.entity.GoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(goal: GoalEntity)

    @Query("SELECT * FROM goal WHERE userId = :userId ORDER BY effectiveDate DESC LIMIT 1")
    suspend fun getCurrent(userId: String): GoalEntity?

    @Query("SELECT * FROM goal WHERE userId = :userId ORDER BY effectiveDate DESC LIMIT 1")
    fun observeCurrent(userId: String): Flow<GoalEntity?>

    @Query("SELECT * FROM goal WHERE userId = :userId ORDER BY effectiveDate DESC")
    suspend fun getHistory(userId: String): List<GoalEntity>
}