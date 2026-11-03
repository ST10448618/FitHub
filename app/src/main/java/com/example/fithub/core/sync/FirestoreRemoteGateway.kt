// AI-assisted (DeepSeek); reviewed and tested by the FitHub team.
package com.example.fithub.core.sync

import android.util.Log
import com.example.fithub.core.LogTags
import com.example.fithub.core.await
import com.example.fithub.data.local.dao.FoodLogDao
import com.example.fithub.data.local.dao.WeightEntryDao
import com.example.fithub.data.local.entity.PendingSyncEntity
import com.example.fithub.data.local.entity.PendingSyncOperation
import com.example.fithub.data.local.entity.PendingSyncType
import com.example.fithub.data.local.mapper.FoodLogLocalMapper
import com.example.fithub.data.local.mapper.WeightLocalMapper
import com.example.fithub.data.remote.FirestorePaths
import com.example.fithub.data.remote.mapper.FoodLogMapper
import com.example.fithub.data.remote.mapper.WeightMapper
import com.example.fithub.domain.repository.FoodLogRepository
import com.example.fithub.domain.repository.WeightRepository

/**
 * Reference: https://firebase.google.com/docs/firestore/manage-data/add-data
 *            https://firebase.google.com/docs/firestore/query-data/get-data
 * (accessed February 2026)
 */
class FirestoreRemoteGateway(
    private val foodLogDao: FoodLogDao,
    private val weightDao: WeightEntryDao,
    private val foodLogRepository: FoodLogRepository,
    private val weightRepository: WeightRepository
) : RemoteGateway {

    override suspend fun upsert(entry: PendingSyncEntity) {
        when (entry.entityType) {
            PendingSyncType.FOOD_LOG -> {
                if (entry.operation == PendingSyncOperation.DELETE) return delete(entry)
                val entity = foodLogDao.getById(entry.entityId) ?: run {
                    Log.w(LogTags.SYNC, "FoodLog row ${entry.entityId.take(8)}… gone — no-op")
                    return
                }
                val domain = FoodLogLocalMapper.toDomain(entity)
                Log.d(LogTags.SYNC, "Push FOOD_LOG ${entry.entityId.take(8)}…")
                FirestorePaths.foodLog(domain.userId, domain.id)
                    .set(FoodLogMapper.toMap(domain))
                    .await()
            }
            PendingSyncType.WEIGHT_ENTRY -> {
                if (entry.operation == PendingSyncOperation.DELETE) return delete(entry)
                val entity = weightDao.getById(entry.entityId) ?: run {
                    Log.w(LogTags.SYNC, "Weight row ${entry.entityId.take(8)}… gone — no-op")
                    return
                }
                val domain = WeightLocalMapper.toDomain(entity)
                Log.d(LogTags.SYNC, "Push WEIGHT_ENTRY ${entry.entityId.take(8)}…")
                FirestorePaths.weightEntry(domain.userId, domain.id)
                    .set(WeightMapper.toMap(domain))
                    .await()
                // Mirror the weight into the profile doc so the dashboard reads
                // the same value Firestore shows on other devices.
                FirestorePaths.user(domain.userId)
                    .update("currentWeightKg", domain.weightKg)
                    .await()
            }
            else -> {
                Log.w(LogTags.SYNC, "Unknown entityType=${entry.entityType} — skipping")
            }
        }
    }

    override suspend fun delete(entry: PendingSyncEntity) {
        // DELETE operations carry only the id; the payload may already be gone
        // locally. For weight we must first resolve the user id from the row.
        when (entry.entityType) {
            PendingSyncType.FOOD_LOG -> {
                val entity = foodLogDao.getById(entry.entityId) ?: run {
                    Log.d(LogTags.SYNC, "FoodLog already gone locally — nothing to delete remotely")
                    return
                }
                Log.d(LogTags.SYNC, "Delete FOOD_LOG ${entry.entityId.take(8)}…")
                FirestorePaths.foodLog(entity.userId, entry.entityId).delete().await()
            }
            PendingSyncType.WEIGHT_ENTRY -> {
                val entity = weightDao.getById(entry.entityId) ?: run {
                    Log.d(LogTags.SYNC, "Weight entry already gone locally — nothing to delete remotely")
                    return
                }
                Log.d(LogTags.SYNC, "Delete WEIGHT_ENTRY ${entry.entityId.take(8)}…")
                FirestorePaths.weightEntry(entity.userId, entry.entityId).delete().await()
            }
            else -> Log.w(LogTags.SYNC, "Unknown entityType=${entry.entityType} for DELETE")
        }
    }

    override suspend fun refreshFromRemote(userId: String) {
        Log.d(LogTags.SYNC, "refreshFromRemote start uid=${userId.take(8)}…")
        runCatching { foodLogRepository.syncFromRemote(userId) }
            .onFailure { Log.e(LogTags.SYNC, "refresh food logs failed", it) }
        runCatching { weightRepository.syncFromRemote(userId) }
            .onFailure { Log.e(LogTags.SYNC, "refresh weights failed", it) }
        Log.d(LogTags.SYNC, "refreshFromRemote finished uid=${userId.take(8)}…")
    }
}