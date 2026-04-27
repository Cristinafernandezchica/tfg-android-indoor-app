package com.cristina.tfg_android_indoor_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.cristina.tfg_android_indoor_app.data.repository.AuthRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class RegisterActivity : BaseActivity() {

    private val authRepository = AuthRepository()

    private lateinit var etName: EditText
    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvError: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        hideBottomNavigation()
        hideTopAppBar()

        setupViews()
        setupListeners()
    }

    private fun setupViews() {
        etName = findViewById(R.id.etName)
        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvError = findViewById(R.id.tvRegisterError)

        tvError.visibility = android.view.View.GONE
    }

    private fun setupListeners() {
        val tvGoToLogin = findViewById<TextView>(R.id.tvGoToLogin)
        tvGoToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        val btnBack = findViewById<android.widget.ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Validación de campos vacíos
            when {
                name.isEmpty() -> {
                    showError("El nombre completo es obligatorio")
                    return@setOnClickListener
                }
                username.isEmpty() -> {
                    showError("El nombre de usuario es obligatorio")
                    return@setOnClickListener
                }
                email.isEmpty() -> {
                    showError("El correo electrónico es obligatorio")
                    return@setOnClickListener
                }
                password.isEmpty() -> {
                    showError("La contraseña es obligatoria")
                    return@setOnClickListener
                }
                password.length < 6 -> {
                    showError("La contraseña debe tener al menos 6 caracteres")
                    return@setOnClickListener
                }
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    showError("Ingresa un correo electrónico válido")
                    return@setOnClickListener
                }
            }

            clearError()
            btnRegister.isEnabled = false
            btnRegister.text = "Registrando..."

            lifecycleScope.launch {
                val result = authRepository.register(username, email, password, name)
                result
                    .onSuccess { token ->
                        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
                        prefs.edit().putString("token", token).apply()

                        Toast.makeText(this@RegisterActivity, "Registro correcto", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                        finish()
                    }
                    .onFailure { e ->
                        btnRegister.isEnabled = true
                        btnRegister.text = "Registrarse"
                        showError(e.message ?: "Error al registrar usuario")
                    }
            }
        }

        // Limpiar errores cuando el usuario empieza a escribir
        etName.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) clearError() }
        etUsername.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) clearError() }
        etEmail.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) clearError() }
        etPassword.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) clearError() }
    }

    private fun showError(message: String) {
        tvError.text = message
        tvError.visibility = android.view.View.VISIBLE
    }

    private fun clearError() {
        tvError.visibility = android.view.View.GONE
    }
}