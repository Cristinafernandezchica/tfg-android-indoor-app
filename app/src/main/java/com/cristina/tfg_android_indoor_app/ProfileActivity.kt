package com.cristina.tfg_android_indoor_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cristina.tfg_android_indoor_app.data.repository.AuthRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
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


        // Para cerrar sesión
        val btnLogout = findViewById<MaterialButton>(R.id.btnLogout)

        btnLogout.setOnClickListener {

            val dialogView = layoutInflater.inflate(R.layout.dialog_logout, null)
            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .create()

            dialogView.findViewById<MaterialButton>(R.id.btnConfirmLogout).setOnClickListener {
                val prefs = getSharedPreferences("auth", MODE_PRIVATE)
                prefs.edit().clear().apply()

                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }

            dialogView.findViewById<MaterialButton>(R.id.btnCancelLogout).setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()

        }



    }
}

