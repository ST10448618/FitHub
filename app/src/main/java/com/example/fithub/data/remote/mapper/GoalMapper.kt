package com.example.fithub.data.remote.mapper

import com.example.fithub.domain.model.Goal
import com.example.fithub.domain.model.GoalDirection
import java.time.LocalDate
import java.time.LocalDateTime

object GoalMapper {

    fun toMap(g: Goal): Map<String, Any?> = mapOf(
        "currentWeightKgAtGoalSet" to g.currentWeightKgAtGoalSet,
        "targetWeightKg" to g.targetWeightKg,
        "direction" to g.direction.name,
        "effectiveDate" to g.effectiveDate.toString(),
        "createdAt" to g.createdAt.toString()
    )

    fun fromMap(id: String, userId: String, map: Map<String, Any?>): Goal = Goal(
        id = id,
        userId = userId,
        currentWeightKgAtGoalSet = (map["currentWeightKgAtGoalSet"] as? Number)?.toDouble() ?: 0.0,
        targetWeightKg = (map["targetWeightKg"] as? Number)?.toDouble() ?: 0.0,
        direction = (map["direction"] as? String)?.let {
            runCatching { GoalDirection.valueOf(it) }.getOrDefault(GoalDirection.MAINTAIN)
        } ?: GoalDirection.MAINTAIN,
        effectiveDate = (map["effectiveDate"] as? String)?.let {
            runCatching { LocalDate.parse(it) }.getOrNull()
        } ?: LocalDate.now(),
        createdAt = (map["createdAt"] as? String)?.let {
            runCatching { LocalDateTime.parse(it) }.getOrNull()
        } ?: LocalDateTime.now()
    )
}