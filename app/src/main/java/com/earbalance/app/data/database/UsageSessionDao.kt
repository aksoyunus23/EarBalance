package com.earbalance.app.data.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface UsageSessionDao {

    @Insert
    suspend fun insert(session: UsageSession): Long

    @Query("SELECT * FROM usage_sessions ORDER BY startTime DESC")
    fun getAllSessions(): LiveData<List<UsageSession>>

    @Query("SELECT * FROM usage_sessions WHERE date = :date ORDER BY startTime DESC")
    fun getSessionsByDate(date: String): LiveData<List<UsageSession>>

    @Query("""
        SELECT date, 
               SUM(CASE WHEN earSide = 'LEFT' THEN durationSeconds ELSE 0 END) as leftSeconds,
               SUM(CASE WHEN earSide = 'RIGHT' THEN durationSeconds ELSE 0 END) as rightSeconds,
               SUM(CASE WHEN earSide = 'BOTH' THEN durationSeconds ELSE 0 END) as bothSeconds,
               SUM(durationSeconds) as totalSeconds
        FROM usage_sessions 
        GROUP BY date 
        ORDER BY date DESC
        LIMIT 7
    """)
    fun getLast7DaysSummary(): LiveData<List<DailySummary>>

    @Query("""
        SELECT SUM(CASE WHEN earSide = 'LEFT' THEN durationSeconds ELSE 0 END) as leftSeconds,
               SUM(CASE WHEN earSide = 'RIGHT' THEN durationSeconds ELSE 0 END) as rightSeconds,
               SUM(CASE WHEN earSide = 'BOTH' THEN durationSeconds ELSE 0 END) as bothSeconds,
               SUM(durationSeconds) as totalSeconds
        FROM usage_sessions 
        WHERE date = :date
    """)
    suspend fun getDaySummaryOnce(date: String): DailySummary?

    @Query("DELETE FROM usage_sessions")
    suspend fun deleteAll()

    @Query("SELECT * FROM usage_sessions ORDER BY startTime DESC LIMIT 1")
    suspend fun getLastSession(): UsageSession?
}

data class DailySummary(
    val date: String = "",
    val leftSeconds: Long = 0,
    val rightSeconds: Long = 0,
    val bothSeconds: Long = 0,
    val totalSeconds: Long = 0
)
