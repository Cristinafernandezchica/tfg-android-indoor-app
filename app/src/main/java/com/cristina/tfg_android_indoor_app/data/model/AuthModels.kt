package com.cristina.tfg_android_indoor_app.data.model

data class LoginRequest(
    val identifier: String,
    val password: String
)
data class LoginResponse(
    val token: String
)