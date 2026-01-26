package com.cristina.tfg_android_indoor_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.cristina.tfg_android_indoor_app.data.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var etIdentifier: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvStatus: TextView

    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etIdentifier = findViewById(R.id.etIdentifier)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvStatus = findViewById(R.id.tvStatus)
        val tvLoginError = findViewById<TextView>(R.id.tvLoginError)
        tvLoginError.visibility = View.GONE


        btnLogin.setOnClickListener {
            val identifier = etIdentifier.text.toString()
            val password = etPassword.text.toString()

            if (identifier.isBlank() || password.isBlank()) {
                tvStatus.text = "Rellena todos los campos"
                return@setOnClickListener
            }

            tvStatus.text = "Iniciando sesión..."

            lifecycleScope.launch {
                val result = authRepository.login(identifier, password)
                result
                    .onSuccess { token ->
                        tvStatus.text = "Login correcto"
                        tvLoginError.visibility = View.GONE
                        saveToken(token)
                        // Aquí luego navegarás a la pantalla principal
                        val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                        startActivity(intent)
                        finish() // para que no vuelva al login al pulsar atrás
                    }
                    .onFailure { e ->
                        tvStatus.text = ""
                        tvLoginError.text = e.message?.replace("{\"error\":", "")?.replace("}", "")?.replace("\"", "")?.trim()
                        tvLoginError.visibility = View.VISIBLE
                    }
            }
        }

        val tvGoToRegister = findViewById<TextView>(R.id.tvGoToRegister)
        tvGoToRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }



    }

    private fun saveToken(token: String) {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        prefs.edit().putString("token", token).apply()
    }
}


