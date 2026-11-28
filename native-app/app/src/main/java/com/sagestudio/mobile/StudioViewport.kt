package com.sagestudio.mobile

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import kotlin.math.max
import kotlin.math.sqrt

class StudioViewport(context: Context) : GLSurfaceView(context) {
    private val sceneRenderer = StudioRenderer()
    private var previousX = 0f
    private var previousY = 0f
    private var downX = 0f
    private var downY = 0f
    private var previousDistance = 0f
    private var moved = false
    var activeTool: String = "Select"

    init {
        setEGLContextClientVersion(2)
        setRenderer(sceneRenderer)
        renderMode = RENDERMODE_CONTINUOUSLY
        isFocusable = true
    }

    fun cameraJoystick(x: Float, y: Float) = queueEvent { sceneRenderer.orbit(x * 1.8f, y * 1.8f) }
    fun addCube() = queueEvent { sceneRenderer.addCube() }
    fun duplicateSelected() = queueEvent { sceneRenderer.duplicateSelected() }
    fun deleteSelected() = queueEvent { sceneRenderer.deleteSelected() }
    fun selectNext() = queueEvent { sceneRenderer.selectNext() }
    fun showParts(callback: (List<String>) -> Unit) {
        queueEvent { val items = sceneRenderer.listParts(); post { callback(items) } }
    }
    fun showSelected(callback: (PartInfo?) -> Unit) {
        queueEvent { val info = sceneRenderer.selectedInfo(); post { callback(info) } }
    }
    fun updateSelected(info: PartInfo) = queueEvent {
        sceneRenderer.updateSelected(info.name, info.x, info.y, info.z,
            info.sx, info.sy, info.sz, info.rotation)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                previousX = event.x; previousY = event.y
                downX = event.x; downY = event.y; moved = false
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
                    if (kotlin.math.abs(event.x - downX) + kotlin.math.abs(event.y - downY) > 12f) moved = true
                    if (activeTool == "Select") {
                        queueEvent { sceneRenderer.orbit(dx * 0.45f, dy * 0.35f) }
                    } else {
                        queueEvent { sceneRenderer.transformSelected(activeTool, dx, dy) }
                    }
                }
                previousX = event.x; previousY = event.y
                return true
            }
            MotionEvent.ACTION_POINTER_UP -> { previousDistance = 0f; return true }
            MotionEvent.ACTION_UP -> {
                if (!moved && activeTool == "Select") queueEvent { sceneRenderer.selectAt(event.x, event.y) }
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
        return max(1f, sqrt(dx * dx + dy * dy))
    }
}
