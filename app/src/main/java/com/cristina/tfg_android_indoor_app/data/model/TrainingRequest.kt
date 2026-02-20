package com.cristina.tfg_android_indoor_app.data.model

import com.google.gson.annotations.SerializedName

data class TrainingRequest(
    @SerializedName("user_id") val userId: String,
    @SerializedName("room_id") val roomId: String,
    @SerializedName("zone_id") val zoneId: String?,
    val sensors: List<SensorReading>
)


data class SensorReading(
    @SerializedName("sensor_id") val sensor_id: String,
    val rssi: Int
)
