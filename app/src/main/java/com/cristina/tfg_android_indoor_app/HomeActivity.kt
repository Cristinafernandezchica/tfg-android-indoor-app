package com.cristina.tfg_android_indoor_app

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.cristina.tfg_android_indoor_app.training.TrainingAdminActivity
import com.cristina.tfg_android_indoor_app.ui.admin.AdminVisitsActivity
import com.cristina.tfg_android_indoor_app.ui.admin.EditRoomsActivity
import com.cristina.tfg_android_indoor_app.ui.userlist.UserListActivity
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class HomeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        bottomNav.selectedItemId = R.id.nav_home

        requestBlePermissionsIfNeeded()
        checkBluetoothEnabled()

        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        val tvUserName = findViewById<TextView>(R.id.tvUserName)
        val chipRole = findViewById<Chip>(R.id.chipRole)
        val tvStatusMessage = findViewById<TextView>(R.id.tvStatusMessage)
        val layoutAdminButtons = findViewById<View>(R.id.layoutAdminButtons)

        // Configurar cards clickeables (ahora son LinearLayout)
        val cardMap = findViewById<LinearLayout>(R.id.cardMap)
        val cardProfile = findViewById<LinearLayout>(R.id.cardProfile)
        val cardUserList = findViewById<LinearLayout>(R.id.cardUserList)
        val cardTraining = findViewById<LinearLayout>(R.id.cardTraining)
        val cardVisits = findViewById<LinearLayout>(R.id.cardVisits)
        val cardEditRooms = findViewById<LinearLayout>(R.id.cardEditRooms)

        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""
        val role = prefs.getString("role", "user")
        val isAdmin = role == "admin"

        // Cargar nombre del usuario
        lifecycleScope.launch {
            val authRepository = com.cristina.tfg_android_indoor_app.data.repository.AuthRepository()
            val result = authRepository.getCurrentUser(token)
            result.onSuccess { user ->
                tvUserName.text = user.name
                tvWelcome.text = "¡Hola, ${user.name.split(" ").first()}!"
            }
        }

        // Configurar según rol
        if (isAdmin) {
            chipRole.text = "ADMIN"
            chipRole.chipBackgroundColor = android.content.res.ColorStateList.valueOf(
                android.graphics.Color.parseColor("#DC2626")
            )
            layoutAdminButtons.visibility = View.VISIBLE
        } else {
            chipRole.text = "USER"
            chipRole.chipBackgroundColor = android.content.res.ColorStateList.valueOf(
                android.graphics.Color.parseColor("#2563EB")
            )
            layoutAdminButtons.visibility = View.GONE
        }

        // Verificar Bluetooth
        updateBluetoothStatus(tvStatusMessage)

        // Listeners
        cardMap.setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
        }

        cardProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        if (isAdmin) {
            cardUserList.setOnClickListener {
                startActivity(Intent(this, UserListActivity::class.java))
            }

            cardTraining.setOnClickListener {
                startActivity(Intent(this, TrainingAdminActivity::class.java))
            }

            cardVisits.setOnClickListener {
                startActivity(Intent(this, AdminVisitsActivity::class.java))
            }

            cardEditRooms.setOnClickListener {
                startActivity(Intent(this, EditRoomsActivity::class.java))
            }
        }
    }

    private fun updateBluetoothStatus(tvStatusMessage: TextView) {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            tvStatusMessage.text = "Este dispositivo no soporta Bluetooth"
        } else if (bluetoothAdapter.isEnabled) {
            tvStatusMessage.text = "Bluetooth activado - Posicionamiento disponible"
        } else {
            tvStatusMessage.text = "Bluetooth desactivado - Actívalo para mejor experiencia"
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

    private fun checkBluetoothEnabled() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter() ?: run {
            Toast.makeText(this, "Este dispositivo no soporta Bluetooth", Toast.LENGTH_LONG).show()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            AlertDialog.Builder(this)
                .setTitle("Bluetooth desactivado")
                .setMessage("La aplicación necesita Bluetooth para detectar tu posición en el mapa.")
                .setPositiveButton("Activar") { _, _ ->
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivityForResult(enableBtIntent, 2001)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 2001) {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            if (bluetoothAdapter?.isEnabled == true) {
                Toast.makeText(this, "Bluetooth activado", Toast.LENGTH_SHORT).show()
                recreate()
            } else {
                Toast.makeText(this, "La app necesita Bluetooth para funcionar correctamente", Toast.LENGTH_LONG).show()
            }
        }
    }
}