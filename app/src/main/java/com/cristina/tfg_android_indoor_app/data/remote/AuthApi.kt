package com.cristina.tfg_android_indoor_app.data.remote

import com.cristina.tfg_android_indoor_app.data.model.LoginRequest
import com.cristina.tfg_android_indoor_app.data.model.LoginResponse
import com.cristina.tfg_android_indoor_app.data.model.RegisterRequest
import com.cristina.tfg_android_indoor_app.data.model.UpdateUserRequest
import com.cristina.tfg_android_indoor_app.data.model.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT

interface AuthApi {

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<Unit>

    @PUT("auth/update")
    suspend fun updateUser(
        @Header("Authorization") token: String,
        @Body request: UpdateUserRequest
    ): Response<Unit>


    @GET("auth/me")
    suspend fun getCurrentUser(
        @Header("Authorization") token: String
    ): Response<UserResponse>



}
