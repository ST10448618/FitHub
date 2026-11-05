// AI-assisted (DeepSeek); reviewed and tested by the FitHub team.
package com.example.fithub.data.repository

import android.util.Log
import com.example.fithub.core.LogTags
import com.example.fithub.core.Resource
import com.example.fithub.core.ServiceLocator
import com.example.fithub.core.await
import com.example.fithub.core.sync.SyncScheduler
import com.example.fithub.data.local.dao.PendingSyncDao
import com.example.fithub.data.local.dao.UserProfileDao
import com.example.fithub.data.local.dao.WeightEntryDao
import com.example.fithub.data.local.entity.PendingSyncEntity
import com.example.fithub.data.local.entity.PendingSyncOperation
import com.example.fithub.data.local.entity.PendingSyncType
import com.example.fithub.data.local.mapper.WeightLocalMapper
import com.example.fithub.data.remote.FirestorePaths
import com.example.fithub.data.remote.mapper.WeightMapper
import com.example.fithub.domain.model.WeightEntry
import com.example.fithub.domain.repository.WeightRepository
import com.example.fithub.util.IdGenerator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime

class WeightRepositoryImpl(
    private val dao: WeightEntryDao,
    private val userDao: UserProfileDao,
    private val pendingSyncDao: PendingSyncDao
) : WeightRepository {

    override fun observeAll(uid: String): Flow<List<WeightEntry>> =
        dao.observeAll(uid).map { list -> list.map(WeightLocalMapper::toDomain) }

    override fun observeLatest(uid: String): Flow<WeightEntry?> =
        dao.observeLatest(uid).map { it?.let(WeightLocalMapper::toDomain) }

    override suspend fun getLatest(uid: String): WeightEntry? =
        dao.getLatest(uid)?.let(WeightLocalMapper::toDomain)

    override suspend fun addEntry(entry: WeightEntry): Resource<Unit> = try {
        dao.upsert(WeightLocalMapper.toEntity(entry))
        userDao.updateCurrentWeight(
            entry.userId,
            entry.weightKg,
            LocalDateTime.now().toString()
        )
        enqueueOutbox(entry.userId, entry.id, PendingSyncOperation.UPSERT)
        Log.d(LogTags.SYNC, "Weight add queued id=${entry.id.take(8)}…")
        Resource.Success(Unit)
    } catch (e: Exception) {
        Log.e(LogTags.SYNC, "Weight add failed (local write)", e)
        Resource.Error(e.message ?: "Failed to save weight entry", e)
    }

    override suspend fun deleteEntry(entryId: String): Resource<Unit> = try {
        val entity = dao.getById(entryId)
            ?: return Resource.Error("Weight entry not found")
        dao.delete(entryId)
        enqueueOutbox(entity.userId, entryId, PendingSyncOperation.DELETE)
        Log.d(LogTags.SYNC, "Weight delete queued id=${entryId.take(8)}…")
        Resource.Success(Unit)
    } catch (e: Exception) {
        Log.e(LogTags.SYNC, "Weight delete failed (local write)", e)
        Resource.Error(e.message ?: "Failed to delete weight entry", e)
    }

    override suspend fun syncFromRemote(uid: String): Resource<Unit> = try {
        val snapshot = FirestorePaths.weightEntries(uid).get().await()
        val entries = snapshot.documents.mapNotNull { doc ->
            val map = doc.data ?: return@mapNotNull null
            WeightMapper.fromMap(doc.id, uid, map)
        }
        dao.upsertAll(entries.map(WeightLocalMapper::toEntity))
        Log.d(LogTags.SYNC, "Weight syncFromRemote loaded ${entries.size} rows")
        Resource.Success(Unit)
    } catch (e: Exception) {
        Log.e(LogTags.SYNC, "Weight syncFromRemote failed", e)
        Resource.Error(e.message ?: "Failed to sync weight entries", e)
    }

    override suspend fun updatePhotoUrl(entryId: String, photoUrl: String?): Resource<Unit> = try {
        dao.updatePhotoUrl(entryId, photoUrl)
        Log.d(LogTags.STORAGE, "photoUrl updated locally for id=${entryId.take(8)}…")
        Resource.Success(Unit)
    } catch (e: Exception) {
        Log.e(LogTags.STORAGE, "updatePhotoUrl failed", e)
        Resource.Error(e.message ?: "Failed to save photo URL", e)
    }

    private suspend fun enqueueOutbox(userId: String, entityId: String, operation: String) {
        pendingSyncDao.insert(
            PendingSyncEntity(
                id = IdGenerator.newId(),
                userId = userId,
                entityType = PendingSyncType.WEIGHT_ENTRY,
                entityId = entityId,
                operation = operation,
                createdAt = System.currentTimeMillis(),
                attemptCount = 0,
                lastError = null
            )
        )
        SyncScheduler.enqueue(ServiceLocator.appContext())
    }
}