package com.cristina.tfg_android_indoor_app.beacons

data class KnownBeacon(
    val uuid: String,
    val major: Int,
    val minor: Int,
    val roomId: String
)

data class DetectedBeacon(
    val uuid: String,
    val major: Int,
    val minor: Int,
    val rssi: Int,
    val roomId: String? = null
)

object BeaconConfig {
    val knownBeacons = listOf(
        KnownBeacon(
            uuid = "DF-7E-1C-79-43-E9-44-FF-88-6F-1D-1F-7D-A6-A0-01",
            major = 10007,
            minor = 4921,
            roomId = "SALON"
        ),
        KnownBeacon(
            uuid = "DF-7E-1C-79-43-E9-44-FF-88-6F-1D-1F-7D-A6-A0-02",
            major = 10007,
            minor = 4812,
            roomId = "HAB1"
        ),
        KnownBeacon(
            uuid = "DF-7E-1C-79-43-E9-44-FF-88-6F-1D-1F-7D-A6-A0-03",
            major = 10007,
            minor = 4871,
            roomId = "HAB2"
        )
    )

    fun findRoomFor(uuid: String, major: Int, minor: Int): String? {
        return knownBeacons.firstOrNull {
            it.uuid.equals(uuid, ignoreCase = true) &&
                    it.major == major &&
                    it.minor == minor
        }?.roomId
    }
}
