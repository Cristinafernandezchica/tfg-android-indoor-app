package com.cristina.tfg_android_indoor_app.ui.userlist

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cristina.tfg_android_indoor_app.R
import com.cristina.tfg_android_indoor_app.data.repository.AuthRepository
import kotlinx.coroutines.launch

class UserListActivity : AppCompatActivity() {

    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)

        val rvUsers = findViewById<RecyclerView>(R.id.rvUsers)
        val etSearch = findViewById<EditText>(R.id.etSearch)

        rvUsers.layoutManager = LinearLayoutManager(this)

        val token = getSharedPreferences("auth", MODE_PRIVATE)
            .getString("token", "") ?: ""

        fun loadUsers(query: String = "") {
            lifecycleScope.launch {
                val result = authRepository.getUsers(token, query)
                result
                    .onSuccess { users ->
                        rvUsers.adapter = UserAdapter(
                            users,
                            onDelete = { id -> deleteUser(id, token) },
                            onResetPassword = { id -> resetPassword(id, token) },
                            onChangeRole = { id -> changeRole(id, token) }
                        )
                    }
                    .onFailure {
                        Toast.makeText(this@UserListActivity, "Error cargando usuarios", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        loadUsers()

        etSearch.addTextChangedListener {
            loadUsers(it.toString())
        }
    }

    private fun deleteUser(id: Int, token: String) {
        lifecycleScope.launch {
            val result = authRepository.deleteUser(token, id)
            result.onSuccess {
                Toast.makeText(this@UserListActivity, "Usuario eliminado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun resetPassword(id: Int, token: String) {
        val editText = EditText(this)
        editText.hint = "Nueva contraseña"

        AlertDialog.Builder(this)
            .setTitle("Cambiar contraseña")
            .setMessage("Introduce la nueva contraseña para este usuario")
            .setView(editText)
            .setPositiveButton("Aceptar") { _, _ ->
                val newPassword = editText.text.toString()

                if (newPassword.isBlank()) {
                    Toast.makeText(this, "La contraseña no puede estar vacía", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                lifecycleScope.launch {
                    val result = authRepository.resetPassword(token, id, newPassword)
                    result.onSuccess {
                        Toast.makeText(this@UserListActivity, "Contraseña actualizada", Toast.LENGTH_SHORT).show()
                    }.onFailure {
                        Toast.makeText(this@UserListActivity, "Error al cambiar contraseña", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }


    private fun changeRole(id: Int, token: String) {
        lifecycleScope.launch {
            val newRole = "admin" // o "user"
            val result = authRepository.changeRole(token, id, newRole)
            result.onSuccess {
                Toast.makeText(this@UserListActivity, "Rol actualizado", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
