package com.example.fithub.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.fithub.domain.model.GoalDirection
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "goal", indices = [Index("userId")])
data class GoalEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val currentWeightKgAtGoalSet: Double,
    val targetWeightKg: Double,
    val direction: GoalDirection,
    val effectiveDate: LocalDate,
    val createdAt: LocalDateTime
)