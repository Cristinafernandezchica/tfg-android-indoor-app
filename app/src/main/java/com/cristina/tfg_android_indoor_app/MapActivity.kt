package com.cristina.tfg_android_indoor_app

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.cristina.tfg_android_indoor_app.data.remote.ROOMS_API_BASE_URL
import com.cristina.tfg_android_indoor_app.data.repository.RoomRepository
import com.cristina.tfg_android_indoor_app.map.MapCoordinates
import com.cristina.tfg_android_indoor_app.map.RoomInfoOverlayView
import com.cristina.tfg_android_indoor_app.map.RouteOverlayView
import com.cristina.tfg_android_indoor_app.map.ZoomableImageView
import com.cristina.tfg_android_indoor_app.services.BeaconScanService
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch
import org.json.JSONObject

class MapActivity : BaseActivity() {

    private lateinit var mapImage: ZoomableImageView
    private lateinit var overlay: RouteOverlayView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var btnMenu: FloatingActionButton
    private lateinit var btnStartRoute: Button
    private lateinit var btnPrevStep: Button
    private lateinit var btnNextStep: Button
    private lateinit var btnShowFullRoute: Button
    private lateinit var btnClearRoute: Button
    private lateinit var btnForceStart: Button
    private lateinit var tvRouteProgress: TextView
    private lateinit var roomInfoOverlay: RoomInfoOverlayView
    private lateinit var layoutStepNavigation: LinearLayout
    private lateinit var tvStepCounter: TextView

    // Bottom Sheet
    private lateinit var bottomSheet: View
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var btnShowPanel: FloatingActionButton

    private val TAG = "MAP_ACTIVITY"
    private var currentRoomRoute = emptyList<String>()
    private var currentStepIndex = 0
    private var showingFullRoute = true
    private var lastKnownRoom: String? = null

    private val roomRepo = RoomRepository()

    // Receiver para actualizaciones de posición desde el servicio
    private val positionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val room = intent.getStringExtra(BeaconScanService.EXTRA_ROOM)
            val zone = intent.getStringExtra(BeaconScanService.EXTRA_ZONE)
            val status = intent.getStringExtra(BeaconScanService.EXTRA_STATUS)
            val pendingCount = intent.getIntExtra(BeaconScanService.EXTRA_PENDING_COUNT, 0)

            when (status) {
                "pending" -> {
                    room?.let {
                        val coordinates = MapCoordinates.getRoomCenter(it)
                        if (coordinates != null) {
                            overlay.updateCurrentPosition(it, coordinates, pendingCount)
                            Log.d(TAG, "Detectando: $it ($pendingCount/3)")
                        }
                    }
                }
                "confirmed" -> {
                    room?.let {
                        val coordinates = MapCoordinates.getRoomCenter(it)
                        if (coordinates != null) {
                            overlay.updateCurrentPosition(it, coordinates, 0)
                            if (it != lastKnownRoom) {
                                lastKnownRoom = it
                                Log.d(TAG, "Posición confirmada: $it")
                            }
                        }
                    }
                    updateOccupancyOnMap()
                }
            }
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        initializeViews()

        setupListeners()

        setupMapAndOverlays()

        setupBottomSheet()

        roomInfoOverlay.setOnRoomInfoClickListener { roomId ->
            showRoomInfoDialog(roomId)
        }

        val filter = IntentFilter(BeaconScanService.ACTION_POSITION_UPDATE)
        registerReceiver(positionReceiver, filter)

        updateOccupancyOnMap()
    }

    private fun initializeViews() {
        mapImage = findViewById(R.id.mapImage)
        overlay = findViewById(R.id.routeOverlay)
        roomInfoOverlay = findViewById(R.id.roomInfoOverlay)
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)

        btnMenu = findViewById(R.id.btnMenu)
        btnStartRoute = findViewById(R.id.btnStartRoute)
        btnPrevStep = findViewById(R.id.btnPrevStep)
        btnNextStep = findViewById(R.id.btnNextStep)
        btnShowFullRoute = findViewById(R.id.btnShowFullRoute)
        btnClearRoute = findViewById(R.id.btnClearRoute)
        btnForceStart = findViewById(R.id.btnForceStart)

        layoutStepNavigation = findViewById(R.id.layoutStepNavigation)
        tvStepCounter = findViewById(R.id.tvStepCounter)

        bottomSheet = findViewById(R.id.bottomSheet)
        btnShowPanel = findViewById(R.id.btnShowPanel)

        val headerView = navigationView.getHeaderView(0)
        tvRouteProgress = headerView.findViewById(R.id.tvRouteProgress)

        layoutStepNavigation.visibility = View.GONE
    }

    private fun setupListeners() {
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_real_time_occupancy -> {
                    updateOccupancyOnMap()
                    Toast.makeText(this, "Ocupación actualizada", Toast.LENGTH_SHORT).show()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        btnStartRoute.setOnClickListener {
            requestRouteFromBackend(forceStart = false)
        }

        btnPrevStep.setOnClickListener { prevStep() }
        btnNextStep.setOnClickListener { nextStep() }
        btnShowFullRoute.setOnClickListener { toggleRouteView() }
        btnClearRoute.setOnClickListener { clearRoute() }
        btnForceStart.setOnClickListener {
            requestRouteFromBackend(forceStart = true)
        }

        mapImage.setOnTouchEventListener { event ->
            roomInfoOverlay.handleTouch(event)
        }
    }

    private fun setupMapAndOverlays() {
        mapImage.setOnMatrixChangeListener { matrix ->
            overlay.setTransformMatrix(matrix)
            roomInfoOverlay.setTransformMatrix(matrix)
        }

        mapImage.post {
            val matrix = mapImage.getCurrentMatrix()
            overlay.setTransformMatrix(matrix)
            roomInfoOverlay.setTransformMatrix(matrix)
            updateMapCoordinatesSize()
            val drawable = mapImage.drawable
            if (drawable != null) {
                roomInfoOverlay.updateDimensions(drawable.intrinsicWidth, drawable.intrinsicHeight)
            }
        }
    }

    private fun setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)

        // Configurar comportamiento
        bottomSheetBehavior.peekHeight = 100 // Altura cuando está contraído
        bottomSheetBehavior.isHideable = true // Permitir ocultar completamente
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED // Estado inicial

        // Listener para cambios de estado
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        // Panel completamente oculto
                        btnShowPanel.visibility = View.VISIBLE
                        btnShowPanel.show()
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        // Panel contraído
                        btnShowPanel.visibility = View.GONE
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        // Panel expandido
                        btnShowPanel.visibility = View.GONE
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                btnShowPanel.alpha = 1 - slideOffset
            }
        })

        // Botón flotante para mostrar el panel cuando está oculto
        btnShowPanel.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            btnShowPanel.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(positionReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receiver: ${e.message}")
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

    private fun updateMapCoordinatesSize() {
        val drawable = mapImage.drawable
        if (drawable != null) {
            MapCoordinates.updateCurrentDimensions(drawable.intrinsicWidth, drawable.intrinsicHeight)
        }
    }

    private fun updateMenu() {
        val menu = navigationView.menu

        // Mostrar/ocultar navegación de pasos según si hay ruta
        val hasRoute = currentRoomRoute.isNotEmpty()
        layoutStepNavigation.visibility = if (hasRoute) View.VISIBLE else View.GONE

        if (hasRoute) {
            tvStepCounter.text = "Paso ${currentStepIndex + 1}/${currentRoomRoute.size}"
        }

        // Actualizar el menú lateral
        menu.removeGroup(R.id.group_route_steps)

        currentRoomRoute.forEachIndexed { index, room ->
            val title = "${index + 1}. $room"
            val item = menu.add(R.id.group_route_steps, index, index, title)
            item.isCheckable = true
            if (index == currentStepIndex) {
                item.isChecked = true
            }
        }

        tvRouteProgress.text = "${currentStepIndex + 1}/${currentRoomRoute.size} pasos"

        // Actualizar estados de botones de navegación
        btnPrevStep.isEnabled = currentStepIndex > 0
        btnNextStep.isEnabled = currentStepIndex < currentRoomRoute.size - 1
        btnShowFullRoute.isEnabled = hasRoute
    }

    private fun requestRouteFromBackend(forceStart: Boolean = false) {
        val message = if (forceStart) {
            "Generando ruta desde ENTRADA..."
        } else {
            "Calculando ruta desde tu posición..."
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val userId = prefs.getInt("user_id", -1)

        if (userId == -1) {
            Toast.makeText(this, "Usuario no identificado", Toast.LENGTH_SHORT).show()
            return
        }

        val queue = Volley.newRequestQueue(this)
        val json = JSONObject().apply {
            put("user_id", userId.toString())
            put("force_start", forceStart)
        }

        val request = JsonObjectRequest(
            Request.Method.POST,
            "$ROOMS_API_BASE_URL/routes/auto/bfs",
            json,
            { response ->
                try {
                    val roomsArray = response.getJSONArray("rooms")
                    val startRoom = response.optString("start_room", "ENTRADA")
                    val roomRoute = mutableListOf<String>()

                    for (i in 0 until roomsArray.length()) {
                        val room = roomsArray.getString(i)
                        if (room != "PASILLO") {
                            roomRoute.add(room)
                        }
                    }

                    Log.d(TAG, "Ruta desde: $startRoom")
                    Log.d(TAG, "Ruta: $roomRoute")

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

                        updateMenu()
                        btnShowFullRoute.text = "Paso a paso"

                        drawerLayout.closeDrawer(GravityCompat.START)

                        Toast.makeText(
                            this,
                            "Ruta desde $startRoom (${roomRoute.size} habitaciones)",
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
            Toast.makeText(
                this,
                "Mostrando paso ${currentStepIndex + 1} → ${currentStepIndex + 2}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun nextStep() {
        if (currentStepIndex < currentRoomRoute.size - 1) {
            currentStepIndex++
            overlay.setCurrentStep(currentStepIndex)
            updateMenu()

            if (!showingFullRoute) {
                overlay.setShowFullRoute(false)
                Toast.makeText(
                    this,
                    "Paso ${currentStepIndex + 1}: ${currentRoomRoute[currentStepIndex]} → ${
                        currentRoomRoute.getOrNull(currentStepIndex + 1) ?: "FIN"
                    }",
                    Toast.LENGTH_SHORT
                ).show()
            }

            if (currentStepIndex == currentRoomRoute.size - 1) {
                Toast.makeText(this, "¡Ruta completada!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun prevStep() {
        if (currentStepIndex > 0) {
            currentStepIndex--
            overlay.setCurrentStep(currentStepIndex)
            updateMenu()

            if (!showingFullRoute) {
                overlay.setShowFullRoute(false)
                Toast.makeText(
                    this,
                    "Paso ${currentStepIndex + 1}: ${currentRoomRoute[currentStepIndex]} → ${
                        currentRoomRoute.getOrNull(currentStepIndex + 1) ?: "FIN"
                    }",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun clearRoute() {
        currentRoomRoute = emptyList()
        currentStepIndex = 0
        showingFullRoute = true
        overlay.clearRoute()
        overlay.clearCurrentPosition()
        updateMenu()
        tvRouteProgress.text = "0/0 pasos"
        btnShowFullRoute.text = "Paso a paso"
        btnShowFullRoute.isEnabled = false
        Toast.makeText(this, "Ruta borrada", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("MissingInflatedId")
    private fun showRoomInfoDialog(roomId: String) {
        lifecycleScope.launch {
            try {
                val response = roomRepo.getRooms()
                if (!response.isSuccessful) {
                    Toast.makeText(this@MapActivity, "Error cargando información", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val rooms = response.body() ?: emptyList()
                val room = rooms.find { it.room_id == roomId }

                val occupancyResponse = roomRepo.getOccupancy()
                val occupancyMap = if (occupancyResponse.isSuccessful) occupancyResponse.body() ?: emptyMap() else emptyMap()
                val currentOccupancy = occupancyMap[roomId] ?: room?.current_occupancy ?: 0

                val dialogView = layoutInflater.inflate(R.layout.dialog_room_info, null)
                val tvRoomName = dialogView.findViewById<TextView>(R.id.tvRoomName)
                val tvRoomId = dialogView.findViewById<TextView>(R.id.tvRoomId)
                val tvDescription = dialogView.findViewById<TextView>(R.id.tvDescription)
                val tvOccupancy = dialogView.findViewById<TextView>(R.id.tvOccupancy)

                tvRoomName.text = room?.name ?: roomId
                tvRoomId.text = "ID: $roomId"
                tvDescription.text = room?.description ?: "Sin descripción disponible"
                tvOccupancy.text = "Ocupación actual: $currentOccupancy personas"

                AlertDialog.Builder(this@MapActivity)
                    .setView(dialogView)
                    .setPositiveButton("Cerrar", null)
                    .show()

            } catch (e: Exception) {
                Log.e(TAG, "Error mostrando info: ${e.message}")
                Toast.makeText(this@MapActivity, "Error cargando información", Toast.LENGTH_SHORT).show()
            }
        }
    }
}