package com.cristina.tfg_android_indoor_app.data.remote

import com.cristina.tfg_android_indoor_app.data.model.LoginRequest
import com.cristina.tfg_android_indoor_app.data.model.LoginResponse
import com.cristina.tfg_android_indoor_app.data.model.RegisterRequest
import com.cristina.tfg_android_indoor_app.data.model.RegisterResponse
import com.cristina.tfg_android_indoor_app.data.model.ThresholdsRequest
import com.cristina.tfg_android_indoor_app.data.model.UpdateUserRequest
import com.cristina.tfg_android_indoor_app.data.model.UserListItem
import com.cristina.tfg_android_indoor_app.data.model.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface AuthApi {

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>

    @PUT("auth/update")
    suspend fun updateUser(
        @Header("Authorization") token: String,
        @Body request: UpdateUserRequest
    ): Response<Unit>


    @GET("auth/me")
    suspend fun getCurrentUser(
        @Header("Authorization") token: String
    ): Response<UserResponse>

    @GET("auth/users")
    suspend fun getUsers(
        @Header("Authorization") token: String,
        @Query("q") query: String
    ): Response<List<UserListItem>>


    @DELETE("auth/admin/delete/{id}")
    suspend fun deleteUser(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Unit>

    @PUT("auth/admin/reset-password/{id}")
    suspend fun resetPassword(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body body: Map<String, String>
    ): Response<Unit>

    @PUT("auth/admin/change-role/{id}")
    suspend fun changeRole(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body body: Map<String, String>
    ): Response<Unit>

    @GET("auth/me/thresholds")
    suspend fun getMyThresholds(
        @Header("Authorization") token: String
    ): Response<Map<String, Int>>

    @PUT("auth/me/thresholds")
    suspend fun setMyThresholds(
        @Header("Authorization") token: String,
        @Body body: ThresholdsRequest
    ): Response<Map<String, Int>>





}
