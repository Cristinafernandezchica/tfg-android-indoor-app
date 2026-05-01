package com.cristina.tfg_android_indoor_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.cristina.tfg_android_indoor_app.data.repository.AuthRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.launch

class ProfileActivity : BaseActivity() {

    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        bottomNav.selectedItemId = R.id.nav_profile

        val etName = findViewById<EditText>(R.id.etProfileName)
        val etEmail = findViewById<EditText>(R.id.etProfileEmail)
        val etPassword = findViewById<EditText>(R.id.etProfilePassword)
        val btnSave = findViewById<Button>(R.id.btnSaveProfile)
        val tvProfileName = findViewById<TextView>(R.id.tvProfileName)  // Nuevo
        val tvUserEmail = findViewById<TextView>(R.id.tvUserEmail)
        val ivProfileImage = findViewById<ShapeableImageView>(R.id.ivProfileImage)

        val token = getSharedPreferences("auth", MODE_PRIVATE)
            .getString("token", "") ?: ""

        // Cargar datos del usuario
        lifecycleScope.launch {
            val result = authRepository.getCurrentUser(token)
            result
                .onSuccess { user ->
                    val prefs = getSharedPreferences("auth", MODE_PRIVATE)
                    prefs.edit().putString("role", user.role).apply()

                    // Actualizar los campos
                    etName.setText(user.name)
                    etEmail.setText(user.email)

                    // Actualizar el header con el nombre del usuario
                    tvProfileName.text = user.name
                    tvUserEmail.text = user.email

                    // Configurar iniciales del avatar (opcional)
                    val iniciales = user.name.take(2).uppercase()
                    // Puedes usar estas iniciales para un avatar personalizado
                }
                .onFailure {
                    Toast.makeText(this@ProfileActivity, "Error cargando datos", Toast.LENGTH_SHORT).show()
                    tvProfileName.text = "Usuario"
                }
        }

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Validaciones
            if (name.isEmpty()) {
                Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Ingresa un email válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Deshabilitar botón durante la operación
            btnSave.isEnabled = false
            btnSave.text = "Guardando..."

            lifecycleScope.launch {
                val result = authRepository.updateUser(token, name, email, password)
                result
                    .onSuccess {
                        Toast.makeText(this@ProfileActivity, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show()

                        // Actualizar el header con el nuevo nombre
                        tvProfileName.text = name
                        tvUserEmail.text = email
                        etPassword.text?.clear()
                    }
                    .onFailure { e ->
                        Toast.makeText(this@ProfileActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }

                btnSave.isEnabled = true
                btnSave.text = "Guardar cambios"
            }
        }

        // Botón de cerrar sesión
        val btnLogout = findViewById<MaterialButton>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Estás seguro de que quieres cerrar sesión?")
            .setPositiveButton("Sí, cerrar sesión") { _, _ ->
                val prefs = getSharedPreferences("auth", MODE_PRIVATE)
                prefs.edit().clear().apply()

                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}