package com.receparslan.finance.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.receparslan.finance.model.Cryptocurrency

@Database(entities = [Cryptocurrency::class], version = 1, exportSchema = false)
abstract class CryptocurrencyDatabase : RoomDatabase() {
    abstract fun cryptocurrencyDao(): CryptocurrencyDao
}