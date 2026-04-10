package com.cristina.tfg_android_indoor_app

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cristina.tfg_android_indoor_app.R
import com.cristina.tfg_android_indoor_app.data.repository.RoomRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class OccupancyHistoryActivity : AppCompatActivity() {

    private val repo = RoomRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_occupancy_history)

        val spinnerRooms = findViewById<Spinner>(R.id.spinnerRooms)
        val datePicker = findViewById<DatePicker>(R.id.datePicker)
        val timePicker = findViewById<TimePicker>(R.id.timePicker)
        timePicker.setIs24HourView(true)
        val btnCheck = findViewById<Button>(R.id.btnCheck)
        val tvResult = findViewById<TextView>(R.id.tvResult)

        lifecycleScope.launch {
            val rooms = repo.getRooms().body() ?: emptyList()
            spinnerRooms.adapter = ArrayAdapter(
                this@OccupancyHistoryActivity,
                android.R.layout.simple_spinner_item,
                rooms.map { it.room_id }
            )
        }

        btnCheck.setOnClickListener {
            val roomId = spinnerRooms.selectedItem as String

            val date = LocalDate.of(datePicker.year, datePicker.month + 1, datePicker.dayOfMonth)
            val time = LocalTime.of(timePicker.hour, timePicker.minute)
            val timestamp = LocalDateTime.of(date, time).toString()

            lifecycleScope.launch {
                val response = repo.getOccupancyAt(roomId, timestamp).body()
                tvResult.text = "Ocupación en ese momento: ${response?.occupancy ?: 0}"
            }
        }
    }
}
