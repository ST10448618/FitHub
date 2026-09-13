package com.example.fithub.data.local.mapper

import com.example.fithub.data.local.entity.WorkoutGoalsEntity
import com.example.fithub.domain.model.WorkoutGoals

object WorkoutGoalsLocalMapper {
    fun toDomain(e: WorkoutGoalsEntity) = WorkoutGoals(
        id = e.id,
        userId = e.userId,
        sessionsPerWeek = e.sessionsPerWeek,
        monthlyActivityGoalKcal = e.monthlyActivityGoalKcal,
        updatedAt = e.updatedAt
    )

    fun toEntity(d: WorkoutGoals) = WorkoutGoalsEntity(
        id = d.id,
        userId = d.userId,
        sessionsPerWeek = d.sessionsPerWeek,
        monthlyActivityGoalKcal = d.monthlyActivityGoalKcal,
        updatedAt = d.updatedAt
    )
}