package com.cristina.tfg_android_indoor_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.cristina.tfg_android_indoor_app.ui.userlist.UserListActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        bottomNav.selectedItemId = R.id.nav_home

        val btnUserList = findViewById<Button>(R.id.btnUserList)

        // Leemos el rol guardado
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val role = prefs.getString("role", "user")

        // Se muestra el botón solo si es admin
        if (role == "admin") {
            btnUserList.visibility = View.VISIBLE
        }

        // Acción del botón
        btnUserList.setOnClickListener {
            startActivity(Intent(this, UserListActivity::class.java))
        }

    }
}
