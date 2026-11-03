package com.example.fithub.core.sync

/**
 * Result of one `SyncManager.flush()` call — small enough to log and to feed
 * the sync banner without a second query.
 */
data class SyncReport(
    val pushed: Int,
    val failed: Int,
    val remaining: Int,
    val abortedOffline: Boolean = false
) {
    val touchedAnything: Boolean get() = pushed > 0 || failed > 0
}