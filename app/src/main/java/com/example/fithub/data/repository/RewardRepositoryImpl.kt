package com.example.fithub.data.repository

import com.example.fithub.core.Resource
import com.example.fithub.core.await
import com.example.fithub.data.local.dao.ParticleBalanceDao
import com.example.fithub.data.local.dao.UserAchievementDao
import com.example.fithub.data.local.entity.ParticleBalanceEntity
import com.example.fithub.data.local.entity.UserAchievementEntity
import com.example.fithub.data.remote.FirestorePaths
import com.example.fithub.domain.model.ParticleBalance
import com.example.fithub.domain.model.UserAchievement
import com.example.fithub.domain.repository.RewardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime

class RewardRepositoryImpl(
    private val balanceDao: ParticleBalanceDao,
    private val achievementDao: UserAchievementDao
) : RewardRepository {

    override fun observeBalance(uid: String): Flow<ParticleBalance?> =
        balanceDao.observe(uid).map { entity ->
            entity?.let {
                ParticleBalance(
                    userId = it.userId,
                    balance = it.balance,
                    lifetimeEarned = it.lifetimeEarned,
                    updatedAt = it.updatedAt
                )
            }
        }

    override fun observeAchievements(uid: String): Flow<List<UserAchievement>> =
        achievementDao.observeAll(uid).map { list ->
            list.map {
                UserAchievement(
                    achievementId = it.achievementId,
                    userId = it.userId,
                    progress = it.progress,
                    isUnlocked = it.isUnlocked,
                    isClaimed = it.isClaimed,
                    unlockedAt = it.unlockedAt,
                    claimedAt = it.claimedAt
                )
            }
        }

    override suspend fun addParticles(uid: String, amount: Int): Resource<Unit> = try {
        val existing = balanceDao.get(uid)
        val newBalance = (existing?.balance ?: 0) + amount
        val newLifetime = (existing?.lifetimeEarned ?: 0) + amount
        val updated = ParticleBalanceEntity(
            userId = uid,
            balance = newBalance,
            lifetimeEarned = newLifetime,
            updatedAt = LocalDateTime.now()
        )
        balanceDao.upsert(updated)

        FirestorePaths.particleBalance(uid).set(
            mapOf(
                "balance" to newBalance,
                "lifetimeEarned" to newLifetime,
                "updatedAt" to LocalDateTime.now().toString()
            )
        ).await()

        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to add particles", e)
    }

    override suspend fun upsertAchievement(uid: String, achievement: UserAchievement): Resource<Unit> = try {
        val entity = UserAchievementEntity(
            userId = uid,
            achievementId = achievement.achievementId,
            progress = achievement.progress,
            isUnlocked = achievement.isUnlocked,
            isClaimed = achievement.isClaimed,
            unlockedAt = achievement.unlockedAt,
            claimedAt = achievement.claimedAt
        )
        achievementDao.upsert(entity)

        FirestorePaths.userAchievement(uid, achievement.achievementId).set(
            mapOf(
                "progress" to achievement.progress,
                "isUnlocked" to achievement.isUnlocked,
                "isClaimed" to achievement.isClaimed,
                "unlockedAt" to achievement.unlockedAt?.toString(),
                "claimedAt" to achievement.claimedAt?.toString()
            )
        ).await()

        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to update achievement", e)
    }

    override suspend fun claimAchievement(uid: String, achievementId: String): Resource<Unit> = try {
        val existing = achievementDao.get(uid, achievementId)
        if (existing != null && existing.isUnlocked && !existing.isClaimed) {
            val updated = existing.copy(
                isClaimed = true,
                claimedAt = LocalDateTime.now()
            )
            achievementDao.upsert(updated)
            FirestorePaths.userAchievement(uid, achievementId).update(
                mapOf(
                    "isClaimed" to true,
                    "claimedAt" to LocalDateTime.now().toString()
                )
            ).await()
        }
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to claim achievement", e)
    }

    override suspend fun syncFromRemote(uid: String): Resource<Unit> = try {
        val balDoc = FirestorePaths.particleBalance(uid).get().await()
        if (balDoc.exists()) {
            balDoc.data?.let { map ->
                val entity = ParticleBalanceEntity(
                    userId = uid,
                    balance = (map["balance"] as? Number)?.toInt() ?: 0,
                    lifetimeEarned = (map["lifetimeEarned"] as? Number)?.toInt() ?: 0,
                    updatedAt = LocalDateTime.now()
                )
                balanceDao.upsert(entity)
            }
        }

        val achSnap = FirestorePaths.userAchievements(uid).get().await()
        achSnap.documents.forEach { doc ->
            val map = doc.data ?: return@forEach
            val entity = UserAchievementEntity(
                userId = uid,
                achievementId = doc.id,
                progress = (map["progress"] as? Number)?.toInt() ?: 0,
                isUnlocked = map["isUnlocked"] as? Boolean ?: false,
                isClaimed = map["isClaimed"] as? Boolean ?: false,
                unlockedAt = (map["unlockedAt"] as? String)?.let {
                    runCatching { LocalDateTime.parse(it) }.getOrNull()
                },
                claimedAt = (map["claimedAt"] as? String)?.let {
                    runCatching { LocalDateTime.parse(it) }.getOrNull()
                }
            )
            achievementDao.upsert(entity)
        }
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to sync rewards", e)
    }
}