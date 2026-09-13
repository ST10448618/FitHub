package com.example.fithub.domain.repository

import com.example.fithub.core.Resource
import com.example.fithub.domain.model.ParticleBalance
import com.example.fithub.domain.model.UserAchievement
import kotlinx.coroutines.flow.Flow

interface RewardRepository {
    fun observeBalance(uid: String): Flow<ParticleBalance?>
    fun observeAchievements(uid: String): Flow<List<UserAchievement>>
    suspend fun addParticles(uid: String, amount: Int): Resource<Unit>
    suspend fun upsertAchievement(uid: String, achievement: UserAchievement): Resource<Unit>
    suspend fun claimAchievement(uid: String, achievementId: String): Resource<Unit>
    suspend fun syncFromRemote(uid: String): Resource<Unit>
}