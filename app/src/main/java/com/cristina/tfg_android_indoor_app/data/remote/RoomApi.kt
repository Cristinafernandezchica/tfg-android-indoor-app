package com.cristina.tfg_android_indoor_app.data.remote

import com.cristina.tfg_android_indoor_app.data.model.OccupancyResponse
import com.cristina.tfg_android_indoor_app.data.model.TrainingRequest
import com.cristina.tfg_android_indoor_app.data.model.VisitDataResponse
import com.cristina.tfg_android_indoor_app.data.model.dto.RoomDto
import com.cristina.tfg_android_indoor_app.data.model.dto.ZoneDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface RoomApi {

    @GET("rooms")
    suspend fun getRooms(): Response<List<RoomDto>>

    @GET("rooms/{roomId}/zones")
    suspend fun getZones(@Path("roomId") roomId: String): Response<List<ZoneDto>>

    @POST("sensors/training_data")
    suspend fun sendTrainingData(@Body body: TrainingRequest): Response<Unit>

    @GET("rooms/visits/current")
    suspend fun getVisitsCurrent(): Response<Map<String, VisitDataResponse>>

    @GET("rooms/visits/at")
    suspend fun getVisitsAt(
        @Query("date") date: String
    ): Response<Map<String, VisitDataResponse>>

    @GET("rooms/occupancy")
    suspend fun getOccupancy(): Response<Map<String, Int>>

    @GET("rooms/occupancy/at")
    suspend fun getOccupancyAt(
        @Query("room_id") roomId: String,
        @Query("at") timestamp: String
    ): Response<OccupancyResponse>

    @GET("rooms/admin/list")
    suspend fun adminGetAllRooms(): Response<List<RoomDto>>

    @GET("rooms/{roomId}")
    suspend fun getRoom(@Path("roomId") roomId: String): Response<RoomDto>

    @PUT("rooms/{roomId}")
    suspend fun updateRoom(
        @Header("Authorization") token: String,
        @Path("roomId") roomId: String,
        @Body room: RoomDto
    ): Response<RoomDto>

}
