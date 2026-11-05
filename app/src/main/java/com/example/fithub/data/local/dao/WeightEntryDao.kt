// AI-assisted (DeepSeek); reviewed and tested by the FitHub team.
package com.example.fithub.data.local.dao

import androidx.room.*
import com.example.fithub.data.local.entity.WeightEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: WeightEntryEntity)

    @Query("SELECT * FROM weight_entry WHERE userId = :userId ORDER BY date DESC")
    fun observeAll(userId: String): Flow<List<WeightEntryEntity>>

    @Query("SELECT * FROM weight_entry WHERE userId = :userId ORDER BY date DESC LIMIT 1")
    suspend fun getLatest(userId: String): WeightEntryEntity?

    @Query("SELECT * FROM weight_entry WHERE userId = :userId ORDER BY date DESC LIMIT 1")
    fun observeLatest(userId: String): Flow<WeightEntryEntity?>

    @Query("SELECT * FROM weight_entry WHERE userId = :userId AND date BETWEEN :from AND :to ORDER BY date ASC")
    suspend fun getBetween(userId: String, from: String, to: String): List<WeightEntryEntity>

    @Query("DELETE FROM weight_entry WHERE id = :id")
    suspend fun delete(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entries: List<WeightEntryEntity>)

    @Query("SELECT * FROM weight_entry WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): WeightEntryEntity?

    /** Phase 6 (UDF 4): update the photo URL after a successful upload. */
    @Query("UPDATE weight_entry SET photoUrl = :photoUrl WHERE id = :id")
    suspend fun updatePhotoUrl(id: String, photoUrl: String?)

    /** Phase 6 (UDF 4): rows that have no photo (for the retry-pending sweep). */
    @Query("SELECT * FROM weight_entry WHERE userId = :userId AND photoUrl IS NULL ORDER BY date ASC")
    suspend fun getWithoutPhoto(userId: String): List<WeightEntryEntity>
}