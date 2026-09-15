package com.example.fithub.data.local.dao

import androidx.room.*
import com.example.fithub.data.local.entity.CheckpointScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckpointScheduleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(schedule: CheckpointScheduleEntity)

    @Query("SELECT * FROM checkpoint_schedule WHERE userId = :userId LIMIT 1")
    suspend fun get(userId: String): CheckpointScheduleEntity?

    @Query("SELECT * FROM checkpoint_schedule WHERE userId = :userId LIMIT 1")
    fun observe(userId: String): Flow<CheckpointScheduleEntity?>

    @Query("DELETE FROM checkpoint_schedule WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)
}