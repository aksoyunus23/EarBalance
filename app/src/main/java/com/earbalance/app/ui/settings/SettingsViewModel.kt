package com.earbalance.app.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.earbalance.app.data.database.EarBalanceDatabase
import com.earbalance.app.data.repository.UsageRepository
import com.earbalance.app.utils.AppPreferences
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = AppPreferences(application)
    private val repository = UsageRepository.getInstance(EarBalanceDatabase.getDatabase(application))

    val notificationsEnabled = MutableLiveData(prefs.notificationsEnabled)
    val alertThreshold = MutableLiveData(prefs.alertThresholdMinutes)
    val checkInterval = MutableLiveData(prefs.checkIntervalMinutes)

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.notificationsEnabled = enabled
        notificationsEnabled.value = enabled
    }

    fun setAlertThreshold(minutes: Int) {
        prefs.alertThresholdMinutes = minutes
        alertThreshold.value = minutes
    }

    fun setCheckInterval(minutes: Int) {
        prefs.checkIntervalMinutes = minutes
        checkInterval.value = minutes
    }

    fun resetAllData(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteAll()
            onComplete()
        }
    }
}
