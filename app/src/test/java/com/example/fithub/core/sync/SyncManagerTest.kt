package com.example.fithub.core.sync

import com.example.fithub.data.local.entity.PendingSyncEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SyncManagerTest {

    private lateinit var fakeStore: FakePendingSyncStore
    private lateinit var fakeRemote: FakeRemoteGateway
    private lateinit var syncManager: SyncManager
    private var isOnline = true

    @Before
    fun setup() {
        fakeStore = FakePendingSyncStore()
        fakeRemote = FakeRemoteGateway()
        syncManager = SyncManager(
            store = fakeStore,
            remote = fakeRemote,
            isOnline = { isOnline },
            pushTimeoutMs = 5000L
        )
    }

    @Test
    fun `flush does nothing when offline`() = runTest {
        isOnline = false
        fakeStore.enqueue(PendingSyncEntity("1", "uid", "FOOD_LOG", "e1", "UPSERT", 0, 0, null))

        val report = syncManager.flush("uid")

        assertEquals(0, report.pushed)
        assertEquals(1, report.remaining)
        assertTrue(report.abortedOffline)
        assertEquals(0, fakeRemote.pushCount) // Remote was never called
    }

    @Test
    fun `flush pushes pending items and removes them`() = runTest {
        isOnline = true
        fakeStore.enqueue(PendingSyncEntity("1", "uid", "FOOD_LOG", "e1", "UPSERT", 0, 0, null))
        fakeStore.enqueue(PendingSyncEntity("2", "uid", "WEIGHT_ENTRY", "e2", "UPSERT", 0, 0, null))

        val report = syncManager.flush("uid")

        assertEquals(2, report.pushed)
        assertEquals(0, report.remaining)
        assertEquals(2, fakeRemote.pushCount)
    }

    @Test
    fun `flush continues after a single failure`() = runTest {
        isOnline = true
        fakeStore.enqueue(PendingSyncEntity("1", "uid", "FOOD_LOG", "e1", "UPSERT", 0, 0, null))
        fakeStore.enqueue(PendingSyncEntity("2", "uid", "FOOD_LOG", "fail_me", "UPSERT", 0, 0, null))
        fakeStore.enqueue(PendingSyncEntity("3", "uid", "FOOD_LOG", "e3", "UPSERT", 0, 0, null))

        fakeRemote.shouldFailOnId = "fail_me"

        val report = syncManager.flush("uid")

        assertEquals(2, report.pushed) // 1 and 3 pushed
        assertEquals(1, report.failed) // 2 failed
        assertEquals(1, report.remaining) // 2 stays in queue
    }
}

// --- Fakes ---
class FakePendingSyncStore : PendingSyncStore {
    val items = mutableListOf<PendingSyncEntity>()

    override suspend fun enqueue(entry: PendingSyncEntity) { items.add(entry) }
    override suspend fun loadPending(userId: String) = items.filter { it.userId == userId }
    override suspend fun count(userId: String) = items.size
    override fun observeCount(userId: String) = kotlinx.coroutines.flow.flowOf(items.size)
    override suspend fun markFailed(entry: PendingSyncEntity, error: String) {
        val index = items.indexOfFirst { it.id == entry.id }
        if (index != -1) items[index] = items[index].copy(attemptCount = items[index].attemptCount + 1, lastError = error)
    }
    override suspend fun remove(entry: PendingSyncEntity) { items.removeAll { it.id == entry.id } }
    override suspend fun exists(entityType: String, entityId: String) = items.any { it.entityId == entityId }
}

class FakeRemoteGateway : RemoteGateway {
    var pushCount = 0
    var shouldFailOnId: String? = null

    override suspend fun upsert(entry: PendingSyncEntity) {
        if (entry.entityId == shouldFailOnId) throw Exception("Simulated Network Fail")
        pushCount++
    }
    override suspend fun delete(entry: PendingSyncEntity) { pushCount++ }
    override suspend fun refreshFromRemote(userId: String) {}
}