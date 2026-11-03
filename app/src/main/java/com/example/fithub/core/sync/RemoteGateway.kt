package com.example.fithub.core.sync

import com.example.fithub.data.local.entity.PendingSyncEntity

/**
 * Abstraction over the Firestore write path so `SyncManager` can be tested on
 * the JVM without Firebase.
 *
 * Implementations are free to load the local row (via Room) to reconstruct the
 * payload, then push it to Firestore. The interface is deliberately minimal —
 * `SyncManager` should never need to know *how* the push happens.
 */
interface RemoteGateway {

    /**
     * Upserts the entity referenced by [entry]. Implementations may return
     * `null` if the local row has disappeared (deleted concurrently); the
     * manager then treats it as a successful no-op and removes the outbox row.
     */
    suspend fun upsert(entry: PendingSyncEntity)

    /** Deletes the entity referenced by [entry] from the remote store. */
    suspend fun delete(entry: PendingSyncEntity)

    /**
     * Re-reads the remote collections from Firestore into Room after a flush.
     * Implementations must NOT overwrite rows that still have a pending outbox
     * entry (see SyncManager KDoc for the conflict policy).
     */
    suspend fun refreshFromRemote(userId: String)
}