package com.example.fithub.data.local.mapper

import com.example.fithub.data.local.entity.WorkoutExerciseEntity
import com.example.fithub.data.local.entity.WorkoutPlanEntity
import com.example.fithub.domain.model.WorkoutExercise
import com.example.fithub.domain.model.WorkoutPlan

object WorkoutPlanLocalMapper {
    fun toDomain(
        e: WorkoutPlanEntity,
        exercises: List<WorkoutExerciseEntity>
    ) = WorkoutPlan(
        id = e.id,
        userId = e.userId,
        name = e.name,
        description = e.description,
        category = e.category,
        difficulty = e.difficulty,
        equipment = e.equipment,
        imageUrl = e.imageUrl,
        estimatedActivityKcal = e.estimatedActivityKcal,
        estimatedDurationMinutes = e.estimatedDurationMinutes,
        isVerified = e.isVerified,
        isSaved = e.isSaved,
        createdAt = e.createdAt,
        exercises = exercises
            .sortedBy { it.orderIndex }
            .map {
                WorkoutExercise(
                    exerciseId = it.exerciseId,
                    exerciseName = it.exerciseName,
                    category = it.category,
                    orderIndex = it.orderIndex,
                    targetReps = it.targetReps,
                    targetDurationSeconds = it.targetDurationSeconds,
                    estimatedActivityKcal = it.estimatedActivityKcal
                )
            }
    )

    fun toEntity(d: WorkoutPlan) = WorkoutPlanEntity(
        id = d.id,
        userId = d.userId,
        name = d.name,
        description = d.description,
        category = d.category,
        difficulty = d.difficulty,
        equipment = d.equipment,
        imageUrl = d.imageUrl,
        estimatedActivityKcal = d.estimatedActivityKcal,
        estimatedDurationMinutes = d.estimatedDurationMinutes,
        isVerified = d.isVerified,
        isSaved = d.isSaved,
        createdAt = d.createdAt
    )

    fun toExerciseEntities(planId: String, exercises: List<WorkoutExercise>) =
        exercises.mapIndexed { idx, ex ->
            WorkoutExerciseEntity(
                id = "${planId}_${ex.exerciseId}_$idx",
                workoutPlanId = planId,
                exerciseId = ex.exerciseId,
                exerciseName = ex.exerciseName,
                category = ex.category,
                orderIndex = ex.orderIndex,
                targetReps = ex.targetReps,
                targetDurationSeconds = ex.targetDurationSeconds,
                estimatedActivityKcal = ex.estimatedActivityKcal
            )
        }
}