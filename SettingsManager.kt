package com.nova.autoclicker

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("nova_clicker_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_INTERVAL = "click_interval"
        private const val DEFAULT_INTERVAL = 500L
    }

    var clickInterval: Long
        get() = prefs.getLong(KEY_INTERVAL, DEFAULT_INTERVAL)
        set(value) = prefs.edit().putLong(KEY_INTERVAL, value).apply()
}
