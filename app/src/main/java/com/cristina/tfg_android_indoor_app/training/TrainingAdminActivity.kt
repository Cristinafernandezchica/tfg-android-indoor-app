package com.cristina.tfg_android_indoor_app.training

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.cristina.tfg_android_indoor_app.BaseActivity
import com.cristina.tfg_android_indoor_app.R
import com.cristina.tfg_android_indoor_app.data.model.SensorReading
import com.cristina.tfg_android_indoor_app.data.repository.MLRepository
import kotlinx.coroutines.launch
import org.json.JSONObject

class TrainingAdminActivity : BaseActivity() {

    private val repo = MLRepository()
    private val readings = mutableMapOf<String, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_training_admin)

        hideBottomNavigation()
        hideTopAppBar()

        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val btnReset = findViewById<Button>(R.id.btnReset)
        val btnTrain = findViewById<Button>(R.id.btnTrain)
        val btnReload = findViewById<Button>(R.id.btnReload)
        val btnStatus = findViewById<Button>(R.id.btnStatus)
        val tvOutput = findViewById<TextView>(R.id.tvOutput)
        val btnCapture = findViewById<Button>(R.id.btnCapture)
        val btnTestPosition = findViewById<Button>(R.id.btnTestPosition)

        btnReset.setOnClickListener {
            lifecycleScope.launch {
                val resp = repo.resetTraining()
                tvOutput.text = if (resp.isSuccessful) {
                    "Entrenamiento borrado"
                } else {
                    "Error al borrar entrenamiento"
                }
            }
        }

        btnTrain.setOnClickListener {
            lifecycleScope.launch {
                tvOutput.text = "Entrenando modelo..."
                repo.trainModel()
                tvOutput.text = "Modelo entrenado correctamente"
            }
        }

        btnReload.setOnClickListener {
            lifecycleScope.launch {
                repo.reloadModels()
                tvOutput.text = "Modelos recargados"
            }
        }

        btnStatus.setOnClickListener {
            lifecycleScope.launch {
                try {
                    tvOutput.text = "Obteniendo estado..."
                    val status = repo.getStatus()

                    // El status puede ser un objeto, formatearlo bonito
                    val formattedStatus = when (status) {
                        is String -> {
                            try {
                                // Intentar parsear como JSON si es string
                                val json = JSONObject(status)
                                formatStatus(json)
                            } catch (e: Exception) {
                                status
                            }
                        }
                        is Map<*, *> -> {
                            formatStatusMap(status)
                        }
                        else -> {
                            status.toString()
                        }
                    }

                    tvOutput.text = formattedStatus
                } catch (e: Exception) {
                    tvOutput.text = "Error obteniendo estado: ${e.message}"
                    e.printStackTrace()
                }
            }
        }

        btnCapture.setOnClickListener {
            startActivity(Intent(this, TrainingActivity::class.java))
        }

        btnTestPosition.setOnClickListener {
            tvOutput.text = "Escaneando para posicionamiento..."

            if (!canStartScan()) {
                tvOutput.text = "No se puede escanear. Revisa permisos, Bluetooth y ubicación."
                return@setOnClickListener
            }

            startScan()

            Handler(Looper.getMainLooper()).postDelayed({
                stopScan()

                val scanList = readings.map { (id, rssi) ->
                    SensorReading(id, rssi)
                }

                if (scanList.isEmpty()) {
                    tvOutput.text = "No se detectaron beacons."
                    return@postDelayed
                }

                lifecycleScope.launch {
                    val prefs = getSharedPreferences("auth", MODE_PRIVATE)
                    val userId = when (val value = prefs.all["user_id"]) {
                        is String -> value
                        is Int -> value.toString()
                        else -> "test"
                    }

                    val result = repo.detectOnce(userId, scanList)

                    tvOutput.text = if (result != null && result.room != null) {
                        "Habitación: ${result.room}\n Zona: ${result.zone ?: "Ninguna"}\n Confianza: ${result.confidence}\n Beacons: ${result.sensorsCount}"
                    } else {
                        "No se pudo detectar la habitación\n${result?.let { "Confianza: ${it.confidence}" } ?: ""}"
                    }
                }

            }, 8000)
        }
    }

    private fun formatStatus(json: JSONObject): String {
        val sb = StringBuilder()
        sb.append("ESTADO DEL SISTEMA\n")
        sb.append("═".repeat(30))
        sb.append("\n")

        val keys = json.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = json.get(key)
            when (key) {
                "model_loaded" -> sb.append("Modelo cargado: ${if (value == true) "Sí" else "No"}\n")
                "samples_count" -> sb.append("Muestras: $value\n")
                "last_training" -> sb.append("Último entrenamiento: $value\n")
                else -> sb.append("$key: $value\n")
            }
        }
        return sb.toString()
    }

    private fun formatStatusMap(map: Map<*, *>): String {
        val sb = StringBuilder()
        sb.append("ESTADO DEL SISTEMA\n")
        sb.append("═".repeat(30))
        sb.append("\n")

        for ((key, value) in map) {
            when (key) {
                "model_loaded" -> sb.append("Modelo cargado: ${if (value == true) "Sí" else "No"}\n")
                "samples_count" -> sb.append("Muestras: $value\n")
                "last_training" -> sb.append("Último entrenamiento: $value\n")
                else -> sb.append("$key: $value\n")
            }
        }
        return sb.toString()
    }

    // ESCANEO BLE
    private fun canStartScan(): Boolean {
        if (!hasBlePermissions()) return false

        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter == null || !adapter.isEnabled) return false

        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val net = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (!gps && !net) return false

        return true
    }

    private fun startScan() {
        try {
            val scanner = BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner
            readings.clear()
            scanner.startScan(scanCallback)
        } catch (_: Exception) {}
    }

    private fun stopScan() {
        try {
            val scanner = BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner
            scanner.stopScan(scanCallback)
        } catch (_: Exception) {}
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(type: Int, result: ScanResult?) {
            result ?: return
            val beaconData = parseBeacon(result) ?: return
            val sensorId = BeaconMap.map[beaconData] ?: return
            readings[sensorId] = result.rssi
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

    private fun hasBlePermissions(): Boolean {
        val scan = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
        val connect = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
        val location = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        return scan == PackageManager.PERMISSION_GRANTED &&
                connect == PackageManager.PERMISSION_GRANTED &&
                location == PackageManager.PERMISSION_GRANTED
    }
}