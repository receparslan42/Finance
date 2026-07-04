package com.receparslan.finance.core.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.receparslan.finance.core.data.local.entity.CryptocurrencyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CryptocurrencyDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCryptocurrency(cryptocurrency: CryptocurrencyEntity)

    @Delete
    suspend fun deleteCryptocurrency(cryptocurrency: CryptocurrencyEntity)

    @Query("SELECT * from cryptocurrency ORDER BY name ASC")
    fun getAllCryptocurrencies(): Flow<List<CryptocurrencyEntity>>
}