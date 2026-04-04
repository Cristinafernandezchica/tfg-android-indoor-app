package com.cristina.tfg_android_indoor_app

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.cristina.tfg_android_indoor_app.data.remote.ROOMS_API_BASE_URL
import com.cristina.tfg_android_indoor_app.map.MapCoordinates
import com.cristina.tfg_android_indoor_app.map.ZoomableImageView
import com.cristina.tfg_android_indoor_app.map.RouteOverlayView
import org.json.JSONObject

class MapActivity : BaseActivity() {

    private lateinit var mapImage: ZoomableImageView
    private lateinit var overlay: RouteOverlayView
    private lateinit var btnStartRoute: Button
    private lateinit var btnContinueRoute: Button
    private lateinit var btnClearRoute: Button
    private lateinit var btnTestOverlay: Button
    private lateinit var btnTestRealisticRoute: Button
    private lateinit var btnDebugSize: Button

    private val TAG = "MAP_ACTIVITY"
    private var currentRoutePoints = emptyList<Pair<Float, Float>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        mapImage = findViewById(R.id.mapImage)
        overlay = findViewById(R.id.routeOverlay)
        btnStartRoute = findViewById(R.id.btnStartRoute)
        btnContinueRoute = findViewById(R.id.btnContinueRoute)
        btnClearRoute = findViewById(R.id.btnClearRoute)
        btnTestOverlay = findViewById(R.id.btnTestOverlay)
        btnTestRealisticRoute = findViewById(R.id.btnTestRealisticRoute)
        btnDebugSize = findViewById(R.id.btnDebugSize)

        btnStartRoute.setOnClickListener { requestRouteFromBackend() }
        btnClearRoute.setOnClickListener {
            currentRoutePoints = emptyList()
            overlay.setRoutePixels(emptyList())
            btnContinueRoute.visibility = Button.GONE
            Toast.makeText(this, "Ruta borrada", Toast.LENGTH_SHORT).show()
        }
        btnTestOverlay.setOnClickListener { testOverlay() }
        btnTestRealisticRoute.setOnClickListener { testRealisticRoute() }
        btnDebugSize.setOnClickListener { debugImageSize() }

        // Escuchar cambios en la matriz del mapa para actualizar el overlay
        mapImage.setOnMatrixChangeListener { matrix ->
            overlay.setTransformMatrix(matrix)
        }

        // Inicializar con la matriz actual y actualizar dimensiones de MapCoordinates
        mapImage.post {
            overlay.setTransformMatrix(mapImage.getCurrentMatrix())
            updateMapCoordinatesSize()
            debugImageSize()
        }
    }

    /**
     * Actualiza las dimensiones de la imagen en MapCoordinates para el escalado correcto
     */
    private fun updateMapCoordinatesSize() {
        val drawable = mapImage.drawable
        if (drawable != null) {
            val width = drawable.intrinsicWidth
            val height = drawable.intrinsicHeight
            MapCoordinates.updateCurrentDimensions(width, height)
            Log.d(TAG, "MapCoordinates actualizado: ${width}x${height}")
        }
    }

    private fun requestRouteFromBackend() {
        Toast.makeText(this, "Generando ruta...", Toast.LENGTH_SHORT).show()

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
                Log.d(TAG, "Respuesta backend: $response")

                try {
                    val pois = response.getJSONArray("pois")
                    val rooms = response.getJSONArray("rooms")
                    val roomRoute = mutableListOf<String>()

                    for (i in 0 until rooms.length()) {
                        roomRoute.add(rooms.getString(i))
                    }

                    Log.d(TAG, "Ruta de habitaciones: $roomRoute")

                    // Generar ruta detallada con puertas usando MapCoordinates
                    val detailedPoints = MapCoordinates.generateFullRoute(roomRoute)

                    if (detailedPoints.isNotEmpty()) {
                        currentRoutePoints = detailedPoints
                        overlay.setRoutePixels(detailedPoints)
                        btnContinueRoute.visibility = Button.VISIBLE
                        Toast.makeText(
                            this,
                            "✅ Ruta cargada con ${detailedPoints.size} puntos",
                            Toast.LENGTH_SHORT
                        ).show()

                        Log.d(TAG, "Puntos generados: ${detailedPoints.size}")
                        detailedPoints.forEachIndexed { i, (x, y) ->
                            Log.d(TAG, "Punto $i: ($x, $y)")
                        }
                    } else {
                        Toast.makeText(
                            this,
                            "⚠️ No se pudo generar la ruta detallada",
                            Toast.LENGTH_SHORT
                        ).show()
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

    /**
     * Prueba de ruta realista usando las coordenadas de puertas
     * Ruta: ENTRADA → SALON → PASILLO → HAB1 → PASILLO → HAB2 → PASILLO → HAB3
     */
    private fun testRealisticRoute() {
        // Actualizar dimensiones antes de calcular coordenadas escaladas
        updateMapCoordinatesSize()

        // Definir una ruta realista que incluya el PASILLO múltiples veces
        val roomRoute = listOf(
            "ENTRADA",
            "SALON",
            "PASILLO",
            "HAB1",
            "PASILLO",
            "HAB2",
            "PASILLO",
            "HAB3"
        )

        // Generar puntos detallados (centros + puertas)
        val detailedPoints = MapCoordinates.generateFullRoute(roomRoute)

        if (detailedPoints.isNotEmpty()) {
            currentRoutePoints = detailedPoints
            overlay.setRoutePixels(detailedPoints)
            btnContinueRoute.visibility = Button.VISIBLE

            // Log para depuración
            Log.d(TAG, "=== RUTA REALISTA CON PUERTAS ===")
            Log.d(TAG, "Habitaciones: $roomRoute")
            Log.d(TAG, "Puntos generados: ${detailedPoints.size}")
            detailedPoints.forEachIndexed { i, (x, y) ->
                Log.d(TAG, "Punto $i: ($x, $y)")
            }

            Toast.makeText(
                this,
                "✅ Ruta realista con ${detailedPoints.size} puntos",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(this, "❌ Error generando ruta realista", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Prueba simple de overlay (solo centros de habitaciones)
     */
    private fun testOverlay() {
        // Actualizar dimensiones antes de calcular coordenadas escaladas
        updateMapCoordinatesSize()

        // Obtener coordenadas escaladas automáticamente (solo centros)
        val testPoints = listOf(
            MapCoordinates.getPixelCoordinates("poi_57fd1fd2-fc14-47fd-b6df-e2f8589a3e7f")!!, // ENTRADA
            MapCoordinates.getPixelCoordinates("poi_5ab33651-9e9f-444e-8023-c57dce5d276d")!!, // SALON
            MapCoordinates.getPixelCoordinates("poi_089e6886-f194-4c5c-9e49-43b3c18a43e9")!!, // PASILLO
            MapCoordinates.getPixelCoordinates("poi_b9f47ce4-59d2-4015-b923-e0d3fab646ea")!!, // HAB1
            MapCoordinates.getPixelCoordinates("poi_bedbfa50-eeca-40a4-8562-78799e66c2b3")!!, // HAB2
            MapCoordinates.getPixelCoordinates("poi_f93ff721-4606-45dc-9fcc-bf1d1d00b920")!!  // HAB3
        )

        currentRoutePoints = testPoints
        overlay.setRoutePixels(testPoints)
        btnContinueRoute.visibility = Button.VISIBLE

        // Log para verificar las coordenadas escaladas
        Log.d(TAG, "=== TEST OVERLAY (Coordenadas escaladas) ===")
        testPoints.forEachIndexed { i, (x, y) ->
            Log.d(TAG, "Punto $i: ($x, $y)")
        }

        Toast.makeText(this, "✅ Ruta de prueba con ${testPoints.size} puntos", Toast.LENGTH_SHORT).show()
    }

    private fun debugImageSize() {
        mapImage.post {
            val drawable = mapImage.drawable
            if (drawable != null) {
                val width = drawable.intrinsicWidth
                val height = drawable.intrinsicHeight

                // Calcular coordenadas escaladas de ejemplo
                val entradaOriginal = MapCoordinates.getOriginalCoordinates("poi_57fd1fd2-fc14-47fd-b6df-e2f8589a3e7f")
                val entradaEscalada = MapCoordinates.getPixelCoordinates("poi_57fd1fd2-fc14-47fd-b6df-e2f8589a3e7f")



                Toast.makeText(this, "Ver Logcat para detalles de escala", Toast.LENGTH_LONG).show()
            } else {
                Log.e(TAG, "No se pudo obtener el drawable de la imagen")
                Toast.makeText(this, "Error: No se pudo obtener la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

private fun MapCoordinates.getOriginalCoordinates(string: String) {}
