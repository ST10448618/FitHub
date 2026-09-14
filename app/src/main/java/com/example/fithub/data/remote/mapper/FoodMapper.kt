package com.example.fithub.data.remote.mapper

import com.example.fithub.core.CategoryMapper
import com.example.fithub.data.remote.api.OffProduct
import com.example.fithub.domain.model.Food
import com.example.fithub.domain.model.FoodSource
import com.example.fithub.util.IdGenerator

object FoodMapper {

    fun fromOff(product: OffProduct, source: FoodSource): Food? {
        val barcode = product.code ?: return null
        val name = product.productNameEn?.takeIf { it.isNotBlank() }
            ?: product.productName?.takeIf { it.isNotBlank() }
            ?: return null

        val n = product.nutriments

        return Food(
            id = IdGenerator.foodIdFromBarcode(barcode),
            name = name,
            brand = product.brands?.substringBefore(",")?.trim(),
            imageUrl = product.imageFrontUrl ?: product.imageUrl,
            category = CategoryMapper.mapOffCategories(product.categoriesTags),
            caloriesPer100g = n?.energyKcal100g,
            proteinPer100g = n?.proteins100g,
            carbsPer100g = n?.carbs100g,
            fatPer100g = n?.fat100g,
            fiberPer100g = n?.fiber100g,
            sugarPer100g = n?.sugars100g,
            sodiumPer100g = n?.sodium100g?.times(1000),  // OFF stores g, we store mg
            servingSizeG = product.servingQuantity,
            servingLabel = product.servingSize,
            source = source,
            description = null
        )
    }
}