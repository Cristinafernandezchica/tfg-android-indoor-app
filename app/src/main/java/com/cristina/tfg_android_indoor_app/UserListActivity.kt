package com.cristina.tfg_android_indoor_app.ui.userlist

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cristina.tfg_android_indoor_app.BaseActivity
import com.cristina.tfg_android_indoor_app.R
import com.cristina.tfg_android_indoor_app.data.model.UserListItem
import com.cristina.tfg_android_indoor_app.data.repository.AuthRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class UserListActivity : BaseActivity() {

    private val authRepository = AuthRepository()
    private val users = mutableListOf<UserListItem>()
    private lateinit var adapter: UserAdapter
    private lateinit var tvUserCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)
        bottomNav.selectedItemId = R.id.nav_user_list

        // Ocultar top app bar personalizada y usar la nuestra
        hideTopAppBar()

        val rvUsers = findViewById<RecyclerView>(R.id.rvUsers)
        val etSearch = findViewById<EditText>(R.id.etSearch)
        tvUserCount = findViewById(R.id.tvUserCount)

        rvUsers.layoutManager = LinearLayoutManager(this)

        val token = getSharedPreferences("auth", MODE_PRIVATE)
            .getString("token", "") ?: ""

        adapter = UserAdapter(
            users,
            onDelete = { id -> deleteUser(id, token) },
            onResetPassword = { id -> resetPassword(id, token) },
            onChangeRole = { id -> changeRole(id, token) }
        )

        rvUsers.adapter = adapter

        fun loadUsers(query: String = "") {
            lifecycleScope.launch {
                val result = authRepository.getUsers(token, query)
                result.onSuccess { newUsers ->
                    users.clear()
                    users.addAll(newUsers)
                    adapter.notifyDataSetChanged()
                    tvUserCount.text = "${users.size} ${if (users.size == 1) "usuario" else "usuarios"}"
                }.onFailure {
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
        MaterialAlertDialogBuilder(this)
            .setTitle("Eliminar usuario")
            .setMessage("¿Estás seguro de que quieres eliminar este usuario?")
            .setPositiveButton("Eliminar") { _, _ ->
                lifecycleScope.launch {
                    val result = authRepository.deleteUser(token, id)
                    result.onSuccess {
                        Toast.makeText(this@UserListActivity, "Usuario eliminado", Toast.LENGTH_SHORT).show()
                        reload()
                    }.onFailure {
                        Toast.makeText(this@UserListActivity, "Error al eliminar", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun resetPassword(id: Int, token: String) {
        val editText = EditText(this)
        editText.hint = "Nueva contraseña"
        editText.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD

        MaterialAlertDialogBuilder(this)
            .setTitle("Cambiar contraseña")
            .setMessage("Introduce la nueva contraseña para este usuario")
            .setView(editText)
            .setPositiveButton("Guardar") { _, _ ->
                val newPassword = editText.text.toString()

                if (newPassword.length < 6) {
                    Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
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
        val selected = intArrayOf(0)

        MaterialAlertDialogBuilder(this)
            .setTitle("Cambiar rol")
            .setSingleChoiceItems(roles, selected[0]) { _, which ->
                selected[0] = which
            }
            .setPositiveButton("Cambiar") { _, _ ->
                val selectedRole = roles[selected[0]]

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
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun reload() {
        val token = getSharedPreferences("auth", MODE_PRIVATE)
            .getString("token", "") ?: ""

        lifecycleScope.launch {
            val result = authRepository.getUsers(token, "")
            result.onSuccess { newUsers ->
                users.clear()
                users.addAll(newUsers)
                adapter.notifyDataSetChanged()
                tvUserCount.text = "${users.size} ${if (users.size == 1) "usuario" else "usuarios"}"
            }
        }
    }
}