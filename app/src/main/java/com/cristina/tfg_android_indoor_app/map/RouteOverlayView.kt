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

    // Marcador de posición actual
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

    private var fullRoutePoints = emptyList<Pair<Float, Float>>()
    private var currentSegmentPoints = emptyList<Pair<Float, Float>>()
    private var roomPositions = emptyMap<String, Pair<Float, Float>>()
    private var transformMatrix: Matrix? = null
    private var currentStepIndex = 0
    private var roomOrder = emptyList<String>()
    private var showFullRoute = true

    fun setTransformMatrix(matrix: Matrix) {
        transformMatrix = matrix
        invalidate()
    }

    fun setFullRoute(routePoints: List<Pair<Float, Float>>, rooms: List<String>, roomCenters: Map<String, Pair<Float, Float>>) {
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
        if (!showFullRoute) {
            updateCurrentSegment()
        }
        invalidate()
    }

    fun setShowFullRoute(show: Boolean) {
        showFullRoute = show
        if (!showFullRoute) {
            updateCurrentSegment()
        }
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

    private fun updateCurrentSegment() {
        if (roomOrder.isEmpty() || currentStepIndex >= roomOrder.size - 1) {
            currentSegmentPoints = emptyList()
            return
        }

        val fromRoom = roomOrder[currentStepIndex]
        val toRoom = roomOrder[currentStepIndex + 1]
        currentSegmentPoints = MapCoordinates.generateRouteBetweenRooms(fromRoom, toRoom)
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

    private fun drawArrow(canvas: Canvas, startX: Float, startY: Float, endX: Float, endY: Float) {
        val angle = atan2((endY - startY).toDouble(), (endX - startX).toDouble())
        val arrowSize = 25f

        val arrowX = startX + (endX - startX) * 0.7f
        val arrowY = startY + (endY - startY) * 0.7f

        val arrowPath = Path().apply {
            val p1x = arrowX + arrowSize * cos(angle + PI - PI / 6).toFloat()
            val p1y = arrowY + arrowSize * sin(angle + PI - PI / 6).toFloat()
            val p2x = arrowX + arrowSize * cos(angle + PI + PI / 6).toFloat()
            val p2y = arrowY + arrowSize * sin(angle + PI + PI / 6).toFloat()

            moveTo(arrowX, arrowY)
            lineTo(p1x, p1y)
            lineTo(p2x, p2y)
            close()
        }

        canvas.drawPath(arrowPath, arrowPaint)
    }

    private fun drawCurrentPositionMarker(canvas: Canvas) {
        currentPosition?.let { (x, y) ->
            canvas.save()
            transformMatrix?.let { canvas.concat(it) }

            if (pendingConfirmations > 0 && pendingConfirmations < 3) {
                // Círculo pulsante para detecciones pendientes
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
                // Posición confirmada
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

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val pointsToDraw = if (showFullRoute) fullRoutePoints else currentSegmentPoints
        if (pointsToDraw.isNotEmpty()) {
            canvas.save()
            transformMatrix?.let { canvas.concat(it) }

            if (pointsToDraw.size >= 2) {
                for (i in 0 until pointsToDraw.size - 1) {
                    val (x1, y1) = pointsToDraw[i]
                    val (x2, y2) = pointsToDraw[i + 1]
                    canvas.drawLine(x1, y1, x2, y2, linePaint)
                    drawArrow(canvas, x1, y1, x2, y2)
                }
            }

            roomOrder.forEachIndexed { index, roomId ->
                val (x, y) = roomPositions[roomId] ?: return@forEachIndexed
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

                paint.alpha = 255
                canvas.drawCircle(x, y, radius, paint)
            }

            canvas.restore()
        }

        // Dibujar marcador de posición
        drawCurrentPositionMarker(canvas)
    }
}