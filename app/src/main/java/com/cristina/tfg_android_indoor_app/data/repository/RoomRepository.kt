package com.cristina.tfg_android_indoor_app.data.repository

import com.cristina.tfg_android_indoor_app.data.model.VisitDataResponse
import com.cristina.tfg_android_indoor_app.data.model.dto.RoomDto
import com.cristina.tfg_android_indoor_app.data.remote.ApiClient

class RoomRepository {

    private val api = ApiClient.trainingApi

    suspend fun getRooms() = api.getRooms()

    suspend fun getVisitsCurrent() = api.getVisitsCurrent()

    suspend fun getVisitsAt(date: String): retrofit2.Response<Map<String, VisitDataResponse>> =
        api.getVisitsAt(date)

    suspend fun getOccupancy() = api.getOccupancy()

    suspend fun getOccupancyAt(roomId: String, timestamp: String) =
        api.getOccupancyAt(roomId, timestamp)


    suspend fun adminGetAllRooms(token: String): Result<List<RoomDto>> {
        return try {
            val response = api.adminGetAllRooms()

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRoom(roomId: String): Result<RoomDto> {
        return try {
            val response = api.getRoom(roomId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateRoom(token: String, roomId: String, room: RoomDto): Result<RoomDto> {
        return try {
            val response = api.updateRoom("Bearer $token", roomId, room)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}