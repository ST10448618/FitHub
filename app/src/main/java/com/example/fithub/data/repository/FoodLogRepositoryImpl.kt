package com.example.fithub.data.repository

import android.util.Log
import com.example.fithub.core.LogTags
import com.example.fithub.core.Resource
import com.example.fithub.core.ServiceLocator
import com.example.fithub.core.await
import com.example.fithub.core.sync.SyncScheduler
import com.example.fithub.data.local.dao.FoodLogDao
import com.example.fithub.data.local.dao.PendingSyncDao
import com.example.fithub.data.local.entity.PendingSyncEntity
import com.example.fithub.data.local.entity.PendingSyncOperation
import com.example.fithub.data.local.entity.PendingSyncType
import com.example.fithub.data.local.mapper.FoodLogLocalMapper
import com.example.fithub.data.remote.FirestorePaths
import com.example.fithub.data.remote.mapper.FoodLogMapper
import com.example.fithub.domain.model.FoodLog
import com.example.fithub.domain.repository.FoodLogRepository
import com.example.fithub.util.IdGenerator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class FoodLogRepositoryImpl(
    private val dao: FoodLogDao,
    private val pendingSyncDao: PendingSyncDao
) : FoodLogRepository {

    override fun observeByDate(uid: String, date: LocalDate): Flow<List<FoodLog>> =
        dao.observeByDate(uid, date.toString()).map { list ->
            list.map(FoodLogLocalMapper::toDomain)
        }

    override fun observeRecent(uid: String, limit: Int): Flow<List<FoodLog>> =
        dao.observeRecent(uid, limit).map { list ->
            list.map(FoodLogLocalMapper::toDomain)
        }

    override suspend fun getByDate(uid: String, date: LocalDate): List<FoodLog> =
        dao.getByDate(uid, date.toString()).map(FoodLogLocalMapper::toDomain)

    override suspend fun getBetween(uid: String, from: LocalDate, to: LocalDate): List<FoodLog> =
        dao.getBetween(uid, from.toString(), to.toString()).map(FoodLogLocalMapper::toDomain)

    /**
     * Offline-first add. Writes Room, enqueues an outbox UPSERT, fires the
     * SyncWorker, and returns immediately — the caller never blocks on
     * Firestore. `SyncManager` drains the queue when connectivity returns.
     */
    override suspend fun add(log: FoodLog): Resource<Unit> = try {
        dao.upsert(FoodLogLocalMapper.toEntity(log))
        enqueueOutbox(log.userId, log.id, PendingSyncOperation.UPSERT)
        Log.d(LogTags.SYNC, "FoodLog add queued id=${log.id.take(8)}…")
        Resource.Success(Unit)
    } catch (e: Exception) {
        Log.e(LogTags.SYNC, "FoodLog add failed (local write)", e)
        Resource.Error(e.message ?: "Failed to save food log", e)
    }

    /**
     * Offline-first delete. Must be called with the full [FoodLog] because the
     * DAO's `delete` takes the entity. Room delete + outbox DELETE, then
     * return.
     */
    override suspend fun delete(log: FoodLog): Resource<Unit> = try {
        dao.delete(FoodLogLocalMapper.toEntity(log))
        enqueueOutbox(log.userId, log.id, PendingSyncOperation.DELETE)
        Log.d(LogTags.SYNC, "FoodLog delete queued id=${log.id.take(8)}…")
        Resource.Success(Unit)
    } catch (e: Exception) {
        Log.e(LogTags.SYNC, "FoodLog delete failed (local write)", e)
        Resource.Error(e.message ?: "Failed to delete food log", e)
    }

    override suspend fun syncFromRemote(uid: String): Resource<Unit> = try {
        val snapshot = FirestorePaths.foodLogs(uid).get().await()
        val logs = snapshot.documents.mapNotNull { doc ->
            doc.data?.let { FoodLogMapper.fromMap(doc.id, uid, it) }
        }
        logs.forEach { dao.upsert(FoodLogLocalMapper.toEntity(it)) }
        Log.d(LogTags.SYNC, "FoodLog syncFromRemote loaded ${logs.size} rows")
        Resource.Success(Unit)
    } catch (e: Exception) {
        Log.e(LogTags.SYNC, "FoodLog syncFromRemote failed", e)
        Resource.Error(e.message ?: "Failed to sync food logs", e)
    }

    private suspend fun enqueueOutbox(userId: String, entityId: String, operation: String) {
        pendingSyncDao.insert(
            PendingSyncEntity(
                id = IdGenerator.newId(),
                userId = userId,
                entityType = PendingSyncType.FOOD_LOG,
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