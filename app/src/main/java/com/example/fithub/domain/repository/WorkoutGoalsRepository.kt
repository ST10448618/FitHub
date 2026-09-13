package com.example.fithub.domain.repository

import com.example.fithub.core.Resource
import com.example.fithub.domain.model.WorkoutGoals
import kotlinx.coroutines.flow.Flow

interface WorkoutGoalsRepository {
    fun observe(uid: String): Flow<WorkoutGoals?>
    suspend fun get(uid: String): WorkoutGoals?
    suspend fun save(goals: WorkoutGoals): Resource<Unit>
    suspend fun syncFromRemote(uid: String): Resource<Unit>
}