package com.example.fithub.domain.repository

import com.example.fithub.core.Resource
import com.example.fithub.domain.model.Goal
import kotlinx.coroutines.flow.Flow

interface GoalRepository {
    fun observeCurrent(uid: String): Flow<Goal?>
    suspend fun getCurrent(uid: String): Goal?
    suspend fun setGoal(goal: Goal): Resource<Unit>
    suspend fun syncFromRemote(uid: String): Resource<Unit>
}