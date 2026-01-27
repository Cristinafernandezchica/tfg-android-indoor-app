package com.cristina.tfg_android_indoor_app.data.model

data class UserResponse(
    val id: Int,
    val name: String,
    val email: String,
    val username: String,
    val role: String?,
    val thresholds: Map<String, Int>?
)
