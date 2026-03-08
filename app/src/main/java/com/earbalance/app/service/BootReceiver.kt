package com.earbalance.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.earbalance.app.utils.AppPreferences

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs = AppPreferences(context)
            prefs.isServiceRunning = false
            prefs.currentEarSide = ""
            prefs.sessionStartTime = 0L
        }
    }
}
