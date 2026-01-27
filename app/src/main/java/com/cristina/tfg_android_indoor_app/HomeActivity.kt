package com.cristina.tfg_android_indoor_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.cristina.tfg_android_indoor_app.ui.userlist.UserListActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // 1. Referencia al botón
        val btnUserList = findViewById<Button>(R.id.btnUserList)

        // 2. Leer el rol guardado
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val role = prefs.getString("role", "user")

        // 3. Mostrar el botón solo si es admin
        if (role == "admin") {
            btnUserList.visibility = View.VISIBLE
        }

        // 4. Acción del botón
        btnUserList.setOnClickListener {
            startActivity(Intent(this, UserListActivity::class.java))
        }

        // 5. Configurar la barra inferior
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_home -> true

                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }

                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }

                else -> false
            }
        }
    }
}
