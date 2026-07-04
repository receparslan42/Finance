package com.receparslan.finance.core.domain.repository

import com.receparslan.finance.core.domain.model.Cryptocurrency
import com.receparslan.finance.feature.detail.domain.model.KlineData
import com.receparslan.finance.feature.gainloss.domain.model.GainersLosers
import com.receparslan.finance.core.common.Resource
import kotlinx.coroutines.flow.Flow

interface CryptoRepository {
    // API data retrieval
    suspend fun getCryptoByIDs(ids: String): Resource<List<Cryptocurrency>>
    suspend fun getCryptoListByPage(page: Int): Resource<List<Cryptocurrency>>
    suspend fun getCryptoListByNames(names: String): Resource<List<Cryptocurrency>>
    suspend fun searchCrypto(query: String): Resource<List<Cryptocurrency>>
    suspend fun getGainersAndLosers(): Resource<GainersLosers>

    // Historical data retrieval
    suspend fun getHistoricalDataByRange(
        symbol: String,
        startTime: Long,
        endTime: Long,
        interval: String
    ): Resource<List<KlineData>>

    // Database operations
    suspend fun saveCryptoToDb(cryptocurrency: Cryptocurrency): Resource<Unit>
    suspend fun deleteCryptoFromDb(cryptocurrency: Cryptocurrency): Resource<Unit>
    fun getAllSavedCryptoIDsFlow(): Flow<Resource<List<String>>>
    fun observeSavedCryptocurrencies(): Flow<Resource<List<Cryptocurrency>>>
}