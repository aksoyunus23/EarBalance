package com.earbalance.app.utils

import android.content.Context
import android.content.SharedPreferences

class AppPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("ear_balance_prefs", Context.MODE_PRIVATE)

    var notificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATIONS, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFICATIONS, value).apply()

    var alertThresholdMinutes: Int
        get() = prefs.getInt(KEY_ALERT_THRESHOLD, 30)
        set(value) = prefs.edit().putInt(KEY_ALERT_THRESHOLD, value).apply()

    var checkIntervalMinutes: Int
        get() = prefs.getInt(KEY_CHECK_INTERVAL, 15)
        set(value) = prefs.edit().putInt(KEY_CHECK_INTERVAL, value).apply()

    var isServiceRunning: Boolean
        get() = prefs.getBoolean(KEY_SERVICE_RUNNING, false)
        set(value) = prefs.edit().putBoolean(KEY_SERVICE_RUNNING, value).apply()

    var currentEarSide: String
        get() = prefs.getString(KEY_CURRENT_EAR, "") ?: ""
        set(value) = prefs.edit().putString(KEY_CURRENT_EAR, value).apply()

    var sessionStartTime: Long
        get() = prefs.getLong(KEY_SESSION_START, 0L)
        set(value) = prefs.edit().putLong(KEY_SESSION_START, value).apply()

    var connectedDeviceName: String
        get() = prefs.getString(KEY_DEVICE_NAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_DEVICE_NAME, value).apply()

    companion object {
        private const val KEY_NOTIFICATIONS = "notifications_enabled"
        private const val KEY_ALERT_THRESHOLD = "alert_threshold_minutes"
        private const val KEY_CHECK_INTERVAL = "check_interval_minutes"
        private const val KEY_SERVICE_RUNNING = "service_running"
        private const val KEY_CURRENT_EAR = "current_ear_side"
        private const val KEY_SESSION_START = "session_start_time"
        private const val KEY_DEVICE_NAME = "connected_device_name"
    }
}
