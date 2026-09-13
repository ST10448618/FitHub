package com.example.fithub.data.local.dao

import androidx.room.*
import com.example.fithub.data.local.entity.ParticleBalanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ParticleBalanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(balance: ParticleBalanceEntity)

    @Query("SELECT * FROM particle_balance WHERE userId = :userId LIMIT 1")
    suspend fun get(userId: String): ParticleBalanceEntity?

    @Query("SELECT * FROM particle_balance WHERE userId = :userId LIMIT 1")
    fun observe(userId: String): Flow<ParticleBalanceEntity?>
}