// MapCoordinates.kt
package com.cristina.tfg_android_indoor_app.map

object MapCoordinates {
    // Dimensiones originales de la imagen (880 x 1029)
    private const val ORIGINAL_WIDTH = 880
    private const val ORIGINAL_HEIGHT = 1029

    // Coordenadas de los CENTROS de las habitaciones
    private val roomCenters = mapOf(
        "ENTRADA" to Pair(62f, 940f),
        "SALON" to Pair(377f, 887f),
        "COCINA" to Pair(150f, 680f),
        "HAB1" to Pair(568f, 654f),
        "BAN2" to Pair(210f, 437f),
        "HAB2" to Pair(166f, 177f),
        "HAB3" to Pair(527f, 179f),
        "PASILLO" to Pair(366f, 512f)
    )

    // Coordenadas de las PUERTAS
    private val doorCoordinates = mapOf(
        // Puerta de entrada desde la calle
        "ENTRADA_door" to Pair(64f, 1022f),

        // Puerta entre ENTRADA y SALON
        "ENTRADA_SALON_door" to Pair(119f, 915f),

        // Puerta entre SALON y PASILLO
        "SALON_PASILLO_door" to Pair(360f, 767f),

        // Puerta entre COCINA y PASILLO
        "COCINA_PASILLO_door" to Pair(310f, 730f),

        // Puerta entre HAB1 y PASILLO
        "HAB1_PASILLO_door" to Pair(418f, 631f),

        // Puerta entre BAN2 y PASILLO
        "BAN2_PASILLO_door" to Pair(310f, 418f),

        // Puerta entre HAB2 y PASILLO
        "HAB2_PASILLO_door" to Pair(300f, 286f),

        // Puerta entre HAB3 y PASILLO
        "HAB3_PASILLO_door" to Pair(440f, 294f)
    )

    // Mapeo de qué puertas pertenecen a cada habitación
    private val roomDoors = mapOf(
        "ENTRADA" to listOf("ENTRADA_door", "ENTRADA_SALON_door"),
        "SALON" to listOf("ENTRADA_SALON_door", "SALON_PASILLO_door"),
        "COCINA" to listOf("COCINA_PASILLO_door"),
        "HAB1" to listOf("HAB1_PASILLO_door"),
        "BAN2" to listOf("BAN2_PASILLO_door"),
        "HAB2" to listOf("HAB2_PASILLO_door"),
        "HAB3" to listOf("HAB3_PASILLO_door"),
        "PASILLO" to listOf(
            "SALON_PASILLO_door", "COCINA_PASILLO_door", "HAB1_PASILLO_door",
            "BAN2_PASILLO_door", "HAB2_PASILLO_door", "HAB3_PASILLO_door"
        )
    )

    // Orden de las puertas en el PASILLO (para rutas lineales)
    private val pasilloOrder = listOf(
        "SALON_PASILLO_door",      // (360, 767)
        "COCINA_PASILLO_door",     // (310, 730)
        "BAN2_PASILLO_door",       // (310, 418)
        "HAB2_PASILLO_door",       // (300, 286)
        "HAB3_PASILLO_door",       // (440, 294)
        "HAB1_PASILLO_door"        // (418, 631) - Nota: HAB1 está en otra rama
    )

    private var currentWidth = ORIGINAL_WIDTH
    private var currentHeight = ORIGINAL_HEIGHT

    fun updateCurrentDimensions(width: Int, height: Int) {
        currentWidth = width
        currentHeight = height
    }

    private fun scaleCoordinates(x: Float, y: Float): Pair<Float, Float> {
        val scaleX = currentWidth.toFloat() / ORIGINAL_WIDTH
        val scaleY = currentHeight.toFloat() / ORIGINAL_HEIGHT
        return Pair(x * scaleX, y * scaleY)
    }

    fun getRoomCenter(roomId: String): Pair<Float, Float>? {
        return roomCenters[roomId]?.let { scaleCoordinates(it.first, it.second) }
    }

    fun getDoorCoordinate(doorId: String): Pair<Float, Float>? {
        return doorCoordinates[doorId]?.let { scaleCoordinates(it.first, it.second) }
    }

    fun getDoorsForRoom(roomId: String): List<String> {
        return roomDoors[roomId] ?: emptyList()
    }

    /**
     * Genera la ruta completa entre dos habitaciones
     * @param fromRoom Habitación de origen
     * @param toRoom Habitación de destino
     * @return Lista de puntos (centros y puertas) que forman la ruta
     */
    fun generateRouteBetweenRooms(fromRoom: String, toRoom: String): List<Pair<Float, Float>> {
        val points = mutableListOf<Pair<Float, Float>>()

        // Añadir centro de la habitación de origen
        getRoomCenter(fromRoom)?.let { points.add(it) }

        when {
            // Caso 1: Misma habitación
            fromRoom == toRoom -> {
                return points
            }

            // Caso 2: Ambas son el PASILLO (ir de una puerta a otra)
            fromRoom == "PASILLO" && toRoom == "PASILLO" -> {
                // Esto no debería ocurrir normalmente
                return points
            }

            // Caso 3: Desde una habitación al PASILLO
            fromRoom != "PASILLO" && toRoom == "PASILLO" -> {
                val doors = getDoorsForRoom(fromRoom)
                if (doors.isNotEmpty()) {
                    getDoorCoordinate(doors[0])?.let { points.add(it) }
                }
            }

            // Caso 4: Desde el PASILLO a una habitación
            fromRoom == "PASILLO" && toRoom != "PASILLO" -> {
                val doors = getDoorsForRoom(toRoom)
                if (doors.isNotEmpty()) {
                    getDoorCoordinate(doors[0])?.let { points.add(it) }
                }
                getRoomCenter(toRoom)?.let { points.add(it) }
            }

            // Caso 5: Entre dos habitaciones diferentes (a través del PASILLO)
            else -> {
                // Puerta de salida de la habitación origen
                val fromDoors = getDoorsForRoom(fromRoom)
                if (fromDoors.isNotEmpty()) {
                    getDoorCoordinate(fromDoors[0])?.let { points.add(it) }
                }

                // Puerta de entrada a la habitación destino
                val toDoors = getDoorsForRoom(toRoom)
                if (toDoors.isNotEmpty()) {
                    getDoorCoordinate(toDoors[0])?.let { points.add(it) }
                }

                // Centro de la habitación destino
                getRoomCenter(toRoom)?.let { points.add(it) }
            }
        }

        return points
    }

    /**
     * Genera la ruta completa para una lista de habitaciones
     * @param route Lista de IDs de habitaciones en orden
     * @return Lista de puntos (centros y puertas) que forman la ruta completa
     */
    fun generateFullRoute(route: List<String>): List<Pair<Float, Float>> {
        if (route.isEmpty()) return emptyList()
        if (route.size == 1) return getRoomCenter(route[0])?.let { listOf(it) } ?: emptyList()

        val allPoints = mutableListOf<Pair<Float, Float>>()

        for (i in 0 until route.size - 1) {
            val fromRoom = route[i]
            val toRoom = route[i + 1]

            val segmentPoints = generateRouteBetweenRooms(fromRoom, toRoom)

            // Añadir puntos, evitando duplicar el último punto del segmento anterior
            if (allPoints.isEmpty()) {
                allPoints.addAll(segmentPoints)
            } else {
                // Saltar el primer punto del nuevo segmento (es el mismo que el último del anterior)
                allPoints.addAll(segmentPoints.drop(1))
            }
        }

        return allPoints
    }

    // Para mantener compatibilidad con código existente
    fun getPixelCoordinates(poiId: String): Pair<Float, Float>? {
        val roomId = when (poiId) {
            "poi_57fd1fd2-fc14-47fd-b6df-e2f8589a3e7f" -> "ENTRADA"
            "poi_5ab33651-9e9f-444e-8023-c57dce5d276d" -> "SALON"
            "poi_fbc620c5-0578-43e1-b04b-9d9a93239d7d" -> "COCINA"
            "poi_b9f47ce4-59d2-4015-b923-e0d3fab646ea" -> "HAB1"
            "poi_70b24188-a590-4beb-b003-5aa9e7b44b95" -> "BAN2"
            "poi_bedbfa50-eeca-40a4-8562-78799e66c2b3" -> "HAB2"
            "poi_f93ff721-4606-45dc-9fcc-bf1d1d00b920" -> "HAB3"
            "poi_089e6886-f194-4c5c-9e49-43b3c18a43e9" -> "PASILLO"
            else -> return null
        }
        return getRoomCenter(roomId)
    }
}