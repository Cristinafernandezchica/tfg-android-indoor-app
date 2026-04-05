// Constants.kt - Ubicación: data/remote/Constants.kt
package com.cristina.tfg_android_indoor_app.data.remote

private const val LOCAL_PC_IP = "192.168.1.130" // Cambia por tu IP local
private const val LOCAL_PORT_ROOMS = "5001"
private const val LOCAL_PORT_USERS = "5002"

private const val DEPLOY = false

private val isEmulator: Boolean =
    (android.os.Build.FINGERPRINT.startsWith("generic")
            || android.os.Build.FINGERPRINT.lowercase().contains("vbox")
            || android.os.Build.FINGERPRINT.lowercase().contains("test-keys")
            || android.os.Build.MODEL.contains("Emulator")
            || android.os.Build.MODEL.contains("Android SDK built for x86")
            || android.os.Build.MANUFACTURER.contains("Genymotion")
            || (android.os.Build.BRAND.startsWith("generic") && android.os.Build.DEVICE.startsWith("generic"))
            || "google_sdk" == android.os.Build.PRODUCT)

val ROOMS_API_BASE_URL: String =
    if (isEmulator)
        "http://10.0.2.2:$LOCAL_PORT_ROOMS/"
    else if (DEPLOY)
        "https://indoor-tfg-api-rooms.onrender.com"
    else
        "http://$LOCAL_PC_IP:$LOCAL_PORT_ROOMS/"

val USERS_API_BASE_URL: String =
    if (isEmulator)
        "http://10.0.2.2:$LOCAL_PORT_USERS/"
    else if (DEPLOY)
        "https://indoor-tfg-api-users.onrender.com"
    else
        "http://$LOCAL_PC_IP:$LOCAL_PORT_USERS/"