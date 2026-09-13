package com.example.fithub.data.remote.mapper

import com.example.fithub.domain.model.ExerciseCategory
import com.example.fithub.domain.model.WorkoutCategory
import com.example.fithub.domain.model.WorkoutDifficulty
import com.example.fithub.domain.model.WorkoutExercise
import com.example.fithub.domain.model.WorkoutPlan
import java.time.LocalDateTime

object WorkoutPlanMapper {

    fun toMap(p: WorkoutPlan): Map<String, Any?> = mapOf(
        "userId" to p.userId,
        "name" to p.name,
        "description" to p.description,
        "category" to p.category.name,
        "difficulty" to p.difficulty.name,
        "equipment" to p.equipment,
        "imageUrl" to p.imageUrl,
        "estimatedActivityKcal" to p.estimatedActivityKcal,
        "estimatedDurationMinutes" to p.estimatedDurationMinutes,
        "isVerified" to p.isVerified,
        "isSaved" to p.isSaved,
        "createdAt" to p.createdAt.toString(),
        "exercises" to p.exercises.map { ex ->
            mapOf(
                "exerciseId" to ex.exerciseId,
                "exerciseName" to ex.exerciseName,
                "category" to ex.category.name,
                "orderIndex" to ex.orderIndex,
                "targetReps" to ex.targetReps,
                "targetDurationSeconds" to ex.targetDurationSeconds,
                "estimatedActivityKcal" to ex.estimatedActivityKcal
            )
        }
    )

    @Suppress("UNCHECKED_CAST")
    fun fromMap(id: String, map: Map<String, Any?>): WorkoutPlan {
        val exercisesRaw = map["exercises"] as? List<Map<String, Any?>> ?: emptyList()
        val exercises = exercisesRaw.mapNotNull { ex ->
            runCatching {
                WorkoutExercise(
                    exerciseId = ex["exerciseId"] as? String ?: "",
                    exerciseName = ex["exerciseName"] as? String ?: "",
                    category = (ex["category"] as? String)?.let {
                        ExerciseCategory.valueOf(it)
                    } ?: ExerciseCategory.CORE,
                    orderIndex = (ex["orderIndex"] as? Number)?.toInt() ?: 0,
                    targetReps = (ex["targetReps"] as? Number)?.toInt() ?: 0,
                    targetDurationSeconds = (ex["targetDurationSeconds"] as? Number)?.toInt() ?: 0,
                    estimatedActivityKcal = (ex["estimatedActivityKcal"] as? Number)?.toDouble() ?: 0.0
                )
            }.getOrNull()
        }

        return WorkoutPlan(
            id = id,
            userId = map["userId"] as? String,
            name = map["name"] as? String ?: "Unnamed workout",
            description = map["description"] as? String ?: "",
            category = (map["category"] as? String)?.let {
                runCatching { WorkoutCategory.valueOf(it) }.getOrDefault(WorkoutCategory.GENERAL_FITNESS)
            } ?: WorkoutCategory.GENERAL_FITNESS,
            difficulty = (map["difficulty"] as? String)?.let {
                runCatching { WorkoutDifficulty.valueOf(it) }.getOrDefault(WorkoutDifficulty.BEGINNER)
            } ?: WorkoutDifficulty.BEGINNER,
            equipment = map["equipment"] as? String ?: "None",
            imageUrl = map["imageUrl"] as? String,
            estimatedActivityKcal = (map["estimatedActivityKcal"] as? Number)?.toInt() ?: 0,
            estimatedDurationMinutes = (map["estimatedDurationMinutes"] as? Number)?.toInt() ?: 0,
            isVerified = map["isVerified"] as? Boolean ?: false,
            isSaved = map["isSaved"] as? Boolean ?: false,
            createdAt = (map["createdAt"] as? String)?.let {
                runCatching { LocalDateTime.parse(it) }.getOrNull()
            } ?: LocalDateTime.now(),
            exercises = exercises
        )
    }
}