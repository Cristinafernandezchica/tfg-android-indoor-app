package com.cristina.tfg_android_indoor_app.data.remote

import com.cristina.tfg_android_indoor_app.data.model.LoginRequest
import com.cristina.tfg_android_indoor_app.data.model.LoginResponse
import com.cristina.tfg_android_indoor_app.data.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<Unit>

}
