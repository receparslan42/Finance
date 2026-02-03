package com.receparslan.finance.service

import com.receparslan.finance.BuildConfig
import com.receparslan.finance.model.Cryptocurrency
import com.receparslan.finance.model.CryptocurrencyList
import com.receparslan.finance.model.KlineData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

// This interface defines the API endpoints for fetching cryptocurrency data from CoinGecko
interface CoinGeckoApiService {
    // Fetch a list of cryptocurrencies with pagination support
    @GET("coins/markets")
    suspend fun getCryptoListByPage(
        @Header("x_cg_demo_api_key") apiKey: String = BuildConfig.API_KEY,
        @Query("vs_currency") vsCurrency: String = "usd",
        @Query("per_page") perPage: Int = 250,
        @Query("page") page: Int
    ): Response<List<Cryptocurrency>>

    // Fetch cryptocurrencies by their names (as a comma-separated string)
    @GET("coins/markets")
    suspend fun getCryptoListByNames(
        @Header("x_cg_demo_api_key") apiKey: String = BuildConfig.API_KEY,
        @Query("vs_currency") vsCurrency: String = "usd",
        @Query("names") names: String
    ): Response<List<Cryptocurrency>>

    // Fetch cryptocurrencies by their IDs (as a comma-separated string)
    @GET("coins/markets")
    suspend fun getCryptoByIDs(
        @Header("x_cg_demo_api_key") apiKey: String = BuildConfig.API_KEY,
        @Query("vs_currency") vsCurrency: String = "usd",
        @Query("ids") ids: String
    ): Response<List<Cryptocurrency>>

    // Search for cryptocurrencies based on a query string
    @GET("search")
    suspend fun searchCrypto(
        @Header("x_cg_demo_api_key") apiKey: String = BuildConfig.API_KEY,
        @Query("query") query: String
    ): Response<CryptocurrencyList>
}

// This interface defines the API endpoints for fetching historical data from Binance
interface BinanceApiService {
    @GET("klines")
    suspend fun getHistoricalDataByRange(
        @Query("limit") limit: Int = 1000,
        @Query("symbol") symbol: String,
        @Query("startTime") startTime: Long,
        @Query("endTime") endTime: Long,
        @Query("interval") interval: String
    ): List<KlineData>
}