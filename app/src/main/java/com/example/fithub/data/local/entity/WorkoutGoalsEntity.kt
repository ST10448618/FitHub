package com.example.fithub.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "workout_goals", indices = [Index("userId")])
data class WorkoutGoalsEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val sessionsPerWeek: Int,
    val monthlyActivityGoalKcal: Int,
    val updatedAt: LocalDateTime
)