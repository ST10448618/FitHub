package com.example.fithub.data.remote.api

import com.example.fithub.core.RetrofitProvider

class OpenFoodFactsDataSource(
    private val api: OpenFoodFactsApi =
        RetrofitProvider.openFoodFacts.create(OpenFoodFactsApi::class.java)
) {
    suspend fun byBarcode(barcode: String): OffProduct? =
        api.getProductByBarcode(barcode).product

    suspend fun search(terms: String): List<OffProduct> =
        api.searchProducts(terms).products.orEmpty()

    /**
     * Category browse via the search endpoint.
     * Uses OFF category tags (e.g. "fruits", "snacks") as a tag filter.
     */
    suspend fun byCategory(offTag: String): List<OffProduct> =
        api.searchByCategory(tag = offTag).products.orEmpty()
}