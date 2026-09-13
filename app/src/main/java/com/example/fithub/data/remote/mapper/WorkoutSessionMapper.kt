package com.example.fithub.data.remote.mapper

import com.example.fithub.domain.model.CompletedExercise
import com.example.fithub.domain.model.SessionStatus
import com.example.fithub.domain.model.WorkoutCategory
import com.example.fithub.domain.model.WorkoutSession
import java.time.LocalDate
import java.time.LocalDateTime

object WorkoutSessionMapper {

    fun toMap(s: WorkoutSession): Map<String, Any?> = mapOf(
        "workoutPlanId" to s.workoutPlanId,
        "workoutName" to s.workoutName,
        "workoutCategory" to s.workoutCategory.name,
        "durationMinutes" to s.durationMinutes,
        "estimatedActivityKcal" to s.estimatedActivityKcal,
        "status" to s.status.name,
        "startedAt" to s.startedAt.toString(),
        "completedAt" to s.completedAt.toString(),
        "logDate" to s.logDate.toString(),
        "exercises" to s.exercises.map { ex ->
            mapOf(
                "exerciseId" to ex.exerciseId,
                "exerciseName" to ex.exerciseName,
                "reps" to ex.reps,
                "durationSeconds" to ex.durationSeconds,
                "estimatedActivityKcal" to ex.estimatedActivityKcal
            )
        }
    )

    @Suppress("UNCHECKED_CAST")
    fun fromMap(id: String, uid: String, map: Map<String, Any?>): WorkoutSession {
        val raw = map["exercises"] as? List<Map<String, Any?>> ?: emptyList()
        val exercises = raw.mapNotNull { ex ->
            runCatching {
                CompletedExercise(
                    exerciseId = ex["exerciseId"] as? String ?: "",
                    exerciseName = ex["exerciseName"] as? String ?: "",
                    reps = (ex["reps"] as? Number)?.toInt() ?: 0,
                    durationSeconds = (ex["durationSeconds"] as? Number)?.toInt() ?: 0,
                    estimatedActivityKcal = (ex["estimatedActivityKcal"] as? Number)?.toDouble() ?: 0.0
                )
            }.getOrNull()
        }

        return WorkoutSession(
            id = id,
            userId = uid,
            workoutPlanId = map["workoutPlanId"] as? String ?: "",
            workoutName = map["workoutName"] as? String ?: "",
            workoutCategory = (map["workoutCategory"] as? String)?.let {
                runCatching { WorkoutCategory.valueOf(it) }.getOrDefault(WorkoutCategory.GENERAL_FITNESS)
            } ?: WorkoutCategory.GENERAL_FITNESS,
            exercises = exercises,
            durationMinutes = (map["durationMinutes"] as? Number)?.toInt() ?: 0,
            estimatedActivityKcal = (map["estimatedActivityKcal"] as? Number)?.toInt() ?: 0,
            status = (map["status"] as? String)?.let {
                runCatching { SessionStatus.valueOf(it) }.getOrDefault(SessionStatus.COMPLETED)
            } ?: SessionStatus.COMPLETED,
            startedAt = (map["startedAt"] as? String)?.let {
                runCatching { LocalDateTime.parse(it) }.getOrNull()
            } ?: LocalDateTime.now(),
            completedAt = (map["completedAt"] as? String)?.let {
                runCatching { LocalDateTime.parse(it) }.getOrNull()
            } ?: LocalDateTime.now(),
            logDate = (map["logDate"] as? String)?.let {
                runCatching { LocalDate.parse(it) }.getOrNull()
            } ?: LocalDate.now()
        )
    }
}