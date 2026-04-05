package com.cristina.tfg_android_indoor_app.data.model

import com.google.gson.annotations.SerializedName

data class DetectOnceResponse(
    @SerializedName("room") val room: String?,
    @SerializedName("zone") val zone: String?,
    @SerializedName("confidence") val confidence: Float,
    @SerializedName("sensors_count") val sensorsCount: Int
)