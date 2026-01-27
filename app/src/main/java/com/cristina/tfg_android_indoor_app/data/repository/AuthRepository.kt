package com.cristina.tfg_android_indoor_app.data.repository

import com.cristina.tfg_android_indoor_app.data.model.LoginRequest
import com.cristina.tfg_android_indoor_app.data.model.RegisterRequest
import com.cristina.tfg_android_indoor_app.data.model.UpdateUserRequest
import com.cristina.tfg_android_indoor_app.data.model.UserListItem
import com.cristina.tfg_android_indoor_app.data.model.UserResponse
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
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception(errorBody ?: "Error desconocido"))
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

    suspend fun updateUser(token: String, name: String, email: String, password: String): Result<Unit> {
        return try {
            val request = UpdateUserRequest(name, email, password)
            val response = ApiClient.authApi.updateUser("Bearer $token", request)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun getCurrentUser(token: String): Result<UserResponse> {
        return try {
            val response = ApiClient.authApi.getCurrentUser("Bearer $token")
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUsers(token: String, query: String): Result<List<UserListItem>> {
        return try {
            val response = ApiClient.authApi.getUsers("Bearer $token", query)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteUser(token: String, id: Int): Result<Unit> {
        return try {
            val response = ApiClient.authApi.deleteUser("Bearer $token", id)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Error ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetPassword(token: String, id: Int, newPassword: String): Result<Unit> {
        return try {
            val body = mapOf("password" to newPassword)
            val response = ApiClient.authApi.resetPassword("Bearer $token", id, body)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Error ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun changeRole(token: String, id: Int, newRole: String): Result<Unit> {
        return try {
            val body = mapOf("role" to newRole)
            val response = ApiClient.authApi.changeRole("Bearer $token", id, body)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Error ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }




}

