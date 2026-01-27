package com.cristina.tfg_android_indoor_app

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cristina.tfg_android_indoor_app.data.repository.AuthRepository
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val etName = findViewById<EditText>(R.id.etProfileName)
        val etEmail = findViewById<EditText>(R.id.etProfileEmail)
        val etPassword = findViewById<EditText>(R.id.etProfilePassword)
        val btnSave = findViewById<Button>(R.id.btnSaveProfile)

        val token = getSharedPreferences("auth", MODE_PRIVATE)
            .getString("token", "") ?: ""

        lifecycleScope.launch {
            val result = authRepository.getCurrentUser(token)
            result
                .onSuccess { user ->
                    val prefs = getSharedPreferences("auth", MODE_PRIVATE)
                    prefs.edit().putString("role", user.role).apply()
                    etName.setText(user.name)
                    etEmail.setText(user.email)
                }
                .onFailure {
                    Toast.makeText(this@ProfileActivity, "Error cargando datos", Toast.LENGTH_SHORT).show()
                }
        }

        btnSave.setOnClickListener {
            val name = etName.text.toString()
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            lifecycleScope.launch {
                val result = authRepository.updateUser(token, name, email, password)
                result
                    .onSuccess {
                        Toast.makeText(this@ProfileActivity, "Datos actualizados", Toast.LENGTH_SHORT).show()
                    }
                    .onFailure { e ->
                        Toast.makeText(this@ProfileActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}

