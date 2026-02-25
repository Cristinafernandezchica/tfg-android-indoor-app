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
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.cristina.tfg_android_indoor_app.R
import com.cristina.tfg_android_indoor_app.data.model.SensorReading
import com.cristina.tfg_android_indoor_app.data.repository.MLRepository
import kotlinx.coroutines.launch

class TrainingAdminActivity : AppCompatActivity() {

    private val repo = MLRepository()
    private val readings = mutableMapOf<String, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_training_admin)

        val btnReset = findViewById<Button>(R.id.btnReset)
        val btnTrain = findViewById<Button>(R.id.btnTrain)
        val btnReload = findViewById<Button>(R.id.btnReload)
        val btnStatus = findViewById<Button>(R.id.btnStatus)
        val tvOutput = findViewById<TextView>(R.id.tvOutput)
        val btnCapture = findViewById<Button>(R.id.btnCapture)
        val btnTestPosition = findViewById<Button>(R.id.btnTestPosition)

        // --- Botones existentes ---
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
                repo.trainModel()
                tvOutput.text = "Modelo entrenado"
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
                val status = repo.getStatus()
                tvOutput.text = status.toString()
            }
        }

        btnCapture.setOnClickListener {
            startActivity(Intent(this, TrainingActivity::class.java))
        }

        // --- NUEVO: Botón de pruebas ---
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
                    val userId = prefs.getString("user_id", "test")!!

                    val response = repo.updatePosition(userId, scanList)

                    tvOutput.text = response?.let {
                        "Habitación detectada: ${it.room}\nZona detectada: ${it.zone}"
                    } ?: "Error detectando posición"
                }

            }, 3000)
        }
    }

    // -----------------------------
    // ESCANEO BLE (igual que TrainingActivity)
    // -----------------------------

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
