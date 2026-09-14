package com.example.fithub.core

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object RetrofitProvider {

    private const val OFF_BASE_URL = "https://world.openfoodfacts.org/"

    // OFF's preferred format: AppName - Platform - Version - Contact
    private const val USER_AGENT = "FitHub - Android - 1.0 - contact@fithub.app"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        explicitNulls = false
    }

    /** Retries 429/503 with exponential backoff (max 2 attempts). */
    private val retryInterceptor = Interceptor { chain ->
        val request = chain.request()
        var response: Response = chain.proceed(request)
        var attempt = 1
        val maxAttempts = 2

        while (!response.isSuccessful && attempt <= maxAttempts &&
            (response.code == 429 || response.code == 503)
        ) {
            response.close()
            val waitMs = 800L * attempt
            Thread.sleep(waitMs)
            response = chain.proceed(request)
            attempt++
        }
        response
    }

    private val okHttp: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val req = chain.request().newBuilder()
                    .header("User-Agent", USER_AGENT)
                    .header("Accept", "application/json")
                    .build()
                chain.proceed(req)
            }
            .addInterceptor(retryInterceptor)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                }
            )
            .build()
    }

    val openFoodFacts: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(OFF_BASE_URL)
            .client(okHttp)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }
}