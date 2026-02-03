package com.receparslan.finance.util

import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.receparslan.finance.model.KlineData

// Constants object to hold constant values used in the application
object Constants {
    const val COINGECKO_BASE_URL = "https://api.coingecko.com/api/v3/" // Base URL for CoinGecko API

    // URL for CoinGecko gainers and losers page
    const val COINGECKO_GAINERS_LOSERS_URL = "https://www.coingecko.com/en/crypto-gainers-losers"

    const val BINANCE_BASE_URL = "https://api.binance.com/api/v3/" // Base URL for Binance API

    // Extra keys for storing additional data
    object ExtraKeys {
        val klineDataMap = ExtraStore.Key<Map<Long, KlineData>>()
    }

    // Time duration constants in milliseconds
    object TimeMillis {
        const val MILLIS_PER_MINUTE = 60 * 1000L // Number of milliseconds in a minute
        const val MILLIS_PER_HOUR = 60 * MILLIS_PER_MINUTE // Number of milliseconds in an hour
        const val MILLIS_PER_DAY = 24 * MILLIS_PER_HOUR // Number of milliseconds in a day
        const val MILLIS_PER_WEEK = 7 * MILLIS_PER_DAY // Number of milliseconds in a week
        const val MILLIS_PER_MONTH = 30 * MILLIS_PER_DAY // Number of milliseconds in a month
        const val MILLIS_PER_6MONTH = 6 * MILLIS_PER_MONTH // Number of milliseconds in six months
        const val MILLIS_PER_YEAR = 365 * MILLIS_PER_DAY // Number of milliseconds in a year
        const val MILLIS_PER_5YEAR = 5 * MILLIS_PER_YEAR // Number of milliseconds in five years
    }
}