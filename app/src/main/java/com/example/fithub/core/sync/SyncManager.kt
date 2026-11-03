package com.example.fithub.core.sync

import android.util.Log
import com.example.fithub.core.LogTags
import com.example.fithub.core.Resource
import com.example.fithub.core.ServiceLocator
import com.example.fithub.data.local.entity.PendingSyncEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Reference: https://developer.android.com/topic/architecture/data-layer/offline-first
 * (accessed February 2026)
 */
class SyncManager(
    private val store: PendingSyncStore,
    private val remote: RemoteGateway,
    private val isOnline: () -> Boolean,
    private val pushTimeoutMs: Long = 15_000L
) {

    private val running = AtomicBoolean(false)
    private val mutex = Mutex()

    /** Live count of pending rows for the banner. */
    fun observePendingCount(userId: String): Flow<Int> = store.observeCount(userId)

    /**
     * Drains the outbox for [userId]. Safe to call repeatedly; if another flush
     * is already running this call returns immediately with `pushed=0`.
     */
    suspend fun flush(userId: String): SyncReport = mutex.withLock {
        if (!isOnline()) {
            Log.d(LogTags.SYNC, "flush skipped — offline")
            return@withLock SyncReport(0, 0, store.count(userId), abortedOffline = true)
        }
        if (!running.compareAndSet(false, true)) {
            Log.d(LogTags.SYNC, "flush skipped — another flush in progress")
            return@withLock SyncReport(0, 0, store.count(userId))
        }
        try {
            val pending = store.loadPending(userId)
            if (pending.isEmpty()) {
                Log.d(LogTags.SYNC, "flush: nothing to do")
                return@withLock SyncReport(0, 0, 0)
            }
            Log.i(LogTags.SYNC, "flush start uid=${userId.take(8)}… pending=${pending.size}")

            var pushed = 0
            var failed = 0
            var aborted = false

            for (entry in pending) {
                if (!isOnline()) {
                    Log.w(LogTags.SYNC, "flush aborted mid-run — offline")
                    aborted = true
                    break
                }
                val ok = tryPush(entry)
                if (ok) {
                    store.remove(entry)
                    pushed++
                    Log.d(
                        LogTags.SYNC,
                        "push ok type=${entry.entityType} op=${entry.operation} id=${entry.entityId.take(8)}…"
                    )
                } else {
                    failed++
                }
            }

            val remaining = store.count(userId)
            if (remaining == 0) {
                // Only pull when the queue is empty — see conflict policy above.
                runCatching { remote.refreshFromRemote(userId) }
                    .onFailure { Log.e(LogTags.SYNC, "refreshFromRemote failed", it) }
            }
            Log.i(
                LogTags.SYNC,
                "flush done uid=${userId.take(8)}… pushed=$pushed failed=$failed remaining=$remaining abortedOffline=$aborted"
            )
            SyncReport(pushed, failed, remaining, aborted)
        } finally {
            running.set(false)
        }
    }

    /** Returns true when the entry was accepted remotely; false otherwise. */
    private suspend fun tryPush(entry: PendingSyncEntity): Boolean {
        return try {
            withTimeout(pushTimeoutMs) {
                if (entry.operation == com.example.fithub.data.local.entity.PendingSyncOperation.DELETE) {
                    remote.delete(entry)
                } else {
                    remote.upsert(entry)
                }
            }
            true
        } catch (t: TimeoutCancellationException) {
            Log.w(LogTags.SYNC, "push timeout type=${entry.entityType} id=${entry.entityId.take(8)}…")
            store.markFailed(entry, "timeout after ${pushTimeoutMs}ms")
            false
        } catch (t: Throwable) {
            Log.e(
                LogTags.SYNC,
                "push failed type=${entry.entityType} id=${entry.entityId.take(8)}… attempt=${entry.attemptCount + 1}",
                t
            )
            store.markFailed(entry, t.message ?: t.javaClass.simpleName)
            false
        }
    }

    // --- Convenience used by the UI/Application, mirrors the singleton wiring ---
    companion object {
        /** Flush using the process-wide singleton. Never blocks the caller for longer than a flush. */
        suspend fun flushDefault(): SyncReport {
            val uid = com.example.fithub.core.SessionManager.currentUserId
            return if (uid == null) {
                Log.d(LogTags.SYNC, "flushDefault: no session — skip")
                SyncReport(0, 0, 0)
            } else {
                ServiceLocator.syncManager.flush(uid)
            }
        }
    }
}