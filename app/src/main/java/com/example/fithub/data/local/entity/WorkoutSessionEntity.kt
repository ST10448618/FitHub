package com.example.fithub.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.fithub.domain.model.SessionStatus
import com.example.fithub.domain.model.WorkoutCategory
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(
    tableName = "workout_session",
    indices = [
        Index("userId"),
        Index("logDate"),
        Index("workoutPlanId")
    ]
)
data class WorkoutSessionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val workoutPlanId: String,
    val workoutName: String,
    val workoutCategory: WorkoutCategory,
    val durationMinutes: Int,
    val estimatedActivityKcal: Int,
    val status: SessionStatus,
    val startedAt: LocalDateTime,
    val completedAt: LocalDateTime,
    val logDate: LocalDate
)