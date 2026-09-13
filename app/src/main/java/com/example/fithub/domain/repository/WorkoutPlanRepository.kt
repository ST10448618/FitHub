package com.example.fithub.domain.repository

import com.example.fithub.core.Resource
import com.example.fithub.domain.model.WorkoutCategory
import com.example.fithub.domain.model.WorkoutPlan
import kotlinx.coroutines.flow.Flow

interface WorkoutPlanRepository {
    // Verified library
    fun observeVerified(): Flow<List<WorkoutPlan>>
    suspend fun getVerifiedByCategory(category: WorkoutCategory): List<WorkoutPlan>

    // Created by user
    fun observeCreated(uid: String): Flow<List<WorkoutPlan>>
    suspend fun saveCreated(uid: String, plan: WorkoutPlan): Resource<Unit>

    // Saved by user (from library)
    fun observeSaved(uid: String): Flow<List<WorkoutPlan>>
    suspend fun saveFromLibrary(uid: String, plan: WorkoutPlan): Resource<Unit>
    suspend fun unsave(uid: String, planId: String): Resource<Unit>

    // Details
    fun observeById(planId: String): Flow<WorkoutPlan?>
    suspend fun getById(planId: String): WorkoutPlan?

    // Sync
    suspend fun syncVerifiedFromRemote(): Resource<Unit>
}