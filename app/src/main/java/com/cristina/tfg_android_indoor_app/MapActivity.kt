package com.cristina.tfg_android_indoor_app

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.cristina.tfg_android_indoor_app.data.remote.ROOMS_API_BASE_URL
import com.cristina.tfg_android_indoor_app.map.MapCoordinates
import com.cristina.tfg_android_indoor_app.map.ZoomableImageView
import com.cristina.tfg_android_indoor_app.map.RouteOverlayView
import com.google.android.material.navigation.NavigationView
import org.json.JSONObject

class MapActivity : BaseActivity() {

    private lateinit var mapImage: ZoomableImageView
    private lateinit var overlay: RouteOverlayView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var btnMenu: ImageButton
    private lateinit var btnStartRoute: Button
    private lateinit var btnPrevStep: Button
    private lateinit var btnNextStep: Button
    private lateinit var btnShowFullRoute: Button
    private lateinit var btnClearRoute: Button
    private lateinit var tvRouteProgress: TextView

    private val TAG = "MAP_ACTIVITY"
    private var currentRoomRoute = emptyList<String>()
    private var currentStepIndex = 0
    private var showingFullRoute = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        mapImage = findViewById(R.id.mapImage)
        overlay = findViewById(R.id.routeOverlay)
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        btnMenu = findViewById(R.id.btnMenu)
        btnStartRoute = findViewById(R.id.btnStartRoute)
        btnPrevStep = findViewById(R.id.btnPrevStep)
        btnNextStep = findViewById(R.id.btnNextStep)
        btnShowFullRoute = findViewById(R.id.btnShowFullRoute)
        btnClearRoute = findViewById(R.id.btnClearRoute)

        val headerView = navigationView.getHeaderView(0)
        tvRouteProgress = headerView.findViewById(R.id.tvRouteProgress)

        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        btnStartRoute.setOnClickListener { requestRouteFromBackend() }
        btnPrevStep.setOnClickListener { prevStep() }
        btnNextStep.setOnClickListener { nextStep() }
        btnShowFullRoute.setOnClickListener { toggleRouteView() }
        btnClearRoute.setOnClickListener { clearRoute() }

        // Deshabilitar botones hasta que haya ruta
        enableNavigationButtons(false)

        // Escuchar cambios en la matriz del mapa
        mapImage.setOnMatrixChangeListener { matrix ->
            overlay.setTransformMatrix(matrix)
        }

        // Inicializar
        mapImage.post {
            overlay.setTransformMatrix(mapImage.getCurrentMatrix())
            updateMapCoordinatesSize()
        }
    }

    private fun updateMapCoordinatesSize() {
        val drawable = mapImage.drawable
        if (drawable != null) {
            MapCoordinates.updateCurrentDimensions(drawable.intrinsicWidth, drawable.intrinsicHeight)
        }
    }

    private fun enableNavigationButtons(enabled: Boolean) {
        btnPrevStep.isEnabled = enabled
        btnNextStep.isEnabled = enabled
        btnShowFullRoute.isEnabled = enabled
    }

    private fun updateMenu() {
        val menu = navigationView.menu
        menu.clear()

        currentRoomRoute.forEachIndexed { index, room ->
            val title = "${index + 1}. $room"
            val item = menu.add(title)
            item.isCheckable = true
            if (index == currentStepIndex) {
                item.isChecked = true
            }
        }

        tvRouteProgress.text = "${currentStepIndex + 1}/${currentRoomRoute.size} pasos"
    }

    private fun requestRouteFromBackend() {
        Toast.makeText(this, "Calculando ruta...", Toast.LENGTH_SHORT).show()

        val queue = Volley.newRequestQueue(this)
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val userId = prefs.getInt("user_id", -1)

        if (userId == -1) {
            Toast.makeText(this, "Usuario no identificado", Toast.LENGTH_SHORT).show()
            return
        }

        val json = JSONObject().apply { put("user_id", userId) }

        val request = JsonObjectRequest(
            Request.Method.POST,
            "$ROOMS_API_BASE_URL/routes/auto/bfs",
            json,
            { response ->
                try {
                    val roomsArray = response.getJSONArray("rooms")
                    val roomRoute = mutableListOf<String>()

                    for (i in 0 until roomsArray.length()) {
                        val room = roomsArray.getString(i)
                        if (room != "PASILLO") {
                            roomRoute.add(room)
                        }
                    }

                    Log.d(TAG, "Ruta de habitaciones: $roomRoute")

                    if (roomRoute.size >= 2) {
                        currentRoomRoute = roomRoute
                        currentStepIndex = 0
                        showingFullRoute = true

                        val fullRoute = MapCoordinates.generateFullRoute(roomRoute)
                        val roomCenters = roomRoute.associateWith { roomId ->
                            MapCoordinates.getRoomCenter(roomId) ?: Pair(0f, 0f)
                        }

                        overlay.setFullRoute(fullRoute, roomRoute, roomCenters)
                        overlay.setCurrentStep(0)
                        overlay.setShowFullRoute(true)

                        enableNavigationButtons(true)
                        updateMenu()
                        btnShowFullRoute.text = "Paso a paso"

                        drawerLayout.closeDrawer(GravityCompat.START)

                        Toast.makeText(
                            this,
                            "✅ Ruta: ${roomRoute.size} habitaciones",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(this, "No se pudo calcular la ruta", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error: ${e.message}")
                    Toast.makeText(this, "Error procesando la ruta", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Log.e(TAG, "Error: ${error.message}")
                Toast.makeText(this, "Error al obtener la ruta", Toast.LENGTH_SHORT).show()
            }
        )

        queue.add(request)
    }

    private fun toggleRouteView() {
        showingFullRoute = !showingFullRoute
        overlay.setShowFullRoute(showingFullRoute)

        if (showingFullRoute) {
            btnShowFullRoute.text = "Paso a paso"
            Toast.makeText(this, "Mostrando ruta completa", Toast.LENGTH_SHORT).show()
        } else {
            btnShowFullRoute.text = "Ruta completa"
            Toast.makeText(this, "Mostrando paso ${currentStepIndex + 1} → ${currentStepIndex + 2}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun nextStep() {
        if (currentStepIndex < currentRoomRoute.size - 1) {
            currentStepIndex++
            overlay.setCurrentStep(currentStepIndex)
            updateMenu()

            if (!showingFullRoute) {
                overlay.setShowFullRoute(false)
                Toast.makeText(this,
                    "Paso ${currentStepIndex + 1}: ${currentRoomRoute[currentStepIndex]} → ${currentRoomRoute.getOrNull(currentStepIndex + 1) ?: "FIN"}",
                    Toast.LENGTH_SHORT).show()
            }

            if (currentStepIndex == currentRoomRoute.size - 1) {
                btnNextStep.isEnabled = false
                Toast.makeText(this, "🎉 ¡Ruta completada!", Toast.LENGTH_LONG).show()
            }
            btnPrevStep.isEnabled = true
        }
    }

    private fun prevStep() {
        if (currentStepIndex > 0) {
            currentStepIndex--
            overlay.setCurrentStep(currentStepIndex)
            updateMenu()

            if (!showingFullRoute) {
                overlay.setShowFullRoute(false)
                Toast.makeText(this,
                    "Paso ${currentStepIndex + 1}: ${currentRoomRoute[currentStepIndex]} → ${currentRoomRoute.getOrNull(currentStepIndex + 1) ?: "FIN"}",
                    Toast.LENGTH_SHORT).show()
            }

            if (currentStepIndex == 0) {
                btnPrevStep.isEnabled = false
            }
            btnNextStep.isEnabled = true
        }
    }

    private fun clearRoute() {
        currentRoomRoute = emptyList()
        currentStepIndex = 0
        showingFullRoute = true
        overlay.clearRoute()
        enableNavigationButtons(false)
        updateMenu()
        tvRouteProgress.text = "0/0 pasos"
        btnShowFullRoute.text = "Paso a paso"
        Toast.makeText(this, "Ruta borrada", Toast.LENGTH_SHORT).show()
    }
}