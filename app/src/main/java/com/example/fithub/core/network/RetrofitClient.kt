// AI-assisted (DeepSeek); reviewed and tested by the FitHub team.
package com.example.fithub.core.network

import com.example.fithub.data.remote.api.FitHubApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * Phase 6B: singleton Retrofit client for the FitHub REST API.
 *
 * - Auth header injected by [AuthInterceptor] on every request.
 * - kotlinx-serialization converter (NOT Gson) so DTOs are plain @Serializable classes.
 * - Logging interceptor only in debug builds.
 * - Explicit timeouts — Render free tier can spin down.
 *
 * Reference: https://square.github.io/retrofit/ (accessed February 2026)
 */
object RetrofitClient {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    val okHttpClient: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor())
            .connectTimeout(ApiConfig.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(ApiConfig.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(ApiConfig.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)

        if (com.example.fithub.BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(logging)
        }
        builder.build()
    }

    val api: FitHubApi by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConfig.baseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(FitHubApi::class.java)
    }
}