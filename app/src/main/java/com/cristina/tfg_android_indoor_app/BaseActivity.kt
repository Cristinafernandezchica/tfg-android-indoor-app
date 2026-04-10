// BaseActivity.kt
package com.cristina.tfg_android_indoor_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cristina.tfg_android_indoor_app.data.repository.RoomRepository
import com.cristina.tfg_android_indoor_app.map.RouteOverlayView
import com.cristina.tfg_android_indoor_app.ui.admin.AdminVisitsActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

open class BaseActivity : AppCompatActivity() {

    private lateinit var overlay: RouteOverlayView
    protected lateinit var bottomNav: BottomNavigationView
    protected lateinit var topAppBar: MaterialToolbar
    private val roomRepo = RoomRepository()
    private val TAG = "MAP_ACTIVITY"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.setContentView(R.layout.activity_base)

        topAppBar = findViewById(R.id.topAppBar)
        bottomNav = findViewById(R.id.bottomNavigation)

        setupTopBar()
        setupBottomNav()
    }

    override fun setContentView(layoutResID: Int) {
        // Inflamos el layout hijo dentro de contentContainer
        val base = layoutInflater.inflate(R.layout.activity_base, null)
        val container = base.findViewById<android.widget.FrameLayout>(R.id.contentContainer)
        layoutInflater.inflate(layoutResID, container, true)
        super.setContentView(base)

        topAppBar = findViewById(R.id.topAppBar)
        bottomNav = findViewById(R.id.bottomNavigation)

        setupTopBar()
        setupBottomNav()
    }

    private fun setupTopBar() {

        // Menú de la izquierda (más opciones)
        topAppBar.setNavigationOnClickListener { view ->
            val popup = androidx.appcompat.widget.PopupMenu(this, view)
            popup.menuInflater.inflate(R.menu.top_nav_more_menu, popup.menu)

            val prefs = getSharedPreferences("auth", MODE_PRIVATE)
            val role = prefs.getString("role", "user")

            if (role != "admin") {
                popup.menu.findItem(R.id.nav_visits_history)?.isVisible = false
            }

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {

                    // Ocupación en tiempo real
                    R.id.nav_real_time_occupancy -> {
                        if (this is MapActivity) {
                            this.updateOccupancyOnMap()
                        } else {
                            startActivity(Intent(this, MapActivity::class.java).apply {
                                putExtra("refresh_occupancy", true)
                            })
                        }
                        true
                    }

                    // Ocupación histórica
                    R.id.nav_occupancy_history -> {
                        startActivity(Intent(this, OccupancyHistoryActivity::class.java))
                        true
                    }

                    // Historial de visitas (solo admin)
                    R.id.nav_visits_history -> {
                        startActivity(Intent(this, AdminVisitsActivity::class.java))
                        true
                    }

                    // Configuración de umbrales
                    R.id.action_thresholds -> {
                        startActivity(Intent(this, ThresholdSettingsActivity::class.java))
                        true
                    }

                    // Configuración aplicación
                    R.id.config_app -> {
                        startActivity(Intent(this, SettingsActivity::class.java))
                        true
                    }

                    else -> false
                }
            }

            popup.show()
        }


        // Botón de logout
        topAppBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_logout -> {
                    val prefs = getSharedPreferences("auth", MODE_PRIVATE)
                    prefs.edit().clear().apply()

                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)

                    true
                }
                else -> false
            }
        }
    }



    // Configuración del NavBar Inferior
    private fun setupBottomNav() {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val role = prefs.getString("role", "user")

        val menu = bottomNav.menu
        val userListItem = menu.findItem(R.id.nav_user_list)
        userListItem.isVisible = role == "admin"

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    if (this !is HomeActivity) {
                        startActivity(Intent(this, HomeActivity::class.java))
                    }
                    true
                }
                R.id.nav_profile -> {
                    if (this !is ProfileActivity) {
                        startActivity(Intent(this, ProfileActivity::class.java))
                    }
                    true
                }
                R.id.nav_map -> {
                    if (this !is MapActivity) {
                        startActivity(Intent(this, MapActivity::class.java))
                    }
                    true
                }
                R.id.nav_user_list -> {
                    if (this !is com.cristina.tfg_android_indoor_app.ui.userlist.UserListActivity) {
                        startActivity(Intent(this, com.cristina.tfg_android_indoor_app.ui.userlist.UserListActivity::class.java))
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun updateOccupancyOnMap() {
        lifecycleScope.launch {
            try {
                val response = roomRepo.getOccupancy()
                if (response.isSuccessful) {
                    val occupancy = response.body() ?: emptyMap()
                    overlay.updateOccupancy(occupancy)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error obteniendo ocupación: ${e.message}")
            }
        }
    }
}
