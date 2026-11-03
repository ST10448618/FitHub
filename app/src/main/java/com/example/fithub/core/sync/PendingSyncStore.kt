package com.example.fithub.core.sync

import com.example.fithub.data.local.entity.PendingSyncEntity
import kotlinx.coroutines.flow.Flow

interface PendingSyncStore {
    suspend fun enqueue(entry: PendingSyncEntity)
    suspend fun loadPending(userId: String): List<PendingSyncEntity>
    suspend fun count(userId: String): Int
    fun observeCount(userId: String): Flow<Int>
    suspend fun markFailed(entry: PendingSyncEntity, error: String)
    suspend fun remove(entry: PendingSyncEntity)
    suspend fun exists(entityType: String, entityId: String): Boolean
}