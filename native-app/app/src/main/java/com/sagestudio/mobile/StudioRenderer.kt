package com.sagestudio.mobile

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.sin

class StudioRenderer : GLSurfaceView.Renderer {
    private val vertexShader = """
        attribute vec3 aPosition;
        uniform mat4 uMvp;
        void main() { gl_Position = uMvp * vec4(aPosition, 1.0); }
    """.trimIndent()
    private val fragmentShader = """
        precision mediump float;
        uniform vec4 uColor;
        void main() { gl_FragColor = uColor; }
    """.trimIndent()

    private var program = 0
    private var width = 1
    private var height = 1
    private var yaw = 0f
    private var pitch = 0.45f
    private var cameraDistance = 12f
    private var cubeCount = 1
    private val projection = FloatArray(16)
    private val view = FloatArray(16)
    private val model = FloatArray(16)
    private val vp = FloatArray(16)
    private val mvp = FloatArray(16)
    private val grid = makeGrid()
    private val cube = makeCube()

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.055f, 0.075f, 0.105f, 1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        program = linkProgram(vertexShader, fragmentShader)
    }

    override fun onSurfaceChanged(gl: GL10?, w: Int, h: Int) {
        width = w
        height = h.coerceAtLeast(1)
        GLES20.glViewport(0, 0, width, height)
        Matrix.perspectiveM(projection, 0, 48f, width.toFloat() / height, 0.1f, 200f)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        val eyeX = sin(yaw) * cos(pitch) * cameraDistance
        val eyeY = sin(pitch) * cameraDistance
        val eyeZ = cos(yaw) * cos(pitch) * cameraDistance
        Matrix.setLookAtM(view, 0, eyeX, eyeY, eyeZ, 0f, 0f, 0f, 0f, 1f, 0f)
        Matrix.multiplyMM(vp, 0, projection, 0, view, 0)
        GLES20.glUseProgram(program)
        draw(grid, GLES20.GL_LINES, floatArrayOf(0.25f, 0.34f, 0.46f, 1f), 1f)
        Matrix.setIdentityM(model, 0)
        Matrix.multiplyMM(mvp, 0, vp, 0, model, 0)
        draw(cube, GLES20.GL_TRIANGLES, floatArrayOf(0.18f, 0.58f, 0.95f, 1f), 1f)
    }

    fun orbit(dx: Float, dy: Float) {
        yaw += dx * 0.01f
        pitch = (pitch + dy * 0.01f).coerceIn(-1.15f, 1.15f)
    }

    fun zoom(delta: Float) {
        cameraDistance = (cameraDistance - delta * 8f).coerceIn(4f, 35f)
    }

    fun addCube() {
        cubeCount++
    }

    private fun draw(data: FloatBuffer, mode: Int, color: FloatArray, lineWidth: Float) {
        data.position(0)
        val position = GLES20.glGetAttribLocation(program, "aPosition")
        val colorLocation = GLES20.glGetUniformLocation(program, "uColor")
        val matrixLocation = GLES20.glGetUniformLocation(program, "uMvp")
        GLES20.glUniformMatrix4fv(matrixLocation, 1, false, vp, 0)
        GLES20.glUniform4fv(colorLocation, 1, color, 0)
        GLES20.glEnableVertexAttribArray(position)
        GLES20.glVertexAttribPointer(position, 3, GLES20.GL_FLOAT, false, 0, data)
        GLES20.glLineWidth(lineWidth)
        GLES20.glDrawArrays(mode, 0, data.limit() / 3)
        GLES20.glDisableVertexAttribArray(position)
    }

    private fun linkProgram(vs: String, fs: String): Int {
        fun compile(type: Int, source: String): Int {
            val shader = GLES20.glCreateShader(type)
            GLES20.glShaderSource(shader, source)
            GLES20.glCompileShader(shader)
            return shader
        }
        val v = compile(GLES20.GL_VERTEX_SHADER, vs)
        val f = compile(GLES20.GL_FRAGMENT_SHADER, fs)
        return GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, v)
            GLES20.glAttachShader(it, f)
            GLES20.glLinkProgram(it)
        }
    }

    private fun buffer(values: FloatArray): FloatBuffer =
        ByteBuffer.allocateDirect(values.size * 4).order(ByteOrder.nativeOrder())
            .asFloatBuffer().apply { put(values); position(0) }

    private fun makeGrid(): FloatBuffer {
        val values = ArrayList<Float>()
        for (i in -10..10) {
            val n = i.toFloat()
            values.addAll(listOf(-10f, -0.6f, n, 10f, -0.6f, n))
            values.addAll(listOf(n, -0.6f, -10f, n, -0.6f, 10f))
        }
        return buffer(values.toFloatArray())
    }

    private fun makeCube(): FloatBuffer {
        val p = floatArrayOf(
            -1f,-1f,1f, 1f,-1f,1f, 1f,1f,1f, -1f,-1f,1f, 1f,1f,1f, -1f,1f,1f,
            -1f,-1f,-1f, -1f,1f,-1f, 1f,1f,-1f, -1f,-1f,-1f, 1f,1f,-1f, 1f,-1f,-1f,
            -1f,-1f,-1f, -1f,-1f,1f, -1f,1f,1f, -1f,-1f,-1f, -1f,1f,1f, -1f,1f,-1f,
            1f,-1f,-1f, 1f,1f,-1f, 1f,1f,1f, 1f,-1f,-1f, 1f,1f,1f, 1f,-1f,1f,
            -1f,1f,-1f, -1f,1f,1f, 1f,1f,1f, -1f,1f,-1f, 1f,1f,1f, 1f,1f,-1f,
            -1f,-1f,-1f, 1f,-1f,-1f, 1f,-1f,1f, -1f,-1f,-1f, 1f,-1f,1f, -1f,-1f,1f
        )
        return buffer(p)
    }
}
