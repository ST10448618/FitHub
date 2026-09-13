package com.example.fithub.data.remote.mapper

import com.example.fithub.domain.model.WorkoutGoals
import java.time.LocalDateTime

object WorkoutGoalsMapper {
    fun toMap(g: WorkoutGoals): Map<String, Any?> = mapOf(
        "sessionsPerWeek" to g.sessionsPerWeek,
        "monthlyActivityGoalKcal" to g.monthlyActivityGoalKcal,
        "updatedAt" to g.updatedAt.toString()
    )

    fun fromMap(id: String, uid: String, map: Map<String, Any?>): WorkoutGoals =
        WorkoutGoals(
            id = id,
            userId = uid,
            sessionsPerWeek = (map["sessionsPerWeek"] as? Number)?.toInt() ?: 4,
            monthlyActivityGoalKcal = (map["monthlyActivityGoalKcal"] as? Number)?.toInt() ?: 10000,
            updatedAt = (map["updatedAt"] as? String)?.let {
                runCatching { LocalDateTime.parse(it) }.getOrNull()
            } ?: LocalDateTime.now()
        )
}