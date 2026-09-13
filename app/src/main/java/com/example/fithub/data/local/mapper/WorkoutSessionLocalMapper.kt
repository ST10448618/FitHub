package com.example.fithub.data.local.mapper

import com.example.fithub.data.local.entity.CompletedExerciseEntity
import com.example.fithub.data.local.entity.WorkoutSessionEntity
import com.example.fithub.domain.model.CompletedExercise
import com.example.fithub.domain.model.WorkoutSession

object WorkoutSessionLocalMapper {
    fun toDomain(
        e: WorkoutSessionEntity,
        exercises: List<CompletedExerciseEntity>
    ) = WorkoutSession(
        id = e.id,
        userId = e.userId,
        workoutPlanId = e.workoutPlanId,
        workoutName = e.workoutName,
        workoutCategory = e.workoutCategory,
        durationMinutes = e.durationMinutes,
        estimatedActivityKcal = e.estimatedActivityKcal,
        status = e.status,
        startedAt = e.startedAt,
        completedAt = e.completedAt,
        logDate = e.logDate,
        exercises = exercises.map {
            CompletedExercise(
                exerciseId = it.exerciseId,
                exerciseName = it.exerciseName,
                reps = it.reps,
                durationSeconds = it.durationSeconds,
                estimatedActivityKcal = it.estimatedActivityKcal
            )
        }
    )

    fun toEntity(d: WorkoutSession) = WorkoutSessionEntity(
        id = d.id,
        userId = d.userId,
        workoutPlanId = d.workoutPlanId,
        workoutName = d.workoutName,
        workoutCategory = d.workoutCategory,
        durationMinutes = d.durationMinutes,
        estimatedActivityKcal = d.estimatedActivityKcal,
        status = d.status,
        startedAt = d.startedAt,
        completedAt = d.completedAt,
        logDate = d.logDate
    )

    fun toExerciseEntities(sessionId: String, exercises: List<CompletedExercise>) =
        exercises.mapIndexed { idx, ex ->
            CompletedExerciseEntity(
                id = "${sessionId}_${ex.exerciseId}_$idx",
                sessionId = sessionId,
                exerciseId = ex.exerciseId,
                exerciseName = ex.exerciseName,
                reps = ex.reps,
                durationSeconds = ex.durationSeconds,
                estimatedActivityKcal = ex.estimatedActivityKcal
            )
        }
}