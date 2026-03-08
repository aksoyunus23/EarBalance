package com.earbalance.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [UsageSession::class],
    version = 1,
    exportSchema = false
)
abstract class EarBalanceDatabase : RoomDatabase() {

    abstract fun usageSessionDao(): UsageSessionDao

    companion object {
        @Volatile
        private var INSTANCE: EarBalanceDatabase? = null

        fun getDatabase(context: Context): EarBalanceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EarBalanceDatabase::class.java,
                    "ear_balance_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
