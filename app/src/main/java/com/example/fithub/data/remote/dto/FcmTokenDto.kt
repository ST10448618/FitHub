package com.example.fithub.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FcmTokenRequest(
    @SerialName("token") val token: String,
    @SerialName("language") val language: String = "en",
)

@Serializable
data class FcmTokenResponse(
    @SerialName("ok") val ok: Boolean = false,
    @SerialName("updatedAt") val updatedAt: String = "",
    @SerialName("tokenPrefix") val tokenPrefix: String = "",
    @SerialName("previousTokenPrefix") val previousTokenPrefix: String? = null,
)