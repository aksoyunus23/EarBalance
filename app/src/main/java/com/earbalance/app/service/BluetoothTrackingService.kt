package com.earbalance.app.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.earbalance.app.data.database.EarBalanceDatabase
import com.earbalance.app.data.database.UsageSession
import com.earbalance.app.data.repository.UsageRepository
import com.earbalance.app.utils.AppPreferences
import com.earbalance.app.utils.NotificationHelper
import com.earbalance.app.utils.TimeUtils
import kotlinx.coroutines.*

class BluetoothTrackingService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var prefs: AppPreferences
    private lateinit var repository: UsageRepository
    private var timerJob: Job? = null
    private var sessionStartTime = 0L
    private var currentEarSide = ""

    companion object {
        const val ACTION_START_TRACKING = "com.earbalance.START_TRACKING"
        const val ACTION_STOP_TRACKING = "com.earbalance.STOP_TRACKING"
        const val EXTRA_EAR_SIDE = "ear_side"
        const val EXTRA_DEVICE_NAME = "device_name"
        const val BROADCAST_TIMER_UPDATE = "com.earbalance.TIMER_UPDATE"
        const val EXTRA_ELAPSED_SECONDS = "elapsed_seconds"
        const val EXTRA_IS_TRACKING = "is_tracking"
    }

    override fun onCreate() {
        super.onCreate()
        prefs = AppPreferences(this)
        repository = UsageRepository.getInstance(EarBalanceDatabase.getDatabase(this))
        NotificationHelper.createChannels(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TRACKING -> {
                val earSide = intent.getStringExtra(EXTRA_EAR_SIDE) ?: return START_NOT_STICKY
                val deviceName = intent.getStringExtra(EXTRA_DEVICE_NAME) ?: ""
                startTracking(earSide, deviceName)
            }
            ACTION_STOP_TRACKING -> stopTracking()
        }
        return START_STICKY
    }

    private fun startTracking(earSide: String, deviceName: String) {
        currentEarSide = earSide
        sessionStartTime = System.currentTimeMillis()
        prefs.currentEarSide = earSide
        prefs.sessionStartTime = sessionStartTime
        prefs.connectedDeviceName = deviceName
        prefs.isServiceRunning = true

        val notification = NotificationHelper.buildServiceNotification(this, earSide, 0)
        startForeground(NotificationHelper.NOTIFICATION_ID_SERVICE, notification)

        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isActive) {
                delay(1000)
                val elapsed = (System.currentTimeMillis() - sessionStartTime) / 1000
                updateNotification(elapsed)
                broadcastUpdate(elapsed, true)
            }
        }
    }

    private fun stopTracking() {
        timerJob?.cancel()

        if (sessionStartTime > 0 && currentEarSide.isNotEmpty()) {
            val endTime = System.currentTimeMillis()
            val duration = (endTime - sessionStartTime) / 1000

            if (duration > 5) {
                serviceScope.launch {
                    val session = UsageSession(
                        startTime = sessionStartTime,
                        endTime = endTime,
                        durationSeconds = duration,
                        earSide = currentEarSide,
                        deviceName = prefs.connectedDeviceName,
                        date = TimeUtils.todayString()
                    )
                    repository.insert(session)
                }
            }
        }

        prefs.isServiceRunning = false
        prefs.currentEarSide = ""
        prefs.sessionStartTime = 0L
        sessionStartTime = 0L
        currentEarSide = ""

        broadcastUpdate(0, false)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun updateNotification(elapsedSeconds: Long) {
        val notification = NotificationHelper.buildServiceNotification(this, currentEarSide, elapsedSeconds)
        val manager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.notify(NotificationHelper.NOTIFICATION_ID_SERVICE, notification)
    }

    private fun broadcastUpdate(elapsedSeconds: Long, isTracking: Boolean) {
        val intent = Intent(BROADCAST_TIMER_UPDATE).apply {
            putExtra(EXTRA_ELAPSED_SECONDS, elapsedSeconds)
            putExtra(EXTRA_IS_TRACKING, isTracking)
            putExtra(EXTRA_EAR_SIDE, currentEarSide)
        }
        sendBroadcast(intent)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
