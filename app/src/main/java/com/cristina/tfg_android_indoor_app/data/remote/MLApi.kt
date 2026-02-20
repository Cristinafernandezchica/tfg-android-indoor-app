package com.cristina.tfg_android_indoor_app.data.remote

import com.cristina.tfg_android_indoor_app.data.model.TrainingStatus
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.GET

interface MLApi {

    @POST("sensors/ml/reset_training")
    suspend fun resetTraining(): Response<Unit>

    @POST("sensors/ml/train")
    suspend fun trainModel(): Response<Unit>

    @POST("sensors/ml/reload_models")
    suspend fun reloadModels(): Response<Unit>

    @GET("sensors/ml/status")
    suspend fun getTrainingStatus(): Response<List<TrainingStatus>>
}
