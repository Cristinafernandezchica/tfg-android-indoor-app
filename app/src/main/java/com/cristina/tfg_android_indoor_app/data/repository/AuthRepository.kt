package com.cristina.tfg_android_indoor_app.data.repository

import com.cristina.tfg_android_indoor_app.data.model.LoginRequest
import com.cristina.tfg_android_indoor_app.data.model.RegisterRequest
import com.cristina.tfg_android_indoor_app.data.model.ThresholdsRequest
import com.cristina.tfg_android_indoor_app.data.model.UpdateUserRequest
import com.cristina.tfg_android_indoor_app.data.model.UserListItem
import com.cristina.tfg_android_indoor_app.data.model.UserResponse
import com.cristina.tfg_android_indoor_app.data.remote.ApiClient
import org.json.JSONObject

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


    suspend fun register(username: String, email: String, password: String, name: String): Result<String> {
        return try {
            val request = RegisterRequest(username, email, password, name)
            val response = ApiClient.authApi.register(request)

            if (response.isSuccessful) {
                val token = response.body()?.token
                if (token != null) {
                    Result.success(token)
                } else {
                    Result.failure(Exception("Token vacío"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception(parseApiError(errorBody)))
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


    private fun parseApiError(errorBody: String?): String {
        if (errorBody.isNullOrEmpty()) return "Error desconocido"

        return try {
            val json = JSONObject(errorBody)

            when {
                json.has("errors") -> {
                    val errors = json.getJSONObject("errors")
                    val messages = mutableListOf<String>()
                    errors.keys().forEach { key ->
                        val arr = errors.getJSONArray(key)
                        for (i in 0 until arr.length()) {
                            messages.add(arr.getString(i))
                        }
                    }
                    messages.joinToString("\n")
                }
                json.has("message") -> json.getString("message")
                json.has("details") -> {
                    val arr = json.getJSONArray("details")
                    (0 until arr.length()).joinToString("\n") { arr.getString(it) }
                }
                json.has("error") -> json.getString("error")   // ← AÑADIDO
                else -> "Error desconocido"
            }

        } catch (e: Exception) {
            "Error inesperado"
        }
    }

    suspend fun getMyThresholds(token: String): Result<Map<String, Int>> {
        return try {
            val response = ApiClient.authApi.getMyThresholds("Bearer $token")
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyMap())
            } else {
                Result.failure(Exception("Error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveThresholds(token: String, thresholds: Map<String, Int>): Result<Unit> {
        return try {
            val body = ThresholdsRequest(thresholds)
            val response = ApiClient.authApi.setMyThresholds("Bearer $token", body)

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

