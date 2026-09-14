package com.example.fithub.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OpenFoodFactsApi {

    @GET("api/v0/product/{barcode}.json")
    suspend fun getProductByBarcode(
        @Path("barcode") barcode: String
    ): OffProductResponse

    /**
     * Search endpoint — the only reliably-working OFF endpoint from Android.
     */
    @GET("cgi/search.pl")
    suspend fun searchProducts(
        @Query("search_terms") terms: String,
        @Query("search_simple") simple: Int = 1,
        @Query("action") action: String = "process",
        @Query("json") json: Int = 1,
        @Query("page_size") pageSize: Int = 30,
        @Query("page") page: Int = 1,
        @Query("fields") fields: String =
            "code,product_name,product_name_en,brands,image_url,image_front_url,serving_size,serving_quantity,categories_tags,nutriments"
    ): OffSearchResponse

    /**
     * Category browse — via the search endpoint using tag filters.
     * We do NOT use /category/{slug}.json because OFF redirects to a
     * facet endpoint that frequently returns 503.
     */
    @GET("cgi/search.pl")
    suspend fun searchByCategory(
        @Query("action") action: String = "process",
        @Query("json") json: Int = 1,
        @Query("page_size") pageSize: Int = 30,
        @Query("page") page: Int = 1,
        @Query("tagtype_0") tagType: String = "categories",
        @Query("tag_contains_0") tagContains: String = "contains",
        @Query("tag_0") tag: String,
        @Query("fields") fields: String =
            "code,product_name,product_name_en,brands,image_url,image_front_url,serving_size,serving_quantity,categories_tags,nutriments"
    ): OffSearchResponse
}