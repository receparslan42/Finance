package com.receparslan.finance.core.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.receparslan.finance.core.data.local.dao.CryptocurrencyDao
import com.receparslan.finance.core.data.local.entity.CryptocurrencyEntity

@Database(entities = [CryptocurrencyEntity::class], version = 1, exportSchema = false)
abstract class CryptocurrencyDatabase : RoomDatabase() {
    abstract fun cryptocurrencyDao(): CryptocurrencyDao
}