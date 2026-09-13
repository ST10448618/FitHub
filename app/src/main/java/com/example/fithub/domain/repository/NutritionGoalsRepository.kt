package com.example.fithub.domain.repository

import com.example.fithub.core.Resource
import com.example.fithub.domain.model.NutritionGoals
import kotlinx.coroutines.flow.Flow

interface NutritionGoalsRepository {
    fun observeCurrent(uid: String): Flow<NutritionGoals?>
    suspend fun getCurrent(uid: String): NutritionGoals?
    suspend fun save(goals: NutritionGoals): Resource<Unit>
    suspend fun syncFromRemote(uid: String): Resource<Unit>
}