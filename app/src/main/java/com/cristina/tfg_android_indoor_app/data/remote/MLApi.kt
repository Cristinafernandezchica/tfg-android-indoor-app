package com.cristina.tfg_android_indoor_app.data.remote

import com.cristina.tfg_android_indoor_app.data.model.DetectOnceResponse
import com.cristina.tfg_android_indoor_app.data.model.PositionResponse
import com.cristina.tfg_android_indoor_app.data.model.UpdatePositionRequest
import retrofit2.Response
import retrofit2.http.*

interface MLApi {

    @POST("sensors/ml/reset_training")
    suspend fun resetTraining(): Response<Unit>

    @POST("sensors/ml/train")
    suspend fun trainModel(): Response<Unit>

    @POST("sensors/ml/reload_models")
    suspend fun reloadModels(): Response<Unit>

    @POST("sensors/ml/status")
    suspend fun getTrainingStatus(): List<Any>

    @POST("sensors/update_position")
    suspend fun updatePosition(@Body body: UpdatePositionRequest): PositionResponse

    @POST("sensors/detect_once")
    suspend fun detectOnce(@Body body: UpdatePositionRequest): Response<DetectOnceResponse>

    @GET("sensors/ml/status")
    suspend fun getStatus(): Response<Map<String, Any>>

    @POST("sensors/ml/heartbeat")
    suspend fun sendHeartbeat(@Body request: Map<String, String>): Response<Unit>

    /*
    @POST("position/force_start")
    suspend fun forceStartPosition(
        @Body body: Map<String, String>
    ): Response<Map<String, Any>>

    @GET("position/confirmed_position/{user_id}")
    suspend fun getConfirmedPosition(
        @Path("user_id") userId: String
    ): Response<Map<String, Any>>

    @GET("position/position_status/{user_id}")
    suspend fun getPositionStatus(
        @Path("user_id") userId: String
    ): Response<Map<String, Any>>
    */

}