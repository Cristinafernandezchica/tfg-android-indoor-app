package com.cristina.tfg_android_indoor_app

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.cristina.tfg_android_indoor_app.data.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginActivity : BaseActivity() {

    private var etIdentifier: EditText? = null
    private var etPassword: EditText? = null
    private var btnLogin: Button? = null
    private var tvStatus: TextView? = null
    private var tvLoginError: TextView? = null

    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Ocultar elementos de la base
        hideBottomNavigation()
        hideTopAppBar()

        setupViews()
        setupListeners()
    }

    private fun setupViews() {
        etIdentifier = findViewById(R.id.etIdentifier)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvStatus = findViewById(R.id.tvStatus)
        tvLoginError = findViewById(R.id.tvLoginError)

        tvLoginError?.visibility = View.GONE
        tvStatus?.visibility = View.GONE
    }

    private fun setupListeners() {
        btnLogin?.setOnClickListener {
            val identifier = etIdentifier?.text.toString().trim()
            val password = etPassword?.text.toString().trim()

            // Validación de campos vacíos
            when {
                identifier.isEmpty() && password.isEmpty() -> {
                    showError("Email/Usuario y contraseña son obligatorios")
                    return@setOnClickListener
                }
                identifier.isEmpty() -> {
                    showError("El email o nombre de usuario es obligatorio")
                    return@setOnClickListener
                }
                password.isEmpty() -> {
                    showError("La contraseña es obligatoria")
                    return@setOnClickListener
                }
            }

            // Limpiar errores anteriores y mostrar loading
            clearErrors()
            showLoading(true)

            lifecycleScope.launch {
                val result = authRepository.login(identifier, password)
                result
                    .onSuccess { token ->
                        showLoading(false)

                        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
                        prefs.edit().putString("token", token).apply()

                        var userName = ""
                        val userResult = authRepository.getCurrentUser(token)
                        userResult.onSuccess { user ->
                            prefs.edit()
                                .putString("role", user.role)
                                .putInt("user_id", user.id)
                                .apply()
                            userName = user.name
                        }

                        // Toast de bienvenida - INMEDIATO
                        Toast.makeText(
                            this@LoginActivity,
                            "¡Bienvenido ${if (userName.isNotEmpty()) userName else "usuario"}!",
                            Toast.LENGTH_SHORT
                        ).show()

                        startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                        finish()
                    }
                    .onFailure { e ->
                        showLoading(false)
                        val errorMessage = e.message?.replace("{\"error\":", "")?.replace("}", "")?.replace("\"", "")?.trim()
                        showError(errorMessage ?: "Credenciales incorrectas")
                    }
            }
        }

        val tvGoToRegister = findViewById<TextView>(R.id.tvGoToRegister)
        tvGoToRegister?.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Limpiar errores cuando el usuario empieza a escribir
        etIdentifier?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) clearErrors()
        }

        etPassword?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) clearErrors()
        }
    }

    private fun showError(message: String) {
        tvLoginError?.let {
            it.text = message
            it.visibility = View.VISIBLE
        }
        tvStatus?.visibility = View.GONE
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            tvStatus?.let {
                it.text = "Iniciando sesión..."
                it.visibility = View.VISIBLE
                it.setTextColor(Color.parseColor("#2563EB"))
            }
            btnLogin?.isEnabled = false
            btnLogin?.alpha = 0.7f
        } else {
            tvStatus?.visibility = View.GONE
            btnLogin?.isEnabled = true
            btnLogin?.alpha = 1f
        }
    }

    private fun clearErrors() {
        tvLoginError?.visibility = View.GONE
        tvStatus?.visibility = View.GONE
    }
}