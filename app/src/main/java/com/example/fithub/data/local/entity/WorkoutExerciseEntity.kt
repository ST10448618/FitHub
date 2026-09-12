package com.example.fithub.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.fithub.domain.model.ExerciseCategory

@Entity(
    tableName = "workout_exercise",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutPlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutPlanId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("workoutPlanId"), Index("exerciseId")]
)
data class WorkoutExerciseEntity(
    @PrimaryKey val id: String,
    val workoutPlanId: String,
    val exerciseId: String,
    val exerciseName: String,
    val category: ExerciseCategory,
    val orderIndex: Int,
    val targetReps: Int,
    val targetDurationSeconds: Int,
    val estimatedActivityKcal: Double
)