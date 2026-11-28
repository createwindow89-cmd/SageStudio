package com.sagestudio.mobile

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.FrameLayout

class MainActivity : Activity() {
    private lateinit var viewport: StudioViewport

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(12, 15, 22)
        window.navigationBarColor = Color.rgb(12, 15, 22)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(12, 15, 22))
        }

        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(8, 6, 8, 6)
            setBackgroundColor(Color.rgb(24, 29, 40))
        }
        val brand = TextView(this).apply {
            text = "SAGE  STUDIO"
            setTextColor(Color.rgb(120, 190, 255))
            textSize = 16f
            setPadding(8, 0, 12, 0)
        }
        toolbar.addView(brand, LinearLayout.LayoutParams(0, 48, 1f))
        listOf("Select", "Move", "Scale", "Rotate", "+ Part", "Explorer").forEach { label ->
            val button = Button(this).apply {
                text = label
                textSize = 11f
                isAllCaps = false
                setOnClickListener {
                    if (label == "+ Part") viewport.addCube()
                    else if (label == "Explorer") showMessage("Explorer panel is coming next.")
                    else showMessage("$label tool selected")
                }
            }
            toolbar.addView(button, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, 44
            ).apply { setMargins(3, 0, 3, 0) })
        }
        root.addView(toolbar)

        val body = FrameLayout(this).apply {
            setBackgroundColor(Color.rgb(17, 22, 31))
        }
        viewport = StudioViewport(this)
        body.addView(viewport, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))

        val hint = TextView(this).apply {
            text = "3D VIEWPORT  •  Drag to orbit  •  Pinch to zoom"
            setTextColor(Color.LTGRAY)
            textSize = 11f
            setPadding(12, 8, 12, 8)
            setBackgroundColor(0x990C0F16.toInt())
        }
        body.addView(hint, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.TOP or Gravity.START
        ))
        root.addView(body, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
        ))

        val footer = TextView(this).apply {
            text = "SageStudio 0.1.0  |  Native editor foundation"
            setTextColor(Color.rgb(155, 165, 180))
            textSize = 11f
            setPadding(12, 7, 12, 7)
            setBackgroundColor(Color.rgb(24, 29, 40))
        }
        root.addView(footer)
        setContentView(root)
    }

    private fun showMessage(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        if (::viewport.isInitialized) viewport.onResume()
    }

    override fun onPause() {
        if (::viewport.isInitialized) viewport.onPause()
        super.onPause()
    }
}
