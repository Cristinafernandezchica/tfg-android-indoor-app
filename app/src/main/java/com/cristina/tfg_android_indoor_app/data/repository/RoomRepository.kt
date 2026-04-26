package com.cristina.tfg_android_indoor_app.data.repository

import com.cristina.tfg_android_indoor_app.data.model.dto.RoomDto
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


    suspend fun adminGetAllRooms(token: String): Result<List<RoomDto>> {
        return try {
            // Algunas APIs pueden necesitar el token, otras no
            // Si tu backend NO requiere token, usa esta línea:
            val response = api.adminGetAllRooms()

            // Si tu backend SÍ requiere token, usa esta otra:
            // val response = api.adminGetAllRooms("Bearer $token")

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