package com.cristina.tfg_android_indoor_app.data.repository

import com.cristina.tfg_android_indoor_app.data.model.LoginRequest
import com.cristina.tfg_android_indoor_app.data.model.RegisterRequest
import com.cristina.tfg_android_indoor_app.data.remote.ApiClient

class AuthRepository {

    suspend fun login(identifier: String, password: String): Result<String> {
        return try {
            val response = ApiClient.authApi.login(LoginRequest(identifier, password))
            if (response.isSuccessful) {
                val token = response.body()?.token
                if (token != null) {
                    Result.success(token)
                } else {
                    Result.failure(Exception("Token vacío"))
                }
            } else {
                Result.failure(Exception("Error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(username: String, email: String, password: String, name: String): Result<Unit> {
        return try {
            val request = RegisterRequest(username, email, password, name)
            val response = ApiClient.authApi.register(request)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}

