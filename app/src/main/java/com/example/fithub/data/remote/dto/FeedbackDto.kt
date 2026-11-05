package com.example.fithub.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeedbackRequest(
    @SerialName("rating") val rating: Int,
    @SerialName("message") val message: String? = null,
    @SerialName("appVersion") val appVersion: String? = null,
)

@Serializable
data class FeedbackResponse(
    @SerialName("ok") val ok: Boolean = false,
    @SerialName("id") val id: String = "",
    @SerialName("createdAt") val createdAt: String = "",
)