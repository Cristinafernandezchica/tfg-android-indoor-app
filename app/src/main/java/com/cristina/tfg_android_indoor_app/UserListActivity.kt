package com.cristina.tfg_android_indoor_app.ui.userlist

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cristina.tfg_android_indoor_app.HomeActivity
import com.cristina.tfg_android_indoor_app.ProfileActivity
import com.cristina.tfg_android_indoor_app.R
import com.cristina.tfg_android_indoor_app.SettingsActivity
import com.cristina.tfg_android_indoor_app.data.model.UserListItem
import com.cristina.tfg_android_indoor_app.data.repository.AuthRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class UserListActivity : AppCompatActivity() {

    private val authRepository = AuthRepository()

    // Lista mutable para poder actualizarla
    private val users = mutableListOf<UserListItem>()

    // Adapter único reutilizable
    private lateinit var adapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)

        val rvUsers = findViewById<RecyclerView>(R.id.rvUsers)
        val etSearch = findViewById<EditText>(R.id.etSearch)

        rvUsers.layoutManager = LinearLayoutManager(this)

        val token = getSharedPreferences("auth", MODE_PRIVATE)
            .getString("token", "") ?: ""

        // Inicializamos el adapter una sola vez
        adapter = UserAdapter(
            users,
            onDelete = { id -> deleteUser(id, token) },
            onResetPassword = { id -> resetPassword(id, token) },
            onChangeRole = { id -> changeRole(id, token) }
        )

        rvUsers.adapter = adapter

        // Función para cargar usuarios y refrescar la lista
        fun loadUsers(query: String = "") {
            lifecycleScope.launch {
                val result = authRepository.getUsers(token, query)
                result.onSuccess { newUsers ->
                    users.clear()
                    users.addAll(newUsers)
                    adapter.notifyDataSetChanged()
                }.onFailure {
                    Toast.makeText(this@UserListActivity, "Error cargando usuarios", Toast.LENGTH_SHORT).show()
                }
            }
        }

        loadUsers()

        etSearch.addTextChangedListener {
            loadUsers(it.toString())
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
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

    private fun deleteUser(id: Int, token: String) {
        lifecycleScope.launch {
            val result = authRepository.deleteUser(token, id)
            result.onSuccess {
                Toast.makeText(this@UserListActivity, "Usuario eliminado", Toast.LENGTH_SHORT).show()
                reload()
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
                        reload()
                    }.onFailure {
                        Toast.makeText(this@UserListActivity, "Error al cambiar contraseña", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun changeRole(id: Int, token: String) {
        val roles = arrayOf("user", "admin")

        AlertDialog.Builder(this)
            .setTitle("Cambiar rol")
            .setItems(roles) { _, which ->
                val selectedRole = roles[which]

                lifecycleScope.launch {
                    val result = authRepository.changeRole(token, id, selectedRole)
                    result.onSuccess {
                        Toast.makeText(this@UserListActivity, "Rol cambiado a $selectedRole", Toast.LENGTH_SHORT).show()
                        reload()
                    }.onFailure {
                        Toast.makeText(this@UserListActivity, "Error al cambiar rol", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .show()
    }

    // Función auxiliar para recargar la lista
    private fun reload() {
        val token = getSharedPreferences("auth", MODE_PRIVATE)
            .getString("token", "") ?: ""

        lifecycleScope.launch {
            val result = authRepository.getUsers(token, "")
            result.onSuccess { newUsers ->
                users.clear()
                users.addAll(newUsers)
                adapter.notifyDataSetChanged()
            }
        }
    }
}
