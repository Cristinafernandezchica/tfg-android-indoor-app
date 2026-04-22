package com.cristina.tfg_android_indoor_app.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

interface PositionApi {
    @POST("position/heartbeat")
    suspend fun sendHeartbeat(@Body body: Map<String, String>): retrofit2.Response<Unit>
}