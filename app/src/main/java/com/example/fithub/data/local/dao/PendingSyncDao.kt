// AI-assisted (DeepSeek); reviewed and tested by the FitHub team.
package com.example.fithub.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.fithub.data.local.entity.PendingSyncEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingSyncDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: PendingSyncEntity)

    @Update
    suspend fun update(entry: PendingSyncEntity)

    @Query("SELECT * FROM pending_sync WHERE userId = :userId ORDER BY createdAt ASC")
    suspend fun getAllOrdered(userId: String): List<PendingSyncEntity>

    @Query("SELECT COUNT(*) FROM pending_sync WHERE userId = :userId")
    fun observeCount(userId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM pending_sync WHERE userId = :userId")
    suspend fun count(userId: String): Int

    @Query("DELETE FROM pending_sync WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM pending_sync WHERE entityType = :type AND entityId = :entityId LIMIT 1)")
    suspend fun existsFor(type: String, entityId: String): Boolean

    @Query("SELECT * FROM pending_sync WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): PendingSyncEntity?
}