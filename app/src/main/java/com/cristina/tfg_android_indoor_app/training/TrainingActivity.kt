package com.cristina.tfg_android_indoor_app.training

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cristina.tfg_android_indoor_app.R
import com.cristina.tfg_android_indoor_app.data.model.SensorReading
import com.cristina.tfg_android_indoor_app.data.model.TrainingRequest
import com.cristina.tfg_android_indoor_app.data.repository.TrainingRepository
import kotlinx.coroutines.launch

class TrainingActivity : AppCompatActivity() {

    private val repo = TrainingRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_training)

        val spinnerRooms = findViewById<Spinner>(R.id.spinnerRooms)
        val spinnerZones = findViewById<Spinner>(R.id.spinnerZones)
        val btnScan = findViewById<Button>(R.id.btnScan)
        val btnSend = findViewById<Button>(R.id.btnSend)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)

        var lastScan: List<SensorReading> = listOf()

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

        btnScan.setOnClickListener {
            lastScan = listOf(
                SensorReading("BEACON_SALON1", -65),
                SensorReading("BEACON_SALON2", -70)
            )
            tvStatus.text = "Lecturas capturadas (${lastScan.size})"
        }

        btnSend.setOnClickListener {
            val room = spinnerRooms.selectedItem as String
            val zone = spinnerZones.selectedItem as String

            val prefs = getSharedPreferences("auth", MODE_PRIVATE)
            val userId = prefs.getString("user_id", "test")!!

            val body = TrainingRequest(
                userId = userId,
                roomId = room,
                zoneId = zone,
                sensors = lastScan
            )

            lifecycleScope.launch {
                val result = repo.sendTrainingData(body)
                if (result.isSuccess) {
                    tvStatus.text = "Muestra guardada"
                } else {
                    tvStatus.text = "Error enviando muestra"
                }
            }
        }
    }
}
