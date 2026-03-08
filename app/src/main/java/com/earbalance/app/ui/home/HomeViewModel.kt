package com.earbalance.app.ui.home

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.earbalance.app.data.database.EarBalanceDatabase
import com.earbalance.app.data.repository.UsageRepository
import com.earbalance.app.service.BluetoothTrackingService
import com.earbalance.app.utils.AppPreferences
import com.earbalance.app.utils.TimeUtils
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UsageRepository
    private val prefs: AppPreferences = AppPreferences(application)

    private val _isTracking = MutableLiveData(false)
    val isTracking: LiveData<Boolean> = _isTracking

    private val _currentEarSide = MutableLiveData("")
    val currentEarSide: LiveData<String> = _currentEarSide

    private val _elapsedSeconds = MutableLiveData(0L)
    val elapsedSeconds: LiveData<Long> = _elapsedSeconds

    private val _todayLeftSeconds = MutableLiveData(0L)
    val todayLeftSeconds: LiveData<Long> = _todayLeftSeconds

    private val _todayRightSeconds = MutableLiveData(0L)
    val todayRightSeconds: LiveData<Long> = _todayRightSeconds

    private val _balanceText = MutableLiveData("Veri Yok")
    val balanceText: LiveData<String> = _balanceText

    private val _leftPercent = MutableLiveData(50f)
    val leftPercent: LiveData<Float> = _leftPercent

    private val _rightPercent = MutableLiveData(50f)
    val rightPercent: LiveData<Float> = _rightPercent

    private val timerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val elapsed = intent.getLongExtra(BluetoothTrackingService.EXTRA_ELAPSED_SECONDS, 0)
            val isTracking = intent.getBooleanExtra(BluetoothTrackingService.EXTRA_IS_TRACKING, false)
            val earSide = intent.getStringExtra(BluetoothTrackingService.EXTRA_EAR_SIDE) ?: ""
            _isTracking.postValue(isTracking)
            _elapsedSeconds.postValue(elapsed)
            _currentEarSide.postValue(earSide)
            if (!isTracking) loadTodayData()
        }
    }

    init {
        repository = UsageRepository.getInstance(EarBalanceDatabase.getDatabase(application))
        loadTodayData()
        if (prefs.isServiceRunning) {
            _isTracking.value = true
            _currentEarSide.value = prefs.currentEarSide
            val elapsed = (System.currentTimeMillis() - prefs.sessionStartTime) / 1000
            _elapsedSeconds.value = elapsed
        }
    }

    fun registerReceiver(context: Context) {
        val filter = IntentFilter(BluetoothTrackingService.BROADCAST_TIMER_UPDATE)
        context.registerReceiver(timerReceiver, filter)
    }

    fun unregisterReceiver(context: Context) {
        try { context.unregisterReceiver(timerReceiver) } catch (e: Exception) {}
    }

    fun startTracking(earSide: String, context: Context) {
        val intent = Intent(context, BluetoothTrackingService::class.java).apply {
            action = BluetoothTrackingService.ACTION_START_TRACKING
            putExtra(BluetoothTrackingService.EXTRA_EAR_SIDE, earSide)
            putExtra(BluetoothTrackingService.EXTRA_DEVICE_NAME, "Manuel")
        }
        context.startForegroundService(intent)
        _isTracking.value = true
        _currentEarSide.value = earSide
    }

    fun stopTracking(context: Context) {
        val intent = Intent(context, BluetoothTrackingService::class.java).apply {
            action = BluetoothTrackingService.ACTION_STOP_TRACKING
        }
        context.startService(intent)
        _isTracking.value = false
        _elapsedSeconds.value = 0L
        loadTodayData()
    }

    fun loadTodayData() {
        viewModelScope.launch {
            val summary = repository.getTodaySummary()
            val left = summary?.leftSeconds ?: 0L
            val right = summary?.rightSeconds ?: 0L
            val both = summary?.bothSeconds ?: 0L
            _todayLeftSeconds.postValue(left)
            _todayRightSeconds.postValue(right)
            val (leftPct, rightPct, balanceText) = TimeUtils.calculateBalance(left, right, both)
            _leftPercent.postValue(leftPct)
            _rightPercent.postValue(rightPct)
            _balanceText.postValue(balanceText)
        }
    }
}
