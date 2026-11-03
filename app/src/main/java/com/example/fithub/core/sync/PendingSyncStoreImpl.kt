package com.example.fithub.core.sync

import com.example.fithub.data.local.dao.PendingSyncDao
import com.example.fithub.data.local.entity.PendingSyncEntity
import kotlinx.coroutines.flow.Flow

class PendingSyncStoreImpl(
    private val dao: PendingSyncDao
) : PendingSyncStore {

    override suspend fun enqueue(entry: PendingSyncEntity) = dao.insert(entry)

    override suspend fun loadPending(userId: String): List<PendingSyncEntity> =
        dao.getAllOrdered(userId)

    override suspend fun count(userId: String): Int = dao.count(userId)

    override fun observeCount(userId: String): Flow<Int> = dao.observeCount(userId)

    override suspend fun markFailed(entry: PendingSyncEntity, error: String) {
        dao.update(entry.copy(attemptCount = entry.attemptCount + 1, lastError = error))
    }

    override suspend fun remove(entry: PendingSyncEntity) = dao.deleteById(entry.id)

    override suspend fun exists(entityType: String, entityId: String): Boolean =
        dao.existsFor(entityType, entityId)
}