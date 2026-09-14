package com.example.fithub.domain.repository

import com.example.fithub.core.Resource
import com.example.fithub.domain.model.Food

interface FoodRepository {

    /** Search by free text — hits remote, caches locally. */
    suspend fun search(query: String): Resource<List<Food>>

    /** Fetch by barcode — remote → local cache fallback. */
    suspend fun getByBarcode(barcode: String): Resource<Food>

    /** Browse by FitHub category. */
    suspend fun getByCategory(fithubCategory: String): Resource<List<Food>>

    /** Look up a cached food by ID. */
    suspend fun getById(id: String): Food?

    /** Save a manually entered food. */
    suspend fun saveManual(food: Food): Resource<Unit>
}