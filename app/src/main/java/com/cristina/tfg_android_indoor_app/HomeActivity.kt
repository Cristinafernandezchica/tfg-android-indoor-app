package com.cristina.tfg_android_indoor_app

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.cristina.tfg_android_indoor_app.training.TrainingActivity
import com.cristina.tfg_android_indoor_app.training.TrainingAdminActivity
import com.cristina.tfg_android_indoor_app.ui.admin.AdminVisitsActivity
import com.cristina.tfg_android_indoor_app.ui.admin.EditRoomsActivity
import com.cristina.tfg_android_indoor_app.ui.userlist.UserListActivity

class HomeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        bottomNav.selectedItemId = R.id.nav_home

        requestBlePermissionsIfNeeded()
        checkBluetoothEnabled()

        val btnUserList = findViewById<Button>(R.id.btnUserList)
        val btnTraining = findViewById<Button>(R.id.btnTraining)
        val btnVisitsHistory = findViewById<Button>(R.id.btnVisitsHistory)
        val btnEditRooms = findViewById<Button>(R.id.btnEditRooms)  // ← NUEVO

        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val role = prefs.getString("role", "user")
        val isAdmin = role == "admin"

        if (isAdmin) {
            btnUserList.visibility = View.VISIBLE
            btnTraining.visibility = View.VISIBLE
            btnVisitsHistory.visibility = View.VISIBLE
            btnEditRooms.visibility = View.VISIBLE  // ← NUEVO
        }

        btnUserList.setOnClickListener {
            startActivity(Intent(this, UserListActivity::class.java))
        }

        btnTraining.setOnClickListener {
            startActivity(Intent(this, TrainingAdminActivity::class.java))
        }

        btnVisitsHistory.setOnClickListener {
            startActivity(Intent(this, AdminVisitsActivity::class.java))
        }

        btnEditRooms.setOnClickListener {
            startActivity(Intent(this, EditRoomsActivity::class.java))
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
            } else {
                Toast.makeText(this, "La app necesita Bluetooth para funcionar correctamente", Toast.LENGTH_LONG).show()
            }
        }
    }
}