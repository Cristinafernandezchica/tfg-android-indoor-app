package com.cristina.tfg_android_indoor_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.cristina.tfg_android_indoor_app.ui.userlist.UserListActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton

class SettingsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Referencias
        val btnUserList = findViewById<MaterialButton>(R.id.btnUserList)
        val btnConfigThreshold = findViewById<MaterialButton>(R.id.btnConfigThreshold)

        btnConfigThreshold.setOnClickListener {
            startActivity(Intent(this, ThresholdSettingsActivity::class.java))
        }

        // Recuperar rol del usuario
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val role = prefs.getString("role", "user")

        // Mostrar botón solo si es admin
        if (role != "admin") {
            btnUserList.visibility = View.GONE
        }

        // Navegar a lista de usuarios
        btnUserList.setOnClickListener {
            startActivity(Intent(this, UserListActivity::class.java))
        }

    }
}
