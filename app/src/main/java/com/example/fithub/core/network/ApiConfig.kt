package com.example.fithub.core.network

import com.example.fithub.BuildConfig

/**
 * Phase 6B: central place for the REST API base URL and timeouts.
 *
 * The base URL comes from `BuildConfig.API_BASE_URL`, set in app/build.gradle.kts
 * so debug/release can differ without code changes. The trailing slash is
 * required by Retrofit.
 *
 * Reference: https://square.github.io/retrofit/ (accessed February 2026)
 */
object ApiConfig {

    val baseUrl: String = BuildConfig.API_BASE_URL

    /** Seconds. Render free tier can spin down; the first call after wake may take ~30 s. */
    const val CONNECT_TIMEOUT_SECONDS: Long = 30
    const val READ_TIMEOUT_SECONDS: Long = 30
    const val WRITE_TIMEOUT_SECONDS: Long = 30
}