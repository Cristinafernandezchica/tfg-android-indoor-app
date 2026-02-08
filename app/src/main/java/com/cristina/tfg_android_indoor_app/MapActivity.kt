package com.cristina.tfg_android_indoor_app

import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class MapActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val webView = findViewById<WebView>(R.id.anyplaceWebView)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        // Configuración del WebView
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true

        // Carga del mapa desde Anyplace
        webView.loadUrl(
            "https://ap.cs.ucy.ac.cy/viewer/?buid=building_58c0f2b7-1ae4-49ba-8da5-c4d6093ca8f4_1767099263967"
        )

        // Marcar el item actual
        bottomNav.selectedItemId = R.id.nav_map

        // Navegación del navbar
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
                R.id.nav_map -> true
                else -> false
            }
        }
    }
}
