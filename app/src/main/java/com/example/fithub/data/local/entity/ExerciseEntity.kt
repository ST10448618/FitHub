package com.example.fithub.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.fithub.domain.model.ExerciseCategory
import com.example.fithub.domain.model.WorkoutDifficulty

@Entity(tableName = "exercise")
data class ExerciseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: ExerciseCategory,
    val description: String,
    val difficulty: WorkoutDifficulty,
    val equipment: String,
    val isTimeBased: Boolean,
    val estimatedActivityPerRep: Double,
    val estimatedActivityPerSecond: Double,
    val estimatedSecondsPerRep: Int,
    val imageUrl: String?
)