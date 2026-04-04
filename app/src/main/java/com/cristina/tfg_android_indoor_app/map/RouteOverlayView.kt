package com.cristina.tfg_android_indoor_app.map

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class RouteOverlayView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val linePaint = Paint().apply {
        color = Color.BLUE
        strokeWidth = 8f
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val pointPaint = Paint().apply {
        color = Color.RED
        strokeWidth = 12f
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val startPaint = Paint().apply {
        color = Color.GREEN
        strokeWidth = 15f
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val endPaint = Paint().apply {
        color = Color.YELLOW
        strokeWidth = 15f
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 20f
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private var points = listOf<Pair<Float, Float>>()
    private var transformMatrix: Matrix? = null

    fun setTransformMatrix(matrix: Matrix) {
        transformMatrix = matrix
        invalidate()  // Forzar redibujado
    }

    fun setRoutePixels(routePixels: List<Pair<Float, Float>>) {
        points = routePixels
        invalidate()  // Forzar redibujado
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (points.isEmpty()) return

        // Guardar estado del canvas
        canvas.save()

        // Aplicar la matriz de transformación (zoom/pan)
        transformMatrix?.let {
            canvas.concat(it)
        }

        // Dibujar líneas conectando los puntos
        if (points.size >= 2) {
            for (i in 0 until points.size - 1) {
                val (x1, y1) = points[i]
                val (x2, y2) = points[i + 1]
                canvas.drawLine(x1, y1, x2, y2, linePaint)
            }
        }

        // Dibujar puntos
        points.forEachIndexed { index, (x, y) ->
            when (index) {
                0 -> {
                    canvas.drawCircle(x, y, 18f, startPaint)
                    canvas.drawText("INICIO", x + 15, y - 15, textPaint)
                }
                points.size - 1 -> {
                    canvas.drawCircle(x, y, 18f, endPaint)
                    canvas.drawText("FIN", x + 15, y - 15, textPaint)
                }
                else -> {
                    canvas.drawCircle(x, y, 12f, pointPaint)
                    canvas.drawText((index + 1).toString(), x + 15, y + 5, textPaint)
                }
            }
        }

        canvas.restore()
    }
}