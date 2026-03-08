package com.earbalance.app.ui.statistics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.earbalance.app.data.database.DailySummary
import com.earbalance.app.data.database.EarBalanceDatabase
import com.earbalance.app.data.database.UsageSession
import com.earbalance.app.data.repository.UsageRepository

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UsageRepository =
        UsageRepository.getInstance(EarBalanceDatabase.getDatabase(application))

    val last7Days: LiveData<List<DailySummary>> = repository.last7Days
    val allSessions: LiveData<List<UsageSession>> = repository.allSessions
}
