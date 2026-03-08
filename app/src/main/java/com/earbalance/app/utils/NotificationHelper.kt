package com.earbalance.app.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.earbalance.app.MainActivity
import com.earbalance.app.R

object NotificationHelper {

    const val CHANNEL_TRACKING = "ear_balance_tracking"
    const val CHANNEL_ALERTS = "ear_balance_alerts"
    const val NOTIFICATION_ID_SERVICE = 1001
    const val NOTIFICATION_ID_ALERT = 1002
    const val NOTIFICATION_ID_POPUP = 1003

    fun createChannels(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val trackingChannel = NotificationChannel(
            CHANNEL_TRACKING,
            "Kullanım Takibi",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Bluetooth kulaklık kullanım takibi"
            setShowBadge(false)
        }

        val alertChannel = NotificationChannel(
            CHANNEL_ALERTS,
            "Denge Uyarıları",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Kulak dengesi uyarıları"
        }

        manager.createNotificationChannel(trackingChannel)
        manager.createNotificationChannel(alertChannel)
    }

    fun buildServiceNotification(context: Context, earSide: String, elapsedSeconds: Long): Notification {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val sideText = when (earSide) {
            "LEFT" -> "Sol Kulak 👂"
            "RIGHT" -> "Sağ Kulak 👂"
            "BOTH" -> "Her İki Kulak 👂👂"
            else -> "Takip Ediliyor"
        }

        return NotificationCompat.Builder(context, CHANNEL_TRACKING)
            .setContentTitle("Ear Balance - $sideText")
            .setContentText("Süre: ${TimeUtils.formatDuration(elapsedSeconds)}")
            .setSmallIcon(R.drawable.ic_ear)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    fun sendBalanceAlert(context: Context, leftPct: Float, rightPct: Float) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val prefs = AppPreferences(context)
        if (!prefs.notificationsEnabled) return

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ALERTS)
            .setContentTitle("⚠️ Kulak Dengesi Uyarısı")
            .setContentText("Sol: %.0f%% | Sağ: %.0f%% - Dengeyi gözden geçir!".format(leftPct, rightPct))
            .setSmallIcon(R.drawable.ic_ear)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(NOTIFICATION_ID_ALERT, notification)
    }

    fun sendEarChoiceNotification(context: Context, deviceName: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val prefs = AppPreferences(context)
        if (!prefs.notificationsEnabled) return

        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("show_ear_choice", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ALERTS)
            .setContentTitle("🎧 Kulaklık Bağlandı")
            .setContentText("$deviceName bağlandı. Hangi kulağı kullanıyorsun?")
            .setSmallIcon(R.drawable.ic_ear)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        manager.notify(NOTIFICATION_ID_POPUP, notification)
    }
}
