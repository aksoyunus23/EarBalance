package com.earbalance.app.data.repository

import androidx.lifecycle.LiveData
import com.earbalance.app.data.database.DailySummary
import com.earbalance.app.data.database.EarBalanceDatabase
import com.earbalance.app.data.database.UsageSession
import com.earbalance.app.data.database.UsageSessionDao
import java.text.SimpleDateFormat
import java.util.*

class UsageRepository(private val dao: UsageSessionDao) {

    val allSessions: LiveData<List<UsageSession>> = dao.getAllSessions()
    val last7Days: LiveData<List<DailySummary>> = dao.getLast7DaysSummary()

    suspend fun insert(session: UsageSession): Long {
        return dao.insert(session)
    }

    fun getSessionsByDate(date: String): LiveData<List<UsageSession>> {
        return dao.getSessionsByDate(date)
    }

    suspend fun getTodaySummary(): DailySummary? {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return dao.getDaySummaryOnce(today)
    }

    suspend fun deleteAll() {
        dao.deleteAll()
    }

    companion object {
        @Volatile
        private var INSTANCE: UsageRepository? = null

        fun getInstance(database: EarBalanceDatabase): UsageRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = UsageRepository(database.usageSessionDao())
                INSTANCE = instance
                instance
            }
        }
    }
}
