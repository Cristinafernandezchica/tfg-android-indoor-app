package com.cristina.tfg_android_indoor_app.ui.admin

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cristina.tfg_android_indoor_app.R
import com.cristina.tfg_android_indoor_app.data.model.dto.RoomDto
import com.cristina.tfg_android_indoor_app.data.repository.RoomRepository
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditRoomsActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var rvRooms: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: RoomEditAdapter
    private val roomRepository = RoomRepository()
    private var roomsList = mutableListOf<RoomDto>()

    // Habitaciones que NO deben ser editables
    private val nonEditableRooms = setOf("PASILLO")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_rooms)

        toolbar = findViewById(R.id.toolbar)
        rvRooms = findViewById(R.id.rvRooms)
        progressBar = findViewById(R.id.progressBar)

        setupToolbar()
        setupRecyclerView()
        loadRooms()
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = RoomEditAdapter(roomsList, nonEditableRooms) { room ->
            showEditDialog(room)
        }
        rvRooms.layoutManager = LinearLayoutManager(this)
        rvRooms.adapter = adapter
    }

    private fun loadRooms() {
        progressBar.visibility = android.view.View.VISIBLE

        val token = getSharedPreferences("auth", MODE_PRIVATE)
            .getString("token", "") ?: ""

        CoroutineScope(Dispatchers.IO).launch {
            val result = roomRepository.adminGetAllRooms(token)

            withContext(Dispatchers.Main) {
                progressBar.visibility = android.view.View.GONE
                result.onSuccess { rooms ->
                    rooms.forEach { room ->
                        Log.d("EditRooms", "Room: id=${room.room_id}, name=${room.name}")
                    }
                    roomsList.clear()
                    roomsList.addAll(rooms)
                    adapter.updateRooms(roomsList)
                }.onFailure { error ->
                    Toast.makeText(this@EditRoomsActivity,
                        "Error cargando habitaciones: ${error.message}",
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showEditDialog(room: RoomDto) {
        // Verificar que room_id no sea nulo
        val realId = room.getRealId()
        if (realId == null) {
            Toast.makeText(this, "Error: ID de habitación no válido", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_room, null)
        val etName = dialogView.findViewById<EditText>(R.id.etName)
        val etDescription = dialogView.findViewById<EditText>(R.id.etDescription)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)

        etName.setText(room.name ?: "")
        etDescription.setText(room.description ?: "")

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            val newName = etName.text.toString().trim()
            val newDescription = etDescription.text.toString().trim()

            if (newName.isEmpty()) {
                Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updatedRoom = room.copy(
                room_id = realId,
                name = newName,
                description = newDescription
            )
            saveRoom(updatedRoom, dialog)
        }

        dialog.show()
    }

    private fun saveRoom(updatedRoom: RoomDto, dialog: AlertDialog) {
        val roomId = updatedRoom.getRealId()
        if (roomId == null) {
            Toast.makeText(this, "Error: ID de habitación no válido", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = android.view.View.VISIBLE

        val token = getSharedPreferences("auth", MODE_PRIVATE)
            .getString("token", "") ?: ""

        CoroutineScope(Dispatchers.IO).launch {
            val result = roomRepository.updateRoom(token, roomId, updatedRoom)

            withContext(Dispatchers.Main) {
                progressBar.visibility = android.view.View.GONE
                result.onSuccess { savedRoom ->
                    Toast.makeText(this@EditRoomsActivity,
                        "Habitación '${savedRoom.name}' actualizada",
                        Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    loadRooms()
                }.onFailure { error ->
                    Toast.makeText(this@EditRoomsActivity,
                        "Error al guardar: ${error.message}",
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}