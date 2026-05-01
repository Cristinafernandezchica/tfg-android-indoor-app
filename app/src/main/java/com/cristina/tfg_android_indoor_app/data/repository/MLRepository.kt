package com.cristina.tfg_android_indoor_app.data.repository

import android.util.Log
import com.cristina.tfg_android_indoor_app.data.model.DetectOnceResponse
import com.cristina.tfg_android_indoor_app.data.model.PositionResponse
import com.cristina.tfg_android_indoor_app.data.model.SensorReading
import com.cristina.tfg_android_indoor_app.data.model.UpdatePositionRequest
import com.cristina.tfg_android_indoor_app.data.remote.ApiClient

class MLRepository {

    private val api = ApiClient.mlApi

    suspend fun resetTraining() = api.resetTraining()
    suspend fun trainModel() = api.trainModel()
    suspend fun reloadModels() = api.reloadModels()
    suspend fun getStatus() = api.getTrainingStatus()

    suspend fun updatePosition(userId: String, sensors: List<SensorReading>): PositionResponse? {
        return try {
            val response = api.updatePosition(UpdatePositionRequest(user_id = userId, sensors = sensors))
            Log.d("MLRepository", "updatePosition response: room=${response.room}, status=${response.status}")
            response
        } catch (e: Exception) {
            Log.e("MLRepository", "Error updatePosition: ${e.message}")
            null
        }
    }

    suspend fun detectOnce(userId: String, sensors: List<SensorReading>): DetectOnceResponse? {
        return try {
            val response = api.detectOnce(UpdatePositionRequest(user_id = userId, sensors = sensors))
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e("MLRepository", "detectOnce error: ${response.code()} - ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e("MLRepository", "Error detectOnce: ${e.message}")
            null
        }
    }

    /*
    suspend fun forceStartFromEntrada(userId: Int): Result<Unit> {
        return try {
            val response = api.forceStartPosition(mapOf("user_id" to userId.toString()))
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Error forzando inicio"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getConfirmedPosition(userId: Int): Result<Map<String, Any>> {
        return try {
            val response = api.getConfirmedPosition(userId.toString())
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyMap())
            } else {
                Result.failure(Exception("Error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPositionStatus(userId: Int): Result<Map<String, Any>> {
        return try {
            val response = api.getPositionStatus(userId.toString())
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyMap())
            } else {
                Result.failure(Exception("Error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    */

    suspend fun sendHeartbeat(userId: String): Result<Unit> {
        return try {
            val response = ApiClient.positionApi.sendHeartbeat(mapOf("user_id" to userId))
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Error sending heartbeat"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}