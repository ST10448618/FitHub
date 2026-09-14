package com.example.fithub.ml

import com.example.fithub.core.Resource
import com.example.fithub.domain.model.Food

/**
 * Placeholder for online AI food recognition.
 * Track A can later wire this to CaloAI/LogMeal via RapidAPI.
 * For the prototype, it returns Resource.Error so the caller falls back to manual entry.
 */
object OnlineFoodRecognizer {
    suspend fun recognize(bytes: ByteArray): Resource<Food> =
        Resource.Error("Online AI recognition not yet configured.")
}