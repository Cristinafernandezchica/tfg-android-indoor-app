package com.cristina.tfg_android_indoor_app.data.repository

import android.util.Log
import com.cristina.tfg_android_indoor_app.data.model.DetectOnceResponse
import com.cristina.tfg_android_indoor_app.data.model.PositionResponse
import com.cristina.tfg_android_indoor_app.data.model.SensorReading
import com.cristina.tfg_android_indoor_app.data.model.UpdatePositionRequest
import com.cristina.tfg_android_indoor_app.data.remote.ApiClient

class MLRepository {

    private val api = ApiClient.mlApi

    suspend fun resetTraining() = api.resetTraining()
    suspend fun trainModel() = api.trainModel()
    suspend fun reloadModels() = api.reloadModels()

    suspend fun updatePosition(userId: String, sensors: List<SensorReading>): PositionResponse? {
        return try {
            val response = api.updatePosition(UpdatePositionRequest(user_id = userId, sensors = sensors))
            Log.d("MLRepository", "updatePosition response: room=${response.room}, status=${response.status}")
            response
        } catch (e: Exception) {
            Log.e("MLRepository", "Error updatePosition: ${e.message}")
            null
        }
    }

    suspend fun detectOnce(userId: String, sensors: List<SensorReading>): DetectOnceResponse? {
        return try {
            val response = api.detectOnce(UpdatePositionRequest(user_id = userId, sensors = sensors))
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e("MLRepository", "detectOnce error: ${response.code()} - ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e("MLRepository", "Error detectOnce: ${e.message}")
            null
        }
    }

    /*
    suspend fun forceStartFromEntrada(userId: Int): Result<Unit> {
        return try {
            val response = api.forceStartPosition(mapOf("user_id" to userId.toString()))
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Error forzando inicio"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getConfirmedPosition(userId: Int): Result<Map<String, Any>> {
        return try {
            val response = api.getConfirmedPosition(userId.toString())
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyMap())
            } else {
                Result.failure(Exception("Error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPositionStatus(userId: Int): Result<Map<String, Any>> {
        return try {
            val response = api.getPositionStatus(userId.toString())
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyMap())
            } else {
                Result.failure(Exception("Error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    */
    suspend fun getStatus(): String {
        return try {
            val response = api.getStatus()

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                formatStatusResponse(body)
            } else {
                "Error: ${response.code()} - ${response.message()}"
            }
        } catch (e: Exception) {
            android.util.Log.e("MLRepository", "Error: ${e.message}", e)
            "Error: ${e.message}"
        }
    }

    private fun formatStatusResponse(response: Map<String, Any>): String {
        val sb = StringBuilder()
        sb.append("ESTADO DEL SISTEMA\n")
        sb.append("═".repeat(30))
        sb.append("\n\n")

        val status = response["status"] as? String ?: "unknown"
        sb.append("Estado: ${if (status == "ok") "OK" else "$status"}\n")

        val totalSamples = when (val total = response["total_samples"]) {
            is Number -> total.toInt()
            is String -> total.toIntOrNull() ?: 0
            else -> 0
        }
        sb.append("Total muestras: $totalSamples\n\n")

        val samplesByRoom = response["samples_by_room"] as? Map<*, *>

        if (samplesByRoom != null && samplesByRoom.isNotEmpty()) {
            sb.append("MUESTRAS POR HABITACIÓN:\n")
            sb.append("─".repeat(30))
            sb.append("\n")

            val sortedRooms = samplesByRoom.keys.sortedBy { it.toString() }

            for (room in sortedRooms) {
                val roomData = samplesByRoom[room] as? Map<*, *>
                val roomTotal = (roomData?.get("total") as? Number)?.toInt() ?: 0
                val zones = roomData?.get("zones") as? Map<*, *>

                sb.append("\n$room\n")
                sb.append("   Total habitación: $roomTotal muestras\n")

                if (zones != null && zones.isNotEmpty()) {
                    val sortedZones = zones.keys.sortedBy { it.toString() }
                    for (zone in sortedZones) {
                        val zoneCount = (zones[zone] as? Number)?.toInt() ?: 0
                        val percentage = if (roomTotal > 0) (zoneCount * 100.0 / roomTotal) else 0.0
                        sb.append("      $zone: $zoneCount muestras (${String.format("%.1f", percentage)}%)\n")
                    }
                } else {
                    sb.append("      No hay zonas registradas\n")
                }
            }

            sb.append("\n")
            sb.append("═".repeat(30))
            sb.append("\n")
            sb.append("Las muestras son los datos capturados\n")
            sb.append("para entrenar el modelo de posicionamiento.")
        } else {
            sb.append("No hay muestras almacenadas\n\n")
            sb.append("Para agregar muestres:\n")
            sb.append("   1. Ve a 'Capturar muestras'\n")
            sb.append("   2. Selecciona habitación y zona\n")
            sb.append("   3. Escanea beacons\n")
            sb.append("   4. Envía la muestra\n")
        }

        return sb.toString()
    }

    suspend fun sendHeartbeat(userId: String): Result<Unit> {
        return try {
            val response = ApiClient.positionApi.sendHeartbeat(mapOf("user_id" to userId))
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Error sending heartbeat"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}