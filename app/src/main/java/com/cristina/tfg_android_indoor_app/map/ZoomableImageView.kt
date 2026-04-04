package com.cristina.tfg_android_indoor_app.map

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ImageView
import kotlin.math.sqrt

@SuppressLint("AppCompatCustomView")
class ZoomableImageView(context: Context, attrs: AttributeSet?) : ImageView(context, attrs) {

    private val matrixState = Matrix()
    private val savedMatrix = Matrix()
    private val start = PointF()
    private val mid = PointF()
    private var oldDist = 1f
    private var mode = NONE

    private var onMatrixChangeListener: ((Matrix) -> Unit)? = null

    companion object {
        private const val NONE = 0
        private const val DRAG = 1
        private const val ZOOM = 2
    }

    init {
        scaleType = ScaleType.MATRIX
        imageMatrix = matrixState
    }

    fun setOnMatrixChangeListener(listener: (Matrix) -> Unit) {
        onMatrixChangeListener = listener
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                savedMatrix.set(matrixState)
                start.set(event.x, event.y)
                mode = DRAG
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                oldDist = spacing(event)
                if (oldDist > 10f) {
                    savedMatrix.set(matrixState)
                    midPoint(mid, event)
                    mode = ZOOM
                }
            }
            MotionEvent.ACTION_MOVE -> {
                when (mode) {
                    DRAG -> {
                        matrixState.set(savedMatrix)
                        matrixState.postTranslate(event.x - start.x, event.y - start.y)
                    }
                    ZOOM -> {
                        val newDist = spacing(event)
                        if (newDist > 10f) {
                            matrixState.set(savedMatrix)
                            val scale = newDist / oldDist
                            matrixState.postScale(scale, scale, mid.x, mid.y)
                        }
                    }
                }
                // Notificar cambio de matriz
                onMatrixChangeListener?.invoke(matrixState)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                mode = NONE
                // Notificar cambio final
                onMatrixChangeListener?.invoke(matrixState)
            }
        }
        imageMatrix = matrixState
        invalidate()
        return true
    }

    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return sqrt(x * x + y * y)
    }

    private fun midPoint(point: PointF, event: MotionEvent) {
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        point.set(x / 2, y / 2)
    }

    fun getCurrentMatrix(): Matrix = Matrix(matrixState)
}