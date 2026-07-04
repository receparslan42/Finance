package com.receparslan.finance.core.data.remote.datasource

import com.receparslan.finance.core.data.remote.dto.CryptocurrencyDto
import com.receparslan.finance.feature.detail.data.remote.dto.KlineDataDto
import com.receparslan.finance.feature.search.data.remote.dto.SearchResponseDto

// This interface defines the operations for fetching remote cryptocurrency data.
// It abstracts the network calls and returns pure DTOs, encapsulating Retrofit-specific details.
interface CryptoRemoteDataSource {
    suspend fun getCryptoByIDs(ids: String): List<CryptocurrencyDto>
    suspend fun getCryptoListByPage(page: Int): List<CryptocurrencyDto>
    suspend fun getCryptoListByNames(names: String): List<CryptocurrencyDto>
    suspend fun searchCrypto(query: String): SearchResponseDto
    suspend fun getHistoricalDataByRange(
        symbol: String,
        startTime: Long,
        endTime: Long,
        interval: String
    ): List<KlineDataDto>
}