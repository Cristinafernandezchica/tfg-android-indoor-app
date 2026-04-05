package com.cristina.tfg_android_indoor_app.data.model

import com.google.gson.annotations.SerializedName

data class PositionResponse(
    @SerializedName("room") val room: String?,
    @SerializedName("zone") val zone: String?,
    @SerializedName("status") val status: String? = null,
    @SerializedName("pending_count") val pending_count: Int? = null,
    @SerializedName("confidence") val confidence: Float? = null,
    @SerializedName("message") val message: String? = null
)