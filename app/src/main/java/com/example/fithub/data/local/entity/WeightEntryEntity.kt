// AI-assisted (DeepSeek); reviewed and tested by the FitHub team.
package com.example.fithub.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(
    tableName = "weight_entry",
    indices = [Index("userId"), Index("date")]
)
data class WeightEntryEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val weightKg: Double,
    val date: LocalDate,
    val note: String?,
    val createdAt: LocalDateTime,
    /**
     * Firebase Storage download URL for the optional progress photo, or null
     * if the user didn't attach one. Migration 2→3 adds this column.
     */
    val photoUrl: String? = null
)