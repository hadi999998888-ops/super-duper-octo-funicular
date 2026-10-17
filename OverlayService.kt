package com.nova.autoclicker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.core.app.NotificationCompat

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var settingsManager: SettingsManager

    private var panelView: View? = null
    private var targetView: View? = null
    private var collapsedView: View? = null

    private var isTargetVisible = true
    private var isRunning = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        settingsManager = SettingsManager(this)

        startForegroundService()
        createTargetOverlay()
        createPanelOverlay()
        createCollapsedOverlay()
    }

    private fun startForegroundService() {
        val channelId = "nova_clicker_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Nova AutoClicker Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Nova AutoClicker")
            .setContentText("Автокликер запущен")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .build()

        startForeground(1, notification)
    }

    private fun createTargetOverlay() {
        targetView = View(this).apply {
            val drawable = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.argb(180, 255, 0, 0))
                setStroke(4, Color.YELLOW)
            }
            background = drawable
        }

        val params = WindowManager.LayoutParams(
            120, 120,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 300
            y = 500
        }

        makeDraggable(targetView!!, params)
        windowManager.addView(targetView, params)
    }

    private fun createPanelOverlay() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 30, 30, 30)

            val backgroundDrawable = GradientDrawable().apply {
                setColor(Color.parseColor("#E6000000"))
                setCornerRadius(24f)
                setStroke(3, Color.parseColor("#FFD700"))
            }
            background = backgroundDrawable
        }

        val title = TextView(this).apply {
            text = "NOVA AUTOCLICKER"
            setTextColor(Color.parseColor("#FFD700"))
            textSize = 16f
            setPadding(0, 0, 0, 10)
        }
        layout.addView(title)

        val statusText = TextView(this).apply {
            text = "Статус: ВЫКЛЮЧЕН"
            setTextColor(Color.WHITE)
            textSize = 14f
            setPadding(0, 0, 0, 15)
        }
        layout.addView(statusText)

        val spinner = Spinner(this)
        val intervals = arrayOf("50 мс", "100 мс", "250 мс", "500 мс", "1000 мс")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, intervals)
        spinner.adapter = adapter

        val currentSaved = settingsManager.clickInterval
        val defaultIdx = when (currentSaved) {
            50L -> 0
            100L -> 1
            250L -> 2
            500L -> 3
            1000L -> 4
            else -> 3
        }
        spinner.setSelection(defaultIdx)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selected = when (position) {
                    0 -> 50L
                    1 -> 100L
                    2 -> 250L
                    3 -> 500L
                    4 -> 1000L
                    else -> 500L
                }
                settingsManager.clickInterval = selected
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        layout.addView(spinner)

        fun createStyledButton(txt: String): Button {
            return Button(this).apply {
                text = txt
                setTextColor(Color.BLACK)
                textSize = 12f
                val btnBg = GradientDrawable().apply {
                    setColor(Color.parseColor("#FFD700"))
                    setCornerRadius(16f)
                }
                background = btnBg
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                lp.setMargins(0, 8, 0, 8)
                layoutParams = lp
            }
        }

        val btnStart = createStyledButton("▶ СТАРТ")
        val btnStop = createStyledButton("■ СТОП")
        val btnToggle = createStyledButton("🎯 ПОКАЗАТЬ / СКРЫТЬ ЦЕЛЬ")
        val btnMinimize = createStyledButton("− СВЕРНУТЬ")
        val btnClose = createStyledButton("× ЗАКРЫТЬ")

        btnStart.setOnClickListener {
            if (!ClickAccessibilityService.isServiceEnabled()) {
                return@setOnClickListener
            }
            val location = IntArray(2)
            targetView?.getLocationOnScreen(location)
            val centerX = location[0] + (targetView?.width ?: 120) / 2f
            val centerY = location[1] + (targetView?.height ?: 120) / 2f

            ClickAccessibilityService.instance?.startClicking(centerX, centerY, settingsManager.clickInterval)
            isRunning = true
            statusText.text = "Статус: РАБОТАЕТ"
            statusText.setTextColor(Color.GREEN)
        }

        btnStop.setOnClickListener {
            ClickAccessibilityService.instance?.stopClicking()
            isRunning = false
            statusText.text = "Статус: ВЫКЛЮЧЕН"
            statusText.setTextColor(Color.WHITE)
        }

        btnToggle.setOnClickListener {
            isTargetVisible = !isTargetVisible
            targetView?.visibility = if (isTargetVisible) View.VISIBLE else View.GONE
        }

        btnMinimize.setOnClickListener {
            panelView?.visibility = View.GONE
            collapsedView?.visibility = View.VISIBLE
        }

        btnClose.setOnClickListener {
            stopSelf()
        }

        layout.addView(btnStart)
        layout.addView(btnStop)
        layout.addView(btnToggle)
        layout.addView(btnMinimize)
        layout.addView(btnClose)

        panelView = layout

        val params = WindowManager.LayoutParams(
            500, WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 100
        }

        makeDraggable(panelView!!, params)
        windowManager.addView(panelView, params)
    }

    private fun createCollapsedOverlay() {
        val btn = Button(this).apply {
            text = "+"
            setTextColor(Color.BLACK)
            textSize = 24f
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#FFD700"))
                setStroke(2, Color.WHITE)
            }
            background = bg
        }

        val params = WindowManager.LayoutParams(
            130, 130,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 50
            y = 300
        }

        btn.setOnClickListener {
            collapsedView?.visibility = View.GONE
            panelView?.visibility = View.VISIBLE
        }

        makeDraggable(btn, params, isClickableBtn = true)
        collapsedView = btn
        collapsedView?.visibility = View.GONE
        windowManager.addView(collapsedView, params)
    }

    private fun makeDraggable(view: View, params: WindowManager.LayoutParams, isClickableBtn: Boolean = false) {
        view.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private var isMove = false

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (event == null) return false
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        isMove = false
                        return !isClickableBtn
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - initialTouchX).toInt()
                        val dy = (event.rawY - initialTouchY).toInt()
                        if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                            isMove = true
                        }
                        params.x = initialX + dx
                        params.y = initialY + dy
                        windowManager.updateViewLayout(view, params)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (isClickableBtn && !isMove) {
                            v?.performClick()
                        }
                        return true
                    }
                }
                return false
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        ClickAccessibilityService.instance?.stopClicking()
        if (panelView != null) windowManager.removeView(panelView)
        if (targetView != null) windowManager.removeView(targetView)
        if (collapsedView != null) windowManager.removeView(collapsedView)
    }
}
