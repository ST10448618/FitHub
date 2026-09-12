package com.example.fithub.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "particle_balance")
data class ParticleBalanceEntity(
    @PrimaryKey val userId: String,
    val balance: Int,
    val lifetimeEarned: Int,
    val updatedAt: LocalDateTime
)