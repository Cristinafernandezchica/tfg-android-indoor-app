package com.cristina.tfg_android_indoor_app.beacons

object RoomDetector {

    private const val WINDOW_SIZE = 5
    private val lastRooms: ArrayDeque<String> = ArrayDeque()

    fun detectRoom(detectedBeacons: List<DetectedBeacon>): String? {
        val known = detectedBeacons.filter { it.roomId != null }
        if (known.isEmpty()) return null

        val strongest = known.maxByOrNull { it.rssi } ?: return null
        val room = strongest.roomId!!

        lastRooms.addLast(room)
        if (lastRooms.size > WINDOW_SIZE) {
            lastRooms.removeFirst()
        }

        val mostFrequent = lastRooms
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key

        return mostFrequent
    }
}
