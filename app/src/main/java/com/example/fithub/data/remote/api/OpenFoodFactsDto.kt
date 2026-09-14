package com.example.fithub.data.remote.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OffProductResponse(
    val status: Int? = null,
    val code: String? = null,
    val product: OffProduct? = null
)

@Serializable
data class OffProduct(
    val code: String? = null,
    @SerialName("product_name") val productName: String? = null,
    @SerialName("product_name_en") val productNameEn: String? = null,
    val brands: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("image_front_url") val imageFrontUrl: String? = null,
    @SerialName("serving_size") val servingSize: String? = null,
    @SerialName("serving_quantity") val servingQuantity: Double? = null,
    @SerialName("categories_tags") val categoriesTags: List<String>? = null,
    val nutriments: OffNutriments? = null
)

@Serializable
data class OffNutriments(
    @SerialName("energy-kcal_100g") val energyKcal100g: Double? = null,
    @SerialName("energy-kcal_serving") val energyKcalServing: Double? = null,
    @SerialName("proteins_100g") val proteins100g: Double? = null,
    @SerialName("carbohydrates_100g") val carbs100g: Double? = null,
    @SerialName("fat_100g") val fat100g: Double? = null,
    @SerialName("fiber_100g") val fiber100g: Double? = null,
    @SerialName("sugars_100g") val sugars100g: Double? = null,
    @SerialName("sodium_100g") val sodium100g: Double? = null
)

@Serializable
data class OffSearchResponse(
    val count: Int? = null,
    val page: Int? = null,
    @SerialName("page_size") val pageSize: Int? = null,
    val products: List<OffProduct>? = null
)