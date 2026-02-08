package com.cristina.tfg_android_indoor_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.cristina.tfg_android_indoor_app.ui.userlist.UserListActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Referencias
        val btnUserList = findViewById<MaterialButton>(R.id.btnUserList)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)

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

        // Para manejo del navbar
        bottomNav.selectedItemId = R.id.nav_settings

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                R.id.nav_map -> {
                    startActivity(Intent(this, MapActivity::class.java))
                    true
                }
                else -> false
            }
        }

    }
}
