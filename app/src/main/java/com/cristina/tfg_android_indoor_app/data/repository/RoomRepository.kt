package com.cristina.tfg_android_indoor_app.data.repository

import com.cristina.tfg_android_indoor_app.data.remote.ApiClient

class RoomRepository {

    private val api = ApiClient.trainingApi

    suspend fun getRooms() = api.getRooms()

    suspend fun getVisitsCurrent() = api.getVisitsCurrent()

    suspend fun getVisitsAt(roomId: String, timestamp: String) =
        api.getVisitsAt(roomId, timestamp)

    suspend fun getOccupancy() = api.getOccupancy()

    suspend fun getOccupancyAt(roomId: String, timestamp: String) =
        api.getOccupancyAt(roomId, timestamp)
}
