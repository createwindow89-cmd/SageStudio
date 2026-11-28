package com.sagestudio.mobile

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.view.MotionEvent
import android.view.View
import kotlin.math.min
import kotlin.math.sqrt

class CameraJoystick(
    context: Context,
    private val onMove: (Float, Float) -> Unit
) : View(context) {
    private val density = resources.displayMetrics.density
    private val basePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(150, 24, 34, 50)
        style = Paint.Style.FILL
    }
    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(220, 115, 175, 235)
        style = Paint.Style.STROKE
        strokeWidth = 2.2f * density
    }
    private val knobPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(105, 175, 245)
        style = Paint.Style.FILL
    }
    private var knobX = 0f
    private var knobY = 0f
    private var active = false

    init {
        contentDescription = "Camera movement joystick"
        isClickable = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = (116f * density).toInt()
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val radius = min(width, height) * 0.43f
        canvas.drawCircle(cx, cy, radius, basePaint)
        canvas.drawCircle(cx, cy, radius, ringPaint)
        val knobRadius = radius * 0.38f
        canvas.drawCircle(cx + knobX, cy + knobY, knobRadius, knobPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val cx = width / 2f
        val cy = height / 2f
        val maxRadius = min(width, height) * 0.30f
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                active = true
                var dx = event.x - cx
                var dy = event.y - cy
                val distance = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
                if (distance > maxRadius) {
                    dx = dx / distance * maxRadius
                    dy = dy / distance * maxRadius
                }
                knobX = dx
                knobY = dy
                onMove(dx / maxRadius, dy / maxRadius)
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                active = false
                knobX = 0f
                knobY = 0f
                onMove(0f, 0f)
                invalidate()
                performClick()
                return true
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
