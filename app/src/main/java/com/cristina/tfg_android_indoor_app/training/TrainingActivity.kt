package com.cristina.tfg_android_indoor_app.training

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.cristina.tfg_android_indoor_app.R
import com.cristina.tfg_android_indoor_app.data.model.SensorReading
import com.cristina.tfg_android_indoor_app.data.model.TrainingRequest
import com.cristina.tfg_android_indoor_app.data.repository.TrainingRepository
import kotlinx.coroutines.launch

class TrainingActivity : AppCompatActivity() {

    private val repo = TrainingRepository()
    private val readings = mutableMapOf<String, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_training)

        requestBlePermissionsIfNeeded()

        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            1001
        )

        val spinnerRooms = findViewById<Spinner>(R.id.spinnerRooms)
        val spinnerZones = findViewById<Spinner>(R.id.spinnerZones)
        val btnScan = findViewById<Button>(R.id.btnScan)
        val btnSend = findViewById<Button>(R.id.btnSend)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)

        var lastScan: List<SensorReading> = listOf()

        // Cargar habitaciones
        lifecycleScope.launch {
            val rooms = repo.getRooms().body() ?: emptyList()
            val adapter = ArrayAdapter(
                this@TrainingActivity,
                android.R.layout.simple_spinner_item,
                rooms.map { it.room_id }
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerRooms.adapter = adapter
        }

        // Cargar zonas según habitación
        spinnerRooms.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val roomId = parent.getItemAtPosition(position) as String

                lifecycleScope.launch {
                    val zones = repo.getZones(roomId).body() ?: emptyList()
                    val adapter = ArrayAdapter(
                        this@TrainingActivity,
                        android.R.layout.simple_spinner_item,
                        zones.map { it.zone_id }
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerZones.adapter = adapter
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Escanear BLE
        btnScan.setOnClickListener {
            tvStatus.text = "Escaneando..."

            if (!canStartScan()) {
                tvStatus.text = "No se puede escanear. Revisa permisos, Bluetooth y ubicación."
                return@setOnClickListener
            }

            startScan()

            Handler(Looper.getMainLooper()).postDelayed({
                stopScan()

                lastScan = readings.map { (id, rssi) ->
                    SensorReading(id, rssi)
                }

                tvStatus.text = if (lastScan.isEmpty())
                    "No se detectaron beacons. Revisa Bluetooth y ubicación."
                else
                    "Lecturas capturadas (${lastScan.size})"

            }, 8000)
        }

        // Enviar muestra
        btnSend.setOnClickListener {
            val room = spinnerRooms.selectedItem as String
            val zone = spinnerZones.selectedItem as String

            val prefs = getSharedPreferences("auth", MODE_PRIVATE)
            val userId = when (val value = prefs.all["user_id"]) {
                is String -> value
                is Int -> value.toString()
                else -> "test"
            }

            val body = TrainingRequest(
                userId = userId,
                roomId = room,
                zoneId = zone,
                sensors = lastScan
            )

            lifecycleScope.launch {
                val result = repo.sendTrainingData(body)
                tvStatus.text = if (result.isSuccess) "Muestra guardada" else "Error enviando muestra"
            }
        }
    }

    // -----------------------------
    // MANEJO ROBUSTO DE ESCANEO BLE
    // -----------------------------

    private fun canStartScan(): Boolean {
        // 1. Permisos
        if (!hasBlePermissions()) {
            Toast.makeText(this, "Faltan permisos BLE", Toast.LENGTH_LONG).show()
            requestBlePermissionsIfNeeded()
            return false
        }

        // 2. Bluetooth activado
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter == null) {
            Toast.makeText(this, "Este dispositivo no soporta Bluetooth", Toast.LENGTH_LONG).show()
            return false
        }
        if (!adapter.isEnabled) {
            Toast.makeText(this, "Activa el Bluetooth para escanear", Toast.LENGTH_LONG).show()
            return false
        }

        // 3. Ubicación activada
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!gpsEnabled && !networkEnabled) {
            Toast.makeText(this, "Activa la ubicación para escanear BLE", Toast.LENGTH_LONG).show()
            return false
        }

        return true
    }

    private fun startScan() {
        try {
            val scanner = BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner
            readings.clear()
            scanner.startScan(scanCallback)
        } catch (e: SecurityException) {
            Toast.makeText(this, "No se pudo iniciar el escaneo (permisos)", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error iniciando escaneo BLE", Toast.LENGTH_LONG).show()
        }
    }

    private fun stopScan() {
        try {
            val scanner = BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner
            scanner.stopScan(scanCallback)
        } catch (_: Exception) {}
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1001) {
            if (!hasBlePermissions()) {
                Toast.makeText(this, "Los permisos BLE son necesarios para entrenar", Toast.LENGTH_LONG).show()
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

    private fun requestBlePermissionsIfNeeded() {
        if (!hasBlePermissions()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                1001
            )
        }
    }
}
