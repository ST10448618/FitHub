package com.example.fithub.data.repository

import com.example.fithub.core.CategoryMapper
import com.example.fithub.core.Resource
import com.example.fithub.data.local.dao.FoodDao
import com.example.fithub.data.local.entity.FoodEntity
import com.example.fithub.data.remote.api.OpenFoodFactsDataSource
import com.example.fithub.data.remote.mapper.FoodMapper
import com.example.fithub.domain.model.Food
import com.example.fithub.domain.model.FoodSource
import com.example.fithub.domain.repository.FoodRepository
import java.time.LocalDateTime

class FoodRepositoryImpl(
    private val dao: FoodDao,
    private val remote: OpenFoodFactsDataSource
) : FoodRepository {

    override suspend fun search(query: String): Resource<List<Food>> {
        val q = query.trim()
        if (q.length < 2) return Resource.Success(emptyList())

        return try {
            val remoteFoods = remote.search(q)
                .mapNotNull { FoodMapper.fromOff(it, FoodSource.SEARCH) }

            // Cache them (skip any without barcode/id)
            cacheAll(remoteFoods)

            // Also return any local hits (offline-first)
            val local = dao.searchByName(q, limit = 30).map(::entityToDomain)
            val merged = (remoteFoods + local)
                .distinctBy { it.id }
                .sortedBy { it.name }
            Resource.Success(merged)
        } catch (e: Exception) {
            // Fall back to cached results on network failure
            val local = dao.searchByName(q, limit = 30).map(::entityToDomain)
            if (local.isNotEmpty()) Resource.Success(local)
            else Resource.Error("Network error — try again.", e)
        }
    }

    override suspend fun getByBarcode(barcode: String): Resource<Food> {
        val cached = dao.getById("off:$barcode")
        return try {
            val product = remote.byBarcode(barcode)
            val food = product?.let { FoodMapper.fromOff(it, FoodSource.BARCODE) }
            if (food == null) {
                return if (cached != null) Resource.Success(entityToDomain(cached))
                else Resource.Error("Product not found.")
            }
            cache(food)
            Resource.Success(food)
        } catch (e: Exception) {
            if (cached != null) Resource.Success(entityToDomain(cached))
            else Resource.Error("Network error — try again.", e)
        }
    }

    override suspend fun getByCategory(fithubCategory: String): Resource<List<Food>> {
        // "All" / "Other" → cached only, no network
        if (fithubCategory == "All" || fithubCategory == "Other") {
            val cached = dao.getByCategory(fithubCategory, limit = 60).map(::entityToDomain)
            return Resource.Success(cached)
        }

        // Cache-first: if we have ≥10 cached items for this category, use them
        val cached = dao.getByCategory(fithubCategory, limit = 60).map(::entityToDomain)
        if (cached.size >= 10) return Resource.Success(cached)

        val tag = CategoryMapper.toOffCategorySlug(fithubCategory)

        return try {
            val remoteFoods = remote.byCategory(tag)
                .mapNotNull { FoodMapper.fromOff(it, FoodSource.SEARCH) }

            cacheAll(remoteFoods)

            if (remoteFoods.isEmpty()) Resource.Success(cached)
            else Resource.Success(remoteFoods)
        } catch (e: Exception) {
            if (cached.isNotEmpty()) Resource.Success(cached)
            else Resource.Error(
                "Category is temporarily unavailable. Try searching instead.", e
            )
        }
    }

    override suspend fun getById(id: String): Food? =
        dao.getById(id)?.let(::entityToDomain)

    override suspend fun saveManual(food: Food): Resource<Unit> = try {
        cache(food)
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to save food", e)
    }

    // ---------- helpers ----------

    private suspend fun cache(food: Food) {
        dao.upsert(domainToEntity(food))
    }

    private suspend fun cacheAll(foods: List<Food>) {
        if (foods.isEmpty()) return
        dao.upsertAll(foods.map(::domainToEntity))
    }

    private fun domainToEntity(f: Food) = FoodEntity(
        id = f.id,
        name = f.name,
        brand = f.brand,
        imageUrl = f.imageUrl,
        category = f.category,
        caloriesPer100g = f.caloriesPer100g,
        proteinPer100g = f.proteinPer100g,
        carbsPer100g = f.carbsPer100g,
        fatPer100g = f.fatPer100g,
        fiberPer100g = f.fiberPer100g,
        sugarPer100g = f.sugarPer100g,
        sodiumPer100g = f.sodiumPer100g,
        servingSizeG = f.servingSizeG,
        servingLabel = f.servingLabel,
        source = f.source,
        description = f.description,
        cachedAt = LocalDateTime.now()
    )

    private fun entityToDomain(e: FoodEntity) = Food(
        id = e.id,
        name = e.name,
        brand = e.brand,
        imageUrl = e.imageUrl,
        category = e.category,
        caloriesPer100g = e.caloriesPer100g,
        proteinPer100g = e.proteinPer100g,
        carbsPer100g = e.carbsPer100g,
        fatPer100g = e.fatPer100g,
        fiberPer100g = e.fiberPer100g,
        sugarPer100g = e.sugarPer100g,
        sodiumPer100g = e.sodiumPer100g,
        servingSizeG = e.servingSizeG,
        servingLabel = e.servingLabel,
        source = e.source,
        description = e.description
    )
}