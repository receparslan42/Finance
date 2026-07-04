package com.receparslan.finance.feature.detail.domain.usecase

import com.receparslan.finance.core.common.Constants
import com.receparslan.finance.core.common.Resource
import com.receparslan.finance.feature.detail.domain.model.KlineData
import com.receparslan.finance.core.domain.repository.CryptoRepository
import javax.inject.Inject
import kotlin.math.ceil

class GetHistoricalDataUseCase @Inject constructor(
    private val repository: CryptoRepository
) {
    suspend operator fun invoke(
        symbol: String,
        timePeriod: String
    ): Resource<List<KlineData>> {
        val klineDataHistoryListHolder = mutableListOf<KlineData>()

        val startTime = when (timePeriod) {
            "24H" -> System.currentTimeMillis() - Constants.TimeMillis.MILLIS_PER_DAY
            "1W" -> System.currentTimeMillis() - Constants.TimeMillis.MILLIS_PER_WEEK
            "1M" -> System.currentTimeMillis() - Constants.TimeMillis.MILLIS_PER_MONTH
            "6M" -> System.currentTimeMillis() - Constants.TimeMillis.MILLIS_PER_6MONTH
            "1Y" -> System.currentTimeMillis() - Constants.TimeMillis.MILLIS_PER_YEAR
            "5Y" -> System.currentTimeMillis() - Constants.TimeMillis.MILLIS_PER_5YEAR
            else -> System.currentTimeMillis() - Constants.TimeMillis.MILLIS_PER_DAY
        }

        val endTime = (System.currentTimeMillis() / 1000) * 1000

        val interval = when (timePeriod) {
            "24H" -> "1m"
            "1W" -> "1h"
            else -> "1d"
        }

        val intervalTimeMillis = when (timePeriod) {
            "24H" -> Constants.TimeMillis.MILLIS_PER_MINUTE
            "1W" -> Constants.TimeMillis.MILLIS_PER_HOUR
            else -> Constants.TimeMillis.MILLIS_PER_DAY
        }

        val maxPointsPerRequest = 1000L

        val chunks =
            ceil(((endTime - startTime).toDouble() / intervalTimeMillis) / maxPointsPerRequest)
                .toInt()

        repeat(chunks) { chunkIndex ->
            val chunkStart = startTime + (chunkIndex * maxPointsPerRequest * intervalTimeMillis)
            val chunkEnd =
                minOf(endTime, chunkStart + (maxPointsPerRequest * intervalTimeMillis) - 1)

            val resource = repository.getHistoricalDataByRange(
                symbol = symbol,
                startTime = chunkStart,
                endTime = chunkEnd,
                interval = interval,
            )

            when (resource) {
                is Resource.Success -> klineDataHistoryListHolder.addAll(resource.data)
                is Resource.Error -> return Resource.Error(resource.message)
            }
        }

        return Resource.Success(klineDataHistoryListHolder)
    }
}