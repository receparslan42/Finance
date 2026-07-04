package com.receparslan.finance.core.data.remote.api

import com.receparslan.finance.BuildConfig
import com.receparslan.finance.core.data.remote.dto.CryptocurrencyDto
import com.receparslan.finance.feature.search.data.remote.dto.SearchResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

// This interface defines the API endpoints for fetching cryptocurrency data from CoinGecko
interface CoinGeckoApiService {
    // Fetch a list of cryptocurrencies with pagination support
    @GET("coins/markets")
    suspend fun getCryptoListByPage(
        @Header("x-cg-demo-api-key") apiKey: String = BuildConfig.API_KEY,
        @Query("vs_currency") vsCurrency: String = "usd",
        @Query("per_page") perPage: Int = 250,
        @Query("page") page: Int
    ): Response<List<CryptocurrencyDto>>

    // Fetch cryptocurrencies by their names (as a comma-separated string)
    @GET("coins/markets")
    suspend fun getCryptoListByNames(
        @Header("x-cg-demo-api-key") apiKey: String = BuildConfig.API_KEY,
        @Query("vs_currency") vsCurrency: String = "usd",
        @Query("names") names: String
    ): Response<List<CryptocurrencyDto>>

    // Fetch cryptocurrencies by their IDs (as a comma-separated string)
    @GET("coins/markets")
    suspend fun getCryptoByIDs(
        @Header("x-cg-demo-api-key") apiKey: String = BuildConfig.API_KEY,
        @Query("vs_currency") vsCurrency: String = "usd",
        @Query("ids") ids: String
    ): Response<List<CryptocurrencyDto>>

    // Search for cryptocurrencies based on a query string
    @GET("search")
    suspend fun searchCrypto(
        @Header("x-cg-demo-api-key") apiKey: String = BuildConfig.API_KEY,
        @Query("query") query: String
    ): Response<SearchResponseDto>
}