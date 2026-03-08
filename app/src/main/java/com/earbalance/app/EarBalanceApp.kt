package com.earbalance.app

import android.app.Application
import com.earbalance.app.data.database.EarBalanceDatabase
import com.earbalance.app.data.repository.UsageRepository
import com.earbalance.app.utils.NotificationHelper

class EarBalanceApp : Application() {

    val database by lazy { EarBalanceDatabase.getDatabase(this) }
    val repository by lazy { UsageRepository.getInstance(database) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)
    }
}
