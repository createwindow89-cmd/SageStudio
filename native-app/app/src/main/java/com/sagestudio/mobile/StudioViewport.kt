package com.sagestudio.mobile

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import kotlin.math.max

class StudioViewport(context: Context) : GLSurfaceView(context) {
    private val sceneRenderer = StudioRenderer()
    private var previousX = 0f
    private var previousY = 0f
    private var previousDistance = 0f

    init {
        setEGLContextClientVersion(2)
        setRenderer(sceneRenderer)
        renderMode = RENDERMODE_CONTINUOUSLY
        isFocusable = true
    }

    fun addCube() {
        queueEvent { sceneRenderer.addCube() }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                previousX = event.x
                previousY = event.y
                return true
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                if (event.pointerCount >= 2) previousDistance = distance(event)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (event.pointerCount >= 2) {
                    val newDistance = distance(event)
                    if (previousDistance > 0f) {
                        val delta = (newDistance - previousDistance) / 300f
                        queueEvent { sceneRenderer.zoom(delta) }
                    }
                    previousDistance = newDistance
                } else {
                    val dx = event.x - previousX
                    val dy = event.y - previousY
                    queueEvent { sceneRenderer.orbit(dx * 0.45f, dy * 0.35f) }
                }
                previousX = event.x
                previousY = event.y
                return true
            }
            MotionEvent.ACTION_POINTER_UP -> {
                previousDistance = 0f
                return true
            }
        }
        return true
    }

    private fun distance(event: MotionEvent): Float {
        if (event.pointerCount < 2) return 0f
        val dx = event.getX(0) - event.getX(1)
        val dy = event.getY(0) - event.getY(1)
        return max(1f, kotlin.math.sqrt(dx * dx + dy * dy))
    }
}
