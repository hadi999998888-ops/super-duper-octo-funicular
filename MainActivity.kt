package com.nova.autoclicker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tvOverlayStatus: TextView
    private lateinit var tvAccessibilityStatus: TextView
    private lateinit var btnGrantOverlay: Button
    private lateinit var btnGrantAccessibility: Button
    private lateinit var btnStartService: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(createMainLayout())

        tvOverlayStatus = findViewById(101)
        tvAccessibilityStatus = findViewById(102)
        btnGrantOverlay = findViewById(103)
        btnGrantAccessibility = findViewById(104)
        btnStartService = findViewById(105)

        btnGrantOverlay.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            } else {
                Toast.makeText(this, "Разрешение уже предоставлено", Toast.LENGTH_SHORT).show()
            }
        }

        btnGrantAccessibility.setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }

        btnStartService.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Сначала предоставьте разрешение поверх других окон", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!ClickAccessibilityService.isServiceEnabled()) {
                Toast.makeText(this, "Сначала включите службу специальных возможностей", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, OverlayService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            Toast.makeText(this, "Панель управления запущена", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        updatePermissionsStatus()
    }

    private fun updatePermissionsStatus() {
        val hasOverlay = Settings.canDrawOverlays(this)
        tvOverlayStatus.text = if (hasOverlay) "Поверх других окон: РАЗРЕШЕНО" else "Поверх других окон: НЕТ ДОСТУПА"
        tvOverlayStatus.setTextColor(if (hasOverlay) android.graphics.Color.GREEN else android.graphics.Color.RED)

        val hasAccessibility = ClickAccessibilityService.isServiceEnabled()
        tvAccessibilityStatus.text = if (hasAccessibility) "Служба кликера: ВКЛЮЧЕНА" else "Служба кликера: ВЫКЛЮЧЕНА"
        tvAccessibilityStatus.setTextColor(if (hasAccessibility) android.graphics.Color.GREEN else android.graphics.Color.RED)
    }

    private fun createMainLayout(): android.view.View {
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 80, 50, 50)
            setBackgroundColor(android.graphics.Color.BLACK)
        }

        val title = TextView(this).apply {
            text = "Nova AutoClicker"
            textSize = 28f
            setTextColor(android.graphics.Color.parseColor("#FFD700"))
            setTypeface(null, android.graphics.Typeface.BOLD)
            gravity = android.view.Gravity.CENTER
            setPadding(0, 0, 0, 40)
        }
        layout.addView(title)

        val tv1 = TextView(this).apply {
            id = 101
            textSize = 16f
            setPadding(0, 10, 0, 10)
        }
        layout.addView(tv1)

        val b1 = Button(this).apply {
            id = 103
            text = "1. Разрешить поверх окон"
            setTextColor(android.graphics.Color.BLACK)
            setBackgroundColor(android.graphics.Color.parseColor("#FFD700"))
        }
        layout.addView(b1)

        val tv2 = TextView(this).apply {
            id = 102
            textSize = 16f
            setPadding(0, 30, 0, 10)
        }
        layout.addView(tv2)

        val b2 = Button(this).apply {
            id = 104
            text = "2. Включить спец. возможности"
            setTextColor(android.graphics.Color.BLACK)
            setBackgroundColor(android.graphics.Color.parseColor("#FFD700"))
        }
        layout.addView(b2)

        val space = android.view.View(this).apply {
            layoutParams = android.widget.LinearLayout.LayoutParams(1, 60)
        }
        layout.addView(space)

        val b3 = Button(this).apply {
            id = 105
            text = "🚀 Запустить автокликер"
            textSize = 18f
            setTextColor(android.graphics.Color.BLACK)
            setBackgroundColor(android.graphics.Color.parseColor("#FFD700"))
        }
        layout.addView(b3)

        return layout
    }
}
