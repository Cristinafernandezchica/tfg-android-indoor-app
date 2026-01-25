package com.cristina.tfg_android_indoor_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.cristina.tfg_android_indoor_app.data.repository.AuthRepository
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val etName = findViewById<EditText>(R.id.etName)


        btnRegister.setOnClickListener {
            val username = etUsername.text.toString()
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            val name = etName.text.toString()

            if (username.isBlank() || email.isBlank() || password.isBlank() || name.isBlank()) {
                Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val result = authRepository.register(username, email, password, name)
                result
                    .onSuccess {
                        Toast.makeText(this@RegisterActivity, "Registro correcto", Toast.LENGTH_SHORT).show()
                        finish() // vuelve al login
                    }
                    .onFailure { e ->
                        Toast.makeText(this@RegisterActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}


