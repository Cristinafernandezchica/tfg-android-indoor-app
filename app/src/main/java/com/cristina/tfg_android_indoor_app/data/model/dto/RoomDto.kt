package com.cristina.tfg_android_indoor_app.data.model.dto

import com.google.gson.annotations.SerializedName

data class RoomDto(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("room_id") val room_id: String? = null,
    val name: String? = null,
    val description: String? = null,
    val current_occupancy: Int = 0,
    val is_transit: Boolean = false,
    val poi_id: String? = null,
    val connections: List<String>? = emptyList()
) {
    // Función auxiliar para obtener el ID real
    fun getRealId(): String? = room_id ?: id
}