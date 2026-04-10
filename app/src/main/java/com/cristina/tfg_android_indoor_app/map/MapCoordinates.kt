package com.cristina.tfg_android_indoor_app.map

object MapCoordinates {
    // Dimensiones originales de la imagen (880 x 1029)
    private const val ORIGINAL_WIDTH = 880
    private const val ORIGINAL_HEIGHT = 1029

    // Coordenadas de los CENTROS de las habitaciones (solo estos se dibujan como puntos)
    val roomCenters = mapOf(
        "ENTRADA" to Pair(62f, 940f),
        "SALON" to Pair(377f, 887f),
        "COCINA" to Pair(150f, 680f),
        "HAB1" to Pair(568f, 654f),
        "BAN2" to Pair(210f, 437f),
        "HAB2" to Pair(166f, 177f),
        "HAB3" to Pair(527f, 179f)
    )

    // Centro del pasillo (solo para guiar la línea, no se dibuja como habitación)
    private val corridorCenter = Pair(367f, 547f)

    // Coordenadas de las PUERTAS (solo para guiar la línea, no se dibujan)
    private val doorCoordinates = mapOf(
        "ENTRADA_door" to Pair(64f, 1022f),
        "ENTRADA_SALON_door" to Pair(119f, 915f),
        "SALON_PASILLO_door" to Pair(360f, 767f),
        "COCINA_PASILLO_door" to Pair(310f, 730f),
        "HAB1_PASILLO_door" to Pair(418f, 631f),
        "BAN2_PASILLO_door" to Pair(361f, 424f),
        "HAB2_PASILLO_door" to Pair(320f, 314f),
        "HAB3_PASILLO_door" to Pair(420f, 313f)
    )

    // Mapeo de qué puerta usar para salir de cada habitación
    private val exitDoors = mapOf(
        "ENTRADA" to "ENTRADA_SALON_door",
        "SALON" to "SALON_PASILLO_door",
        "COCINA" to "COCINA_PASILLO_door",
        "HAB1" to "HAB1_PASILLO_door",
        "BAN2" to "BAN2_PASILLO_door",
        "HAB2" to "HAB2_PASILLO_door",
        "HAB3" to "HAB3_PASILLO_door"
    )

    // Mapeo de qué puerta usar para entrar a cada habitación
    private val entryDoors = mapOf(
        "ENTRADA" to "ENTRADA_door",
        "SALON" to "ENTRADA_SALON_door",
        "COCINA" to "COCINA_PASILLO_door",
        "HAB1" to "HAB1_PASILLO_door",
        "BAN2" to "BAN2_PASILLO_door",
        "HAB2" to "HAB2_PASILLO_door",
        "HAB3" to "HAB3_PASILLO_door"
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
        if (roomId == "PASILLO") {
            return scaleCoordinates(corridorCenter.first, corridorCenter.second)
        }
        return roomCenters[roomId]?.let { scaleCoordinates(it.first, it.second) }
    }


    private fun getDoorCoordinate(doorId: String): Pair<Float, Float>? {
        return doorCoordinates[doorId]?.let { scaleCoordinates(it.first, it.second) }
    }

    // Helpers para no repetir scale en cada ruta
    private fun rc(room: String) = getRoomCenter(room)
    private fun door(id: String) = getDoorCoordinate(id)
    private fun pasilloCenter() = scaleCoordinates(corridorCenter.first, corridorCenter.second)

    /**
     * Genera la ruta completa entre dos habitaciones
     * Usando las combinaciones explícitas que has definido para evitar atravesar paredes.
     */
    fun generateRouteBetweenRooms(fromRoom: String, toRoom: String): List<Pair<Float, Float>> {
        // Primero intentamos la ruta directa definida
        val direct = generateExplicitRoute(fromRoom, toRoom)
        if (direct.isNotEmpty()) return direct

        // Si no hay ruta explícita, probamos la inversa y la invertimos
        val reverse = generateExplicitRoute(toRoom, fromRoom)
        if (reverse.isNotEmpty()) return reverse.reversed()

        // Fallback genérico (por si añades habitaciones nuevas)
        val points = mutableListOf<Pair<Float, Float>>()

        rc(fromRoom)?.let { points.add(it) }

        exitDoors[fromRoom]?.let { doorId ->
            door(doorId)?.let { points.add(it) }
        }

        entryDoors[toRoom]?.let { doorId ->
            door(doorId)?.let { points.add(it) }
        }

        rc(toRoom)?.let { points.add(it) }

        return points
    }

    /**
     * Rutas explícitas según las combinaciones que has descrito.
     * Solo se llama desde generateRouteBetweenRooms.
     */
    private fun generateExplicitRoute(fromRoom: String, toRoom: String): List<Pair<Float, Float>> {
        val p = mutableListOf<Pair<Float, Float>>()

        fun addRoom(room: String) {
            rc(room)?.let { p.add(it) }
        }

        fun addDoor(id: String) {
            door(id)?.let { p.add(it) }
        }

        fun addPasillo() {
            p.add(pasilloCenter())
        }

        when {
            // -------------------------
            // DESDE ENTRADA
            // -------------------------
            fromRoom == "ENTRADA" && toRoom == "HAB2" -> {
                addRoom("ENTRADA")
                addDoor("ENTRADA_SALON_door")
                addDoor("SALON_PASILLO_door")
                addPasillo()
                addDoor("HAB2_PASILLO_door")
                addRoom("HAB2")
            }

            fromRoom == "ENTRADA" && toRoom == "HAB1" -> {
                addRoom("ENTRADA")
                addDoor("ENTRADA_SALON_door")
                addRoom("SALON")
                addDoor("SALON_PASILLO_door")
                addDoor("HAB1_PASILLO_door")
                addRoom("HAB1")
            }

            fromRoom == "ENTRADA" && toRoom == "COCINA" -> {
                addRoom("ENTRADA")
                addDoor("ENTRADA_SALON_door")
                addRoom("SALON")
                addDoor("SALON_PASILLO_door")
                addDoor("COCINA_PASILLO_door")
                addRoom("COCINA")
            }

            fromRoom == "ENTRADA" && toRoom == "HAB3" -> {
                addRoom("ENTRADA")
                addDoor("ENTRADA_SALON_door")
                addDoor("SALON_PASILLO_door")
                addPasillo()
                addDoor("HAB3_PASILLO_door")
                addRoom("HAB3")
            }

            fromRoom == "ENTRADA" && toRoom == "BAN2" -> {
                addRoom("ENTRADA")
                addDoor("ENTRADA_SALON_door")
                addDoor("SALON_PASILLO_door")
                addPasillo()
                addDoor("BAN2_PASILLO_door")
                addRoom("BAN2")
            }

            fromRoom == "ENTRADA" && toRoom == "SALON" -> {
                addRoom("ENTRADA")
                addDoor("ENTRADA_SALON_door")
                addRoom("SALON")
            }

            // -------------------------
            // DESDE SALON
            // -------------------------
            fromRoom == "SALON" && toRoom == "COCINA" -> {
                addRoom("SALON")
                addDoor("SALON_PASILLO_door")
                addDoor("COCINA_PASILLO_door")
                addRoom("COCINA")
            }

            fromRoom == "SALON" && toRoom == "HAB1" -> {
                addRoom("SALON")
                addDoor("SALON_PASILLO_door")
                addDoor("HAB1_PASILLO_door")
                addRoom("HAB1")
            }

            fromRoom == "SALON" && toRoom == "BAN2" -> {
                addRoom("SALON")
                addDoor("SALON_PASILLO_door")
                addPasillo()
                addDoor("BAN2_PASILLO_door")
                addRoom("BAN2")
            }

            fromRoom == "SALON" && toRoom == "HAB2" -> {
                addRoom("SALON")
                addDoor("SALON_PASILLO_door")
                addPasillo()
                addDoor("HAB2_PASILLO_door")
                addRoom("HAB2")
            }

            fromRoom == "SALON" && toRoom == "HAB3" -> {
                addRoom("SALON")
                addDoor("SALON_PASILLO_door")
                addPasillo()
                addDoor("HAB3_PASILLO_door")
                addRoom("HAB3")
            }

            // -------------------------
            // DESDE COCINA
            // -------------------------
            fromRoom == "COCINA" && toRoom == "HAB1" -> {
                addRoom("COCINA")
                addDoor("COCINA_PASILLO_door")
                addDoor("HAB1_PASILLO_door")
                addRoom("HAB1")
            }

            fromRoom == "COCINA" && toRoom == "BAN2" -> {
                addRoom("COCINA")
                addDoor("COCINA_PASILLO_door")
                addPasillo()
                addDoor("BAN2_PASILLO_door")
                addRoom("BAN2")
            }

            fromRoom == "COCINA" && toRoom == "HAB2" -> {
                addRoom("COCINA")
                addDoor("COCINA_PASILLO_door")
                addPasillo()
                addDoor("HAB2_PASILLO_door")
                addRoom("HAB2")
            }

            fromRoom == "COCINA" && toRoom == "HAB3" -> {
                addRoom("COCINA")
                addDoor("COCINA_PASILLO_door")
                addPasillo()
                addDoor("HAB3_PASILLO_door")
                addRoom("HAB3")
            }

            // -------------------------
            // DESDE HAB1
            // -------------------------
            fromRoom == "HAB1" && toRoom == "BAN2" -> {
                addRoom("HAB1")
                addDoor("HAB1_PASILLO_door")
                addPasillo()
                addDoor("BAN2_PASILLO_door")
                addRoom("BAN2")
            }

            fromRoom == "HAB1" && toRoom == "HAB2" -> {
                addRoom("HAB1")
                addDoor("HAB1_PASILLO_door")
                addPasillo()
                addDoor("HAB2_PASILLO_door")
                addRoom("HAB2")
            }

            fromRoom == "HAB1" && toRoom == "HAB3" -> {
                addRoom("HAB1")
                addDoor("HAB1_PASILLO_door")
                addPasillo()
                addDoor("HAB3_PASILLO_door")
                addRoom("HAB3")
            }

            // -------------------------
            // DESDE BAN2
            // -------------------------
            fromRoom == "BAN2" && toRoom == "HAB2" -> {
                addRoom("BAN2")
                addDoor("BAN2_PASILLO_door")
                addDoor("HAB2_PASILLO_door")
                addRoom("HAB2")
            }

            fromRoom == "BAN2" && toRoom == "HAB3" -> {
                addRoom("BAN2")
                addDoor("BAN2_PASILLO_door")
                addDoor("HAB3_PASILLO_door")
                addRoom("HAB3")
            }

            // -------------------------
            // DESDE HAB2
            // -------------------------
            fromRoom == "HAB2" && toRoom == "HAB3" -> {
                addRoom("HAB2")
                addDoor("HAB2_PASILLO_door")
                addDoor("HAB3_PASILLO_door")
                addRoom("HAB3")
            }

            else -> {
                // No hay ruta explícita definida
                return emptyList()
            }
        }

        return p
    }

    /**
     * Genera la ruta completa para una lista de habitaciones
     */
    fun generateFullRoute(route: List<String>): List<Pair<Float, Float>> {
        if (route.isEmpty()) return emptyList()
        if (route.size == 1) return getRoomCenter(route[0])?.let { listOf(it) } ?: emptyList()

        val allPoints = mutableListOf<Pair<Float, Float>>()

        for (i in 0 until route.size - 1) {
            val fromRoom = route[i]
            val toRoom = route[i + 1]

            val segmentPoints = generateRouteBetweenRooms(fromRoom, toRoom)

            if (allPoints.isEmpty()) {
                allPoints.addAll(segmentPoints)
            } else {
                allPoints.addAll(segmentPoints.drop(1))
            }
        }

        return allPoints
    }

    // Para compatibilidad
    fun getPixelCoordinates(poiId: String): Pair<Float, Float>? {
        val roomId = when (poiId) {
            "poi_57fd1fd2-fc14-47fd-b6df-e2f8589a3e7f" -> "ENTRADA"
            "poi_5ab33651-9e9f-444e-8023-c57dce5d276d" -> "SALON"
            "poi_fbc620c5-0578-43e1-b04b-9d9a93239d7d" -> "COCINA"
            "poi_b9f47ce4-59d2-4015-b923-e0d3fab646ea" -> "HAB1"
            "poi_70b24188-a590-4beb-b003-5aa9e7b44b95" -> "BAN2"
            "poi_bedbfa50-eeca-40a4-8562-78799e66c2b3" -> "HAB2"
            "poi_f93ff721-4606-45dc-9fcc-bf1d1d00b920" -> "HAB3"
            else -> return null
        }
        return getRoomCenter(roomId)
    }
}
