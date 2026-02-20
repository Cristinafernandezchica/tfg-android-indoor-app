package com.cristina.tfg_android_indoor_app.data.model

data class TrainingStatus(
    val _id: TrainingStatusKey,
    val count: Int
)

data class TrainingStatusKey(
    val room: String,
    val zone: String?
)
