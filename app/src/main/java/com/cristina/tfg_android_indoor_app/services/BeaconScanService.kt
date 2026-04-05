package com.cristina.tfg_android_indoor_app.services

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.cristina.tfg_android_indoor_app.data.model.SensorReading
import com.cristina.tfg_android_indoor_app.data.repository.MLRepository
import com.cristina.tfg_android_indoor_app.training.BeaconMap
import kotlinx.coroutines.*

class BeaconScanService : android.app.Service() {

    private val repo = MLRepository()
    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false
    private var scanRunnable: Runnable? = null
    private val readings = mutableMapOf<String, Int>()

    companion object {
        var isServiceRunning = false
        private const val SCAN_INTERVAL_MS = 5000L
        private const val SCAN_DURATION_MS = 5000L

        const val ACTION_POSITION_UPDATE = "com.cristina.tfg.POSITION_UPDATE"
        const val EXTRA_ROOM = "room"
        const val EXTRA_ZONE = "zone"
        const val EXTRA_STATUS = "status"
        const val EXTRA_PENDING_COUNT = "pending_count"
    }

    private fun sendPositionBroadcast(room: String?, zone: String?, status: String, pendingCount: Int = 0) {
        val intent = Intent(ACTION_POSITION_UPDATE).apply {
            putExtra(EXTRA_ROOM, room)
            putExtra(EXTRA_ZONE, zone)
            putExtra(EXTRA_STATUS, status)
            putExtra(EXTRA_PENDING_COUNT, pendingCount)
        }
        sendBroadcast(intent)
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("BeaconScanService", "Servicio creado")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isServiceRunning) {
            isServiceRunning = true
            Log.d("BeaconScanService", "Iniciando escaneo continuo automático")
            startContinuousScan()
        }
        return START_STICKY
    }

    private fun startContinuousScan() {
        scanRunnable = object : Runnable {
            override fun run() {
                if (isServiceRunning) {
                    performScan()
                    handler.postDelayed(this, SCAN_INTERVAL_MS)
                }
            }
        }
        handler.post(scanRunnable!!)
    }

    private fun performScan() {
        if (!hasBlePermissions()) {
            Log.e("BeaconScanService", "Faltan permisos BLE")
            return
        }

        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter == null || !adapter.isEnabled) {
            Log.e("BeaconScanService", "Bluetooth no disponible")
            return
        }

        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!gpsEnabled && !networkEnabled) {
            Log.e("BeaconScanService", "Ubicación no activada")
            return
        }

        readings.clear()

        try {
            val scanner = adapter.bluetoothLeScanner
            scanner.startScan(scanCallback)

            handler.postDelayed({
                try {
                    scanner.stopScan(scanCallback)
                    processScanResults()
                } catch (e: Exception) {
                    Log.e("BeaconScanService", "Error deteniendo escaneo: ${e.message}")
                }
            }, SCAN_DURATION_MS)
        } catch (e: SecurityException) {
            Log.e("BeaconScanService", "No se pudo iniciar escaneo (permisos): ${e.message}")
        } catch (e: Exception) {
            Log.e("BeaconScanService", "Error iniciando escaneo: ${e.message}")
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result ?: return

            val beaconData = parseBeacon(result) ?: return
            val sensorId = BeaconMap.map[beaconData] ?: return

            readings[sensorId] = result.rssi
            Log.d("BeaconScanService", "Beacon detectado: $sensorId, RSSI: ${result.rssi}")
        }
    }

    private fun parseBeacon(result: ScanResult): Triple<String, Int, Int>? {
        val scanRecord = result.scanRecord?.bytes ?: return null

        for (i in 0 until scanRecord.size - 30) {
            if ((scanRecord[i + 2].toInt() and 0xFF) == 0x02 &&
                (scanRecord[i + 3].toInt() and 0xFF) == 0x15) {

                val uuid = String.format(
                    "%02X-%02X-%02X-%02X-%02X-%02X-%02X-%02X-%02X-%02X-%02X-%02X-%02X-%02X-%02X-%02X",
                    scanRecord[i + 4], scanRecord[i + 5], scanRecord[i + 6], scanRecord[i + 7],
                    scanRecord[i + 8], scanRecord[i + 9], scanRecord[i + 10], scanRecord[i + 11],
                    scanRecord[i + 12], scanRecord[i + 13], scanRecord[i + 14], scanRecord[i + 15],
                    scanRecord[i + 16], scanRecord[i + 17], scanRecord[i + 18], scanRecord[i + 19]
                )

                val major = (scanRecord[i + 20].toInt() and 0xFF shl 8) +
                        (scanRecord[i + 21].toInt() and 0xFF)

                val minor = (scanRecord[i + 22].toInt() and 0xFF shl 8) +
                        (scanRecord[i + 23].toInt() and 0xFF)

                return Triple(uuid, major, minor)
            }
        }
        return null
    }

    private fun processScanResults() {
        if (readings.isEmpty()) {
            Log.d("BeaconScanService", "No se detectaron beacons")
            return
        }

        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val userId = when (val value = prefs.all["user_id"]) {
            is String -> value
            is Int -> value.toString()
            else -> {
                Log.e("BeaconScanService", "Usuario no identificado")
                return
            }
        }

        val scanList = readings.map { (id, rssi) -> SensorReading(id, rssi) }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = repo.updatePosition(userId, scanList)

                withContext(Dispatchers.Main) {
                    when (response?.status) {
                        "pending" -> {
                            sendPositionBroadcast(
                                response.room,
                                null,
                                "pending",
                                response.pending_count ?: 1
                            )
                            Log.d("BeaconScanService", "🟡 Detectando: ${response.room} (${response.pending_count}/3)")
                        }
                        "confirmed", "ok" -> {
                            sendPositionBroadcast(
                                response.room,
                                response.zone,
                                "confirmed",
                                0
                            )
                            Log.d("BeaconScanService", "✅ Posición confirmada: ${response.room}")
                        }
                        else -> {
                            Log.d("BeaconScanService", "Respuesta: ${response?.room}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("BeaconScanService", "Error: ${e.message}")
            }
        }
    }

    private fun hasBlePermissions(): Boolean {
        val scan = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
        val connect = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
        val location = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        return scan == PackageManager.PERMISSION_GRANTED &&
                connect == PackageManager.PERMISSION_GRANTED &&
                location == PackageManager.PERMISSION_GRANTED
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        scanRunnable?.let { handler.removeCallbacks(it) }
        Log.d("BeaconScanService", "Servicio detenido")
    }
}