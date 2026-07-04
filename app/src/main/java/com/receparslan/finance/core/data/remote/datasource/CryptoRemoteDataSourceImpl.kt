package com.receparslan.finance.core.data.remote.datasource

import com.receparslan.finance.core.data.remote.api.BinanceApiService
import com.receparslan.finance.core.data.remote.api.CoinGeckoApiService
import com.receparslan.finance.core.data.remote.dto.CryptocurrencyDto
import com.receparslan.finance.feature.detail.data.remote.dto.KlineDataDto
import com.receparslan.finance.feature.search.data.remote.dto.SearchResponseDto
import retrofit2.HttpException
import javax.inject.Inject

class CryptoRemoteDataSourceImpl @Inject constructor(
    private val coinGeckoApiService: CoinGeckoApiService,
    private val binanceApiService: BinanceApiService
) : CryptoRemoteDataSource {

    override suspend fun getCryptoByIDs(ids: String): List<CryptocurrencyDto> {
        val response = coinGeckoApiService.getCryptoByIDs(ids = ids)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Empty body")
        } else {
            throw HttpException(response)
        }
    }

    override suspend fun getCryptoListByPage(page: Int): List<CryptocurrencyDto> {
        val response = coinGeckoApiService.getCryptoListByPage(page = page)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Empty body")
        } else {
            throw HttpException(response)
        }
    }

    override suspend fun getCryptoListByNames(names: String): List<CryptocurrencyDto> {
        val response = coinGeckoApiService.getCryptoListByNames(names = names)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Empty body")
        } else {
            throw HttpException(response)
        }
    }

    override suspend fun searchCrypto(query: String): SearchResponseDto {
        val response = coinGeckoApiService.searchCrypto(query = query)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Empty body")
        } else {
            throw HttpException(response)
        }
    }

    override suspend fun getHistoricalDataByRange(
        symbol: String,
        startTime: Long,
        endTime: Long,
        interval: String
    ): List<KlineDataDto> {
        // BinanceApiService returns List<KlineDataDto> directly, not Response<List<KlineDataDto>>
        return binanceApiService.getHistoricalDataByRange(
            symbol = symbol,
            startTime = startTime,
            endTime = endTime,
            interval = interval
        )
    }
}