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
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.cristina.tfg_android_indoor_app.R
import com.cristina.tfg_android_indoor_app.data.model.SensorReading
import com.cristina.tfg_android_indoor_app.data.model.TrainingRequest
import com.cristina.tfg_android_indoor_app.data.repository.TrainingRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class TrainingActivity : AppCompatActivity() {

    private val repo = TrainingRepository()
    private val readings = mutableMapOf<String, Int>()
    private val roomNameToId = mutableMapOf<String, String>()
    private val zoneNameToId = mutableMapOf<String, String>()

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

        // Configurar toolbar
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val actvRooms = findViewById<AutoCompleteTextView>(R.id.spinnerRooms)
        val actvZones = findViewById<AutoCompleteTextView>(R.id.spinnerZones)
        val btnScan = findViewById<Button>(R.id.btnScan)
        val btnSend = findViewById<Button>(R.id.btnSend)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)

        // Hacer que los campos no sean editables (solo selección)
        actvRooms.isFocusable = false
        actvRooms.isClickable = true
        actvRooms.keyListener = null

        actvZones.isFocusable = false
        actvZones.isClickable = true
        actvZones.keyListener = null

        var lastScan: List<SensorReading> = listOf()

        // Cargar habitaciones
        lifecycleScope.launch {
            val rooms = repo.getRooms().body() ?: emptyList()
            val roomNames = mutableListOf<String>()

            for (room in rooms) {
                val displayName = room.name ?: room.room_id
                if (displayName != null) {
                    roomNames.add(displayName)
                    roomNameToId[displayName] = room.room_id ?: ""
                }
            }

            val adapter = ArrayAdapter(
                this@TrainingActivity,
                android.R.layout.simple_dropdown_item_1line,
                roomNames
            )
            actvRooms.setAdapter(adapter)

            if (roomNames.isNotEmpty()) {
                actvRooms.setText(roomNames[0], false)
                // Cargar zonas de la primera habitación (usando lifecycleScope)
                val firstRoomName = roomNames[0]
                lifecycleScope.launch {
                    loadZones(firstRoomName, actvZones)
                }
            }
        }

        // Cargar zonas según habitación seleccionada
        actvRooms.setOnItemClickListener { _, _, position, _ ->
            val selectedRoomName = actvRooms.adapter.getItem(position).toString()
            if (selectedRoomName.isNotEmpty()) {
                lifecycleScope.launch {
                    loadZones(selectedRoomName, actvZones)
                }
            }
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
            val selectedRoomName = actvRooms.text.toString()
            val selectedZoneName = actvZones.text.toString()

            if (selectedRoomName.isEmpty()) {
                tvStatus.text = "Selecciona una habitación"
                return@setOnClickListener
            }

            if (selectedZoneName.isEmpty()) {
                tvStatus.text = "Selecciona una zona"
                return@setOnClickListener
            }

            if (lastScan.isEmpty()) {
                tvStatus.text = "Primero escanea los beacons"
                return@setOnClickListener
            }

            tvStatus.text = "Enviando muestra..."

            lifecycleScope.launch {
                // Obtener room_id real desde el mapeo
                val roomId = roomNameToId[selectedRoomName] ?: selectedRoomName
                val zoneId = zoneNameToId[selectedZoneName] ?: selectedZoneName

                val prefs = getSharedPreferences("auth", MODE_PRIVATE)
                val userId = when (val value = prefs.all["user_id"]) {
                    is String -> value
                    is Int -> value.toString()
                    else -> "test"
                }

                val body = TrainingRequest(
                    userId = userId,
                    roomId = roomId,
                    zoneId = zoneId,
                    sensors = lastScan
                )

                val result = repo.sendTrainingData(body)
                tvStatus.text = if (result.isSuccess) "Muestra guardada correctamente" else "Error enviando muestra"
            }
        }
    }

    private suspend fun loadZones(roomName: String, actvZones: AutoCompleteTextView) {
        val roomId = roomNameToId[roomName]
        if (roomId.isNullOrEmpty()) return

        val zones = repo.getZones(roomId).body() ?: emptyList()
        val zoneNames = mutableListOf<String>()

        zoneNameToId.clear()
        for (zone in zones) {
            val zoneId = zone.zone_id
            if (zoneId != null) {
                zoneNames.add(zoneId)
                zoneNameToId[zoneId] = zoneId
            }
        }

        val zoneAdapter = ArrayAdapter(
            this@TrainingActivity,
            android.R.layout.simple_dropdown_item_1line,
            zoneNames
        )
        actvZones.setAdapter(zoneAdapter)

        if (zoneNames.isNotEmpty()) {
            actvZones.setText(zoneNames[0], false)
        } else {
            actvZones.setText("")
        }
    }


    private fun canStartScan(): Boolean {
        if (!hasBlePermissions()) {
            Toast.makeText(this, "Faltan permisos BLE", Toast.LENGTH_LONG).show()
            requestBlePermissionsIfNeeded()
            return false
        }

        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter == null) {
            Toast.makeText(this, "Este dispositivo no soporta Bluetooth", Toast.LENGTH_LONG).show()
            return false
        }
        if (!adapter.isEnabled) {
            Toast.makeText(this, "Activa el Bluetooth para escanear", Toast.LENGTH_LONG).show()
            return false
        }

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