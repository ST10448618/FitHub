package com.example.fithub.util

import java.util.UUID

/**
 * Centralised ID generation. Every new record in FitHub gets its ID here.
 * Keeps ID format consistent across domain models, Room entities, and Firestore.
 */
object IdGenerator {

    /** Random UUID string — used for all user-generated records. */
    fun newId(): String = UUID.randomUUID().toString()

    /**
     * Deterministic composite ID — useful for join tables where we want
     * to guarantee uniqueness without random UUIDs.
     * Example: workout_exercise:{planId}:{orderIndex}
     */
    fun compositeId(vararg parts: Any): String =
        parts.joinToString(":") { it.toString() }

    /** Food ID from a barcode, so cached foods can be looked up by barcode. */
    fun foodIdFromBarcode(barcode: String): String = "off:$barcode"

    /**
     * Stable ID for singleton-per-user records (nutrition goals, workout goals,
     * checkpoint schedule). Repeated saves overwrite the same row.
     */
    fun userSingletonId(prefix: String, userId: String): String = "${prefix}_$userId"
}