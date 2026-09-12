package com.example.fithub.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.fithub.domain.model.WorkoutCategory
import com.example.fithub.domain.model.WorkoutDifficulty
import java.time.LocalDateTime

@Entity(
    tableName = "workout_plan",
    indices = [Index("userId"), Index("isVerified"), Index("isSaved")]
)
data class WorkoutPlanEntity(
    @PrimaryKey val id: String,
    val userId: String?,
    val name: String,
    val description: String,
    val category: WorkoutCategory,
    val difficulty: WorkoutDifficulty,
    val equipment: String,
    val imageUrl: String?,
    val estimatedActivityKcal: Int,
    val estimatedDurationMinutes: Int,
    val isVerified: Boolean,
    val isSaved: Boolean,
    val createdAt: LocalDateTime
)