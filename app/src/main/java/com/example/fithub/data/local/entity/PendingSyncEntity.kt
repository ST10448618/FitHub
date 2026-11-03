package com.example.fithub.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Reference: https://developer.android.com/training/data-storage/room/migrating-db-versions
 * (accessed February 2026)
 */
@Entity(tableName = "pending_sync")
data class PendingSyncEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val entityType: String,
    val entityId: String,
    val operation: String,
    val createdAt: Long,
    val attemptCount: Int,
    val lastError: String?
)

/** Enum-like constants — kept as strings so the SQL migration is trivial. */
object PendingSyncType {
    const val FOOD_LOG = "FOOD_LOG"
    const val WEIGHT_ENTRY = "WEIGHT_ENTRY"
}

object PendingSyncOperation {
    const val UPSERT = "UPSERT"
    const val DELETE = "DELETE"
}