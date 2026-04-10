package com.cristina.tfg_android_indoor_app.ui.admin

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cristina.tfg_android_indoor_app.R
import com.cristina.tfg_android_indoor_app.data.repository.RoomRepository
import kotlinx.coroutines.launch

class AdminVisitsActivity : AppCompatActivity() {

    private val repo = RoomRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_visits)

        val spinnerRooms = findViewById<Spinner>(R.id.spinnerRooms)
        val tvVisits = findViewById<TextView>(R.id.tvVisits)
        val btnLoad = findViewById<Button>(R.id.btnLoad)

        lifecycleScope.launch {
            val rooms = repo.getRooms().body() ?: emptyList()
            spinnerRooms.adapter = ArrayAdapter(
                this@AdminVisitsActivity,
                android.R.layout.simple_spinner_item,
                rooms.map { it.room_id }
            )
        }

        btnLoad.setOnClickListener {
            val roomId = spinnerRooms.selectedItem as String

            lifecycleScope.launch {
                val visits = repo.getVisitsCurrent().body() ?: emptyMap()
                tvVisits.text = "Visitas registradas: ${visits[roomId] ?: 0}"
            }
        }
    }
}