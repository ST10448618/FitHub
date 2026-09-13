package com.example.fithub.data.local.mapper

import com.example.fithub.data.local.entity.GoalEntity
import com.example.fithub.domain.model.Goal

object GoalLocalMapper {
    fun toDomain(e: GoalEntity) = Goal(
        id = e.id,
        userId = e.userId,
        currentWeightKgAtGoalSet = e.currentWeightKgAtGoalSet,
        targetWeightKg = e.targetWeightKg,
        direction = e.direction,
        effectiveDate = e.effectiveDate,
        createdAt = e.createdAt
    )

    fun toEntity(d: Goal) = GoalEntity(
        id = d.id,
        userId = d.userId,
        currentWeightKgAtGoalSet = d.currentWeightKgAtGoalSet,
        targetWeightKg = d.targetWeightKg,
        direction = d.direction,
        effectiveDate = d.effectiveDate,
        createdAt = d.createdAt
    )
}