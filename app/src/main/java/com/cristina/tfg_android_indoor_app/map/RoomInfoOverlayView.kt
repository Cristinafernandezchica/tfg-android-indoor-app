package com.cristina.tfg_android_indoor_app.map

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.sqrt

class RoomInfoOverlayView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    companion object {
        private const val TAG = "RoomInfoOverlay"
    }

    private val roomInfoPoints = mapOf(
        "ENTRADA" to Pair(62f, 940f + 35f),
        "SALON" to Pair(377f, 887f + 35f),
        "COCINA" to Pair(150f - 40f, 680f),
        "HAB1" to Pair(568f + 40f, 654f),
        "BAN2" to Pair(210f, 437f + 35f),
        "HAB2" to Pair(166f + 30f, 177f - 25f),
        "HAB3" to Pair(527f - 35f, 179f - 20f)
    )

    private val iconPaint = Paint().apply {
        color = Color.parseColor("#2196F3")
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val iconStrokePaint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 2f
        isAntiAlias = true
        style = Paint.Style.STROKE
    }

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 20f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    private var transformMatrix: Matrix? = null
    private var onRoomInfoClickListener: ((String) -> Unit)? = null
    private var currentWidth = 880f
    private var currentHeight = 1029f

    private val ICON_RADIUS = 40f

    fun setTransformMatrix(matrix: Matrix) {
        transformMatrix = matrix
        invalidate()
    }

    fun setOnRoomInfoClickListener(listener: (String) -> Unit) {
        onRoomInfoClickListener = listener
    }

    fun updateDimensions(width: Int, height: Int) {
        currentWidth = width.toFloat()
        currentHeight = height.toFloat()
        invalidate()
    }

    private fun scaleCoordinates(x: Float, y: Float): Pair<Float, Float> {
        val scaleX = currentWidth / 880f
        val scaleY = currentHeight / 1029f
        return Pair(x * scaleX, y * scaleY)
    }

    // Este método será llamado desde ZoomableImageView
    fun handleTouch(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_UP) {
            return false
        }

        val rawX = event.x
        val rawY = event.y

        val inverseMatrix = Matrix()
        transformMatrix?.invert(inverseMatrix)

        val mapPoint = floatArrayOf(rawX, rawY)
        inverseMatrix.mapPoints(mapPoint)
        val mapX = mapPoint[0]
        val mapY = mapPoint[1]

        for ((roomId, coords) in roomInfoPoints) {
            val scaledCoords = scaleCoordinates(coords.first, coords.second)
            val iconX = scaledCoords.first
            val iconY = scaledCoords.second

            val dx = mapX - iconX
            val dy = mapY - iconY
            val distance = sqrt(dx * dx + dy * dy)

            if (distance <= ICON_RADIUS) {
                Log.d(TAG, "Tocado icono de $roomId")
                onRoomInfoClickListener?.invoke(roomId)
                return true
            }
        }

        return false
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.save()
        transformMatrix?.let { canvas.concat(it) }

        for ((roomId, coords) in roomInfoPoints) {
            val (x, y) = scaleCoordinates(coords.first, coords.second)
            canvas.drawCircle(x, y, ICON_RADIUS, iconPaint)
            canvas.drawCircle(x, y, ICON_RADIUS, iconStrokePaint)
            canvas.drawText("i", x, y + 8, textPaint)
        }

        canvas.restore()
    }
}