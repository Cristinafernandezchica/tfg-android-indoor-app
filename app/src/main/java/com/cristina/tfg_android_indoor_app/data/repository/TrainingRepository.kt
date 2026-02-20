package com.cristina.tfg_android_indoor_app.data.repository

import com.cristina.tfg_android_indoor_app.data.model.TrainingRequest
import com.cristina.tfg_android_indoor_app.data.remote.ApiClient

class TrainingRepository {

    private val api = ApiClient.trainingApi

    suspend fun sendTrainingData(body: TrainingRequest): Result<Unit> {
        return try {
            val response = api.sendTrainingData(body)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Error enviando datos"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRooms() = api.getRooms()

    suspend fun getZones(roomId: String) = api.getZones(roomId)

}
