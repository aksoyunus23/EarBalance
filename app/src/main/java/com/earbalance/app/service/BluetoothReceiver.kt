package com.earbalance.app.service

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.earbalance.app.utils.AppPreferences
import com.earbalance.app.utils.NotificationHelper

class BluetoothReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prefs = AppPreferences(context)

        when (intent.action) {
            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                val deviceName = try {
                    device?.name ?: "Bilinmeyen Cihaz"
                } catch (e: SecurityException) {
                    "Kulaklık"
                }
                prefs.connectedDeviceName = deviceName
                NotificationHelper.createChannels(context)
                NotificationHelper.sendEarChoiceNotification(context, deviceName)
            }

            BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                if (prefs.isServiceRunning) {
                    val stopIntent = Intent(context, BluetoothTrackingService::class.java).apply {
                        action = BluetoothTrackingService.ACTION_STOP_TRACKING
                    }
                    context.startService(stopIntent)
                }
            }
        }
    }
}
