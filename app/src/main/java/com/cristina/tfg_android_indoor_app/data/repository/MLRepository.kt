package com.cristina.tfg_android_indoor_app.data.repository

import com.cristina.tfg_android_indoor_app.data.remote.ApiClient

class MLRepository {

    private val api = ApiClient.mlApi

    suspend fun resetTraining() = api.resetTraining()
    suspend fun trainModel() = api.trainModel()
    suspend fun reloadModels() = api.reloadModels()
    suspend fun getStatus() = api.getTrainingStatus()
}
