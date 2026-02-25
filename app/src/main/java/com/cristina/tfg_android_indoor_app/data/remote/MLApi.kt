package com.cristina.tfg_android_indoor_app.data.remote

import com.cristina.tfg_android_indoor_app.data.model.PositionResponse
import com.cristina.tfg_android_indoor_app.data.model.UpdatePositionRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface MLApi {

    @POST("sensors/ml/reset_training")
    suspend fun resetTraining(): Void

    @POST("sensors/ml/train")
    suspend fun trainModel(): Void

    @POST("sensors/ml/reload_models")
    suspend fun reloadModels(): Void

    @POST("sensors/ml/status")
    suspend fun getTrainingStatus(): List<Any>

    @POST("sensors/update_position")
    suspend fun updatePosition(@Body body: UpdatePositionRequest): PositionResponse

}
