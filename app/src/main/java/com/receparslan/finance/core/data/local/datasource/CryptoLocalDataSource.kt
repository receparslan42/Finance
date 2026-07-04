package com.receparslan.finance.core.data.local.datasource

import com.receparslan.finance.core.data.local.entity.CryptocurrencyEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interface for local data source operations related to cryptocurrencies.
 * It encapsulates Room DAO interactions and exposes business-oriented methods.
 */
interface CryptoLocalDataSource {
    /**
     * Saves a cryptocurrency entity to the local database.
     */
    suspend fun saveCryptocurrency(cryptocurrency: CryptocurrencyEntity)

    /**
     * Deletes a cryptocurrency entity from the local database.
     */
    suspend fun deleteCryptocurrency(cryptocurrency: CryptocurrencyEntity)

    /**
     * Returns a Flow that emits the list of all saved cryptocurrency entities.
     */
    fun observeSavedCryptocurrencies(): Flow<List<CryptocurrencyEntity>>

    /**
     * Returns a Flow that emits the list of IDs of all saved cryptocurrencies.
     */
    fun observeSavedCryptoIds(): Flow<List<String>>
}
