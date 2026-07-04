package com.receparslan.finance.core.data.remote.api

import com.receparslan.finance.feature.detail.data.remote.dto.KlineDataDto
import retrofit2.http.GET
import retrofit2.http.Query

// This interface defines the API endpoints for fetching historical data from Binance
interface BinanceApiService {
    @GET("klines")
    suspend fun getHistoricalDataByRange(
        @Query("limit") limit: Int = 1000,
        @Query("symbol") symbol: String,
        @Query("startTime") startTime: Long,
        @Query("endTime") endTime: Long,
        @Query("interval") interval: String
    ): List<KlineDataDto>
}