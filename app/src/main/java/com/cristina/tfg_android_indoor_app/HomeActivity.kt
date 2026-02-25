package com.cristina.tfg_android_indoor_app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.cristina.tfg_android_indoor_app.training.TrainingActivity
import com.cristina.tfg_android_indoor_app.training.TrainingAdminActivity
import com.cristina.tfg_android_indoor_app.ui.userlist.UserListActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        bottomNav.selectedItemId = R.id.nav_home
        // requestAllPermissions() // Permisos necesarios para el uso de la aplicación
        requestBlePermissionsIfNeeded() // Pedir permisos

        val btnUserList = findViewById<Button>(R.id.btnUserList)
        val btnTraining = findViewById<Button>(R.id.btnTraining)

        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val role = prefs.getString("role", "user")

        if (role == "admin") {
            btnUserList.visibility = View.VISIBLE
            btnTraining.visibility = View.VISIBLE
        }

        btnUserList.setOnClickListener {
            startActivity(Intent(this, UserListActivity::class.java))
        }

        btnTraining.setOnClickListener {
            startActivity(Intent(this, TrainingActivity::class.java))
        }

        btnTraining.setOnClickListener {
            startActivity(Intent(this, TrainingAdminActivity::class.java))
        }

    }


    // Pedir permisos necesarios para usar la aplicación
    private fun requestAllPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            1001
        )
    }

    // Funciones para pedir permisos (sin pedirlo siempre, solo si es necesario y aún no los ha dado)
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
