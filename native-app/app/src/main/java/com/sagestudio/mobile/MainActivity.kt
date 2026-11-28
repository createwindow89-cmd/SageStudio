package com.sagestudio.mobile

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.FrameLayout
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var viewport: StudioViewport
    private val panelColor = Color.rgb(24, 29, 40)
    private val textColor = Color.rgb(225, 232, 242)

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
            setPadding(6, 5, 6, 5)
            setBackgroundColor(panelColor)
        }
        val brand = TextView(this).apply {
            text = "SAGE"
            setTextColor(Color.rgb(120, 190, 255))
            textSize = 15f
            setPadding(8, 0, 8, 0)
        }
        toolbar.addView(brand)
        val scroller = HorizontalScrollView(this).apply { isHorizontalScrollBarEnabled = false }
        val actions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        scroller.addView(actions)
        toolbar.addView(scroller, LinearLayout.LayoutParams(0, 48, 1f))
        root.addView(toolbar)

        val body = FrameLayout(this).apply { setBackgroundColor(Color.rgb(17, 22, 31)) }
        viewport = StudioViewport(this)
        body.addView(viewport, FrameLayout.LayoutParams(-1, -1))
        val hint = TextView(this).apply {
            text = "3D VIEWPORT  •  Select: tap to cycle, drag to orbit  •  Pinch to zoom"
            setTextColor(Color.LTGRAY); textSize = 10f; setPadding(10, 7, 10, 7)
            setBackgroundColor(0x990C0F16.toInt())
        }
        body.addView(hint, FrameLayout.LayoutParams(-2, -2, Gravity.TOP or Gravity.START))
        root.addView(body, LinearLayout.LayoutParams(-1, 0, 1f))
        val footer = TextView(this).apply {
            text = "SageStudio 0.2.0  |  Native 3D editor"
            setTextColor(Color.rgb(155, 165, 180)); textSize = 10f
            setPadding(12, 7, 12, 7); setBackgroundColor(panelColor)
        }
        root.addView(footer)
        setContentView(root)

        fun action(label: String, click: () -> Unit) {
            val button = Button(this).apply {
                text = label; textSize = 10f; isAllCaps = false
                setTextColor(textColor); setOnClickListener { click() }
            }
            actions.addView(button, LinearLayout.LayoutParams(-2, 44).apply {
                setMargins(2, 0, 2, 0)
            })
        }
        listOf("Select", "Move", "Scale", "Rotate").forEach { tool ->
            action(tool) {
                viewport.activeTool = tool
                hint.text = when (tool) {
                    "Select" -> "SELECT  •  Tap to cycle parts, drag to orbit"
                    "Move" -> "MOVE  •  Drag to move selected part"
                    "Scale" -> "SCALE  •  Drag to resize selected part"
                    else -> "ROTATE  •  Drag to rotate selected part"
                }
                toast("$tool tool active")
            }
        }
        action("+ Part") { viewport.addCube(); toast("Part added") }
        action("Duplicate") { viewport.duplicateSelected(); toast("Part duplicated") }
        action("Delete") { viewport.deleteSelected(); toast("Part deleted") }
        action("Explorer") { showExplorer() }
        action("Properties") { showProperties() }
    }

    private fun showExplorer() {
        viewport.showParts { names ->
            AlertDialog.Builder(this).setTitle("Explorer  •  Workspace")
                .setItems(names.toTypedArray()) { _, which ->
                    repeat(which + 1) { viewport.selectNext() }
                }
                .setPositiveButton("Close", null).show()
        }
    }

    private fun showProperties() {
        viewport.showSelected { info ->
            if (info == null) { toast("No part selected"); return@showSelected }
            val fields = listOf(
                "Name" to info.name, "Position X" to info.x.toString(),
                "Position Y" to info.y.toString(), "Position Z" to info.z.toString(),
                "Size X" to info.sx.toString(), "Size Y" to info.sy.toString(),
                "Size Z" to info.sz.toString(), "Rotation Y" to info.rotation.toString()
            )
            val container = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL; setPadding(20, 8, 20, 4)
            }
            val inputs = fields.map { (label, value) ->
                val row = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
                val caption = TextView(this).apply {
                    text = label; setTextColor(textColor); textSize = 12f
                }
                val edit = EditText(this).apply {
                    setText(value); setTextColor(textColor); textSize = 14f
                    setSingleLine(true)
                }
                row.addView(caption); row.addView(edit)
                container.addView(row)
                edit
            }
            AlertDialog.Builder(this).setTitle("Properties  •  ${info.name}")
                .setView(container)
                .setPositiveButton("Apply") { _, _ ->
                    fun number(index: Int, fallback: Float) = inputs[index].text.toString().toFloatOrNull() ?: fallback
                    val updated = PartInfo(
                        inputs[0].text.toString(),
                        number(1, info.x), number(2, info.y), number(3, info.z),
                        number(4, info.sx), number(5, info.sy), number(6, info.sz),
                        number(7, info.rotation)
                    )
                    viewport.updateSelected(updated)
                    toast("Properties updated")
                }
                .setNegativeButton("Cancel", null).show()
        }
    }

    private fun toast(message: String) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    override fun onResume() {
        super.onResume()
        if (::viewport.isInitialized) viewport.onResume()
    }

    override fun onPause() {
        if (::viewport.isInitialized) viewport.onPause()
        super.onPause()
    }
}
