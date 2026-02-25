package com.cristina.tfg_android_indoor_app.data.model

data class UpdatePositionRequest(
    val user_id: String,
    val sensors: List<SensorReading>
)
