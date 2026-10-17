package com.nova.autoclicker

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent

class ClickAccessibilityService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private var isClicking = false
    private var clickInterval = 500L
    private var targetX = 500f
    private var targetY = 500f

    private val clickRunnable = object : Runnable {
        override fun run() {
            if (!isClicking) return
            performClickAt(targetX, targetY)
            handler.postDelayed(this, clickInterval)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {
        stopClicking()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopClicking()
        instance = null
    }

    fun startClicking(x: Float, y: Float, interval: Long) {
        targetX = x
        targetY = y
        clickInterval = interval
        if (!isClicking) {
            isClicking = true
            handler.post(clickRunnable)
        }
    }

    fun stopClicking() {
        isClicking = false
        handler.removeCallbacks(clickRunnable)
    }

    private fun performClickAt(x: Float, y: Float) {
        val path = Path().apply {
            moveTo(x, y)
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 50))
            .build()

        dispatchGesture(gesture, null, null)
    }

    companion object {
        var instance: ClickAccessibilityService? = null
            private set

        fun isServiceEnabled(): Boolean = instance != null
    }
}
