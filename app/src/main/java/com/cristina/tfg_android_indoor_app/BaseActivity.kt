// BaseActivity.kt
package com.cristina.tfg_android_indoor_app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

open class BaseActivity : AppCompatActivity() {

    protected lateinit var bottomNav: BottomNavigationView
    protected lateinit var topAppBar: MaterialToolbar

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

        // 🔹 Menú de la izquierda (más opciones)
        topAppBar.setNavigationOnClickListener { view ->
            val popup = androidx.appcompat.widget.PopupMenu(this, view)
            popup.menuInflater.inflate(R.menu.top_nav_more_menu, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_thresholds -> {
                        startActivity(Intent(this, ThresholdSettingsActivity::class.java))
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        // 🔹 Botón de logout (derecha)
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
}
