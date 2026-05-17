package com.cristina.tfg_android_indoor_app.map

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.*

class RouteOverlayView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val linePaint = Paint().apply {
        color = Color.MAGENTA
        strokeWidth = 8f
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val pointPaint = Paint().apply {
        color = Color.RED
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val startPaint = Paint().apply {
        color = Color.GREEN
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val endPaint = Paint().apply {
        color = Color.YELLOW
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val visitedPaint = Paint().apply {
        color = Color.argb(150, 100, 200, 100)
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val arrowPaint = Paint().apply {
        color = Color.MAGENTA
        strokeWidth = 4f
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    // POSICIÓN DEL USUARIO
    private var currentPosition: Pair<Float, Float>? = null
    private var currentRoomId: String? = null
    private var pendingConfirmations: Int = 0

    private val positionPaint = Paint().apply {
        color = Color.parseColor("#2196F3")
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val positionStrokePaint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 3f
        isAntiAlias = true
        style = Paint.Style.STROKE
    }

    private val pendingPaint = Paint().apply {
        color = Color.argb(200, 255, 152, 0)
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val shadowPaint = Paint().apply {
        color = Color.argb(80, 0, 0, 0)
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    // OCUPACIÓN ACTUAL
    private var occupancyMap: Map<String, Int> = emptyMap()

    private val occupancyPaint = Paint().apply {
        color = Color.BLACK
        textSize = 34f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    // RUTAS
    private var fullRoutePoints = emptyList<Pair<Float, Float>>()
    private var currentSegmentPoints = emptyList<Pair<Float, Float>>()
    private var roomPositions = emptyMap<String, Pair<Float, Float>>()
    private var transformMatrix: Matrix? = null
    private var currentStepIndex = 0
    private var roomOrder = emptyList<String>()
    private var showFullRoute = true

    // MÉTODOS DE CONFIGURACIÓN

    fun setTransformMatrix(matrix: Matrix) {
        transformMatrix = matrix
        invalidate()
    }

    fun setFullRoute(
        routePoints: List<Pair<Float, Float>>,
        rooms: List<String>,
        roomCenters: Map<String, Pair<Float, Float>>
    ) {
        fullRoutePoints = routePoints
        roomOrder = rooms
        roomPositions = roomCenters
        currentStepIndex = 0
        showFullRoute = true
        updateCurrentSegment()
        invalidate()
    }

    fun setCurrentStep(index: Int) {
        currentStepIndex = index
        if (!showFullRoute) updateCurrentSegment()
        invalidate()
    }

    fun setShowFullRoute(show: Boolean) {
        showFullRoute = show
        if (!showFullRoute) updateCurrentSegment()
        invalidate()
    }

    fun updateCurrentPosition(roomId: String, center: Pair<Float, Float>, pendingCount: Int = 0) {
        currentRoomId = roomId
        currentPosition = center
        pendingConfirmations = pendingCount
        invalidate()
    }

    fun clearCurrentPosition() {
        currentPosition = null
        currentRoomId = null
        pendingConfirmations = 0
        invalidate()
    }

    fun updateOccupancy(map: Map<String, Int>) {
        occupancyMap = map
        invalidate()
    }

    fun clearRoute() {
        fullRoutePoints = emptyList()
        currentSegmentPoints = emptyList()
        roomOrder = emptyList()
        roomPositions = emptyMap()
        currentStepIndex = 0
        showFullRoute = true
        invalidate()
    }

    private fun updateCurrentSegment() {
        if (roomOrder.isEmpty() || currentStepIndex >= roomOrder.size - 1) {
            currentSegmentPoints = emptyList()
            return
        }

        val fromRoom = roomOrder[currentStepIndex]
        val toRoom = roomOrder[currentStepIndex + 1]
        currentSegmentPoints = MapCoordinates.generateRouteBetweenRooms(fromRoom, toRoom)
    }

    // DIBUJADO
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Aplicar zoom/scroll del mapa
        canvas.save()
        transformMatrix?.let { canvas.concat(it) }

        drawRoute(canvas)
        drawRoutePoints(canvas)
        drawOccupancy(canvas)

        canvas.restore()

        drawCurrentPositionMarker(canvas)
    }

    // DIBUJAR RUTA
    private fun drawRoute(canvas: Canvas) {
        val points = if (showFullRoute) fullRoutePoints else currentSegmentPoints
        if (points.size < 2) return

        for (i in 0 until points.size - 1) {
            val (x1, y1) = points[i]
            val (x2, y2) = points[i + 1]
            canvas.drawLine(x1, y1, x2, y2, linePaint)
            drawArrow(canvas, x1, y1, x2, y2)
        }
    }

    private fun drawRoutePoints(canvas: Canvas) {
        roomOrder.forEachIndexed { index, roomId ->
            val pos = roomPositions[roomId] ?: return@forEachIndexed
            val (x, y) = pos

            val radius = when {
                index < currentStepIndex -> 18f
                index == currentStepIndex -> 22f
                index == roomOrder.size - 1 -> 18f
                else -> 14f
            }

            val paint = when {
                index < currentStepIndex -> visitedPaint
                index == currentStepIndex -> startPaint
                index == roomOrder.size - 1 -> endPaint
                else -> pointPaint
            }

            canvas.drawCircle(x, y, radius, paint)
        }
    }

    // DIBUJAR OCUPACIÓN
    private fun drawOccupancy(canvas: Canvas) {
        occupancyMap.forEach { (room, count) ->
            val pos = roomPositions[room] ?: return@forEach
            val (x, y) = pos

            canvas.drawText(
                count.toString(),
                x,
                y - 40f, // encima del punto
                occupancyPaint
            )
        }
    }

    // DIBUJAR POSICIÓN USUARIO
    private fun drawCurrentPositionMarker(canvas: Canvas) {
        currentPosition?.let { (x, y) ->

            canvas.save()
            transformMatrix?.let { canvas.concat(it) }

            if (pendingConfirmations in 1..2) {
                val pulseRadius = 25f + (System.currentTimeMillis() % 1000) / 1000f * 10
                canvas.drawCircle(x, y, pulseRadius, pendingPaint)
                canvas.drawCircle(x, y, 18f, positionPaint)
                canvas.drawCircle(x, y, 18f, positionStrokePaint)

                val textPaint = Paint().apply {
                    color = Color.WHITE
                    textSize = 16f
                    isAntiAlias = true
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("$pendingConfirmations/3", x, y + 6, textPaint)

            } else {
                canvas.drawCircle(x, y, 22f, shadowPaint)
                canvas.drawCircle(x, y, 20f, positionPaint)
                canvas.drawCircle(x, y, 20f, positionStrokePaint)

                val eyePaint = Paint().apply { color = Color.WHITE }
                canvas.drawCircle(x - 7, y - 5, 3f, eyePaint)
                canvas.drawCircle(x + 7, y - 5, 3f, eyePaint)

                val smilePaint = Paint().apply {
                    color = Color.WHITE
                    strokeWidth = 2f
                    style = Paint.Style.STROKE
                }
                val smilePath = Path().apply {
                    moveTo(x - 8, y + 5)
                    quadTo(x, y + 12, x + 8, y + 5)
                }
                canvas.drawPath(smilePath, smilePaint)
            }

            canvas.restore()
        }
    }

    // ============================
    // FLECHAS DE DIRECCIÓN
    // ============================

    private fun drawArrow(canvas: Canvas, startX: Float, startY: Float, endX: Float, endY: Float) {
        val angle = atan2((endY - startY), (endX - startX))
        val arrowSize = 25f

        val arrowX = startX + (endX - startX) * 0.7f
        val arrowY = startY + (endY - startY) * 0.7f

        val path = Path().apply {
            val p1x = arrowX + arrowSize * cos(angle + Math.PI - Math.PI / 6).toFloat()
            val p1y = arrowY + arrowSize * sin(angle + Math.PI - Math.PI / 6).toFloat()
            val p2x = arrowX + arrowSize * cos(angle + Math.PI + Math.PI / 6).toFloat()
            val p2y = arrowY + arrowSize * sin(angle + Math.PI + Math.PI / 6).toFloat()

            moveTo(arrowX, arrowY)
            lineTo(p1x, p1y)
            lineTo(p2x, p2y)
            close()
        }

        canvas.drawPath(path, arrowPaint)
    }
}
