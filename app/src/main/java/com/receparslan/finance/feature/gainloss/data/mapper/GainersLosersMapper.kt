package com.receparslan.finance.feature.gainloss.data.mapper

import com.receparslan.finance.core.data.remote.CoinGeckoScraper
import com.receparslan.finance.feature.gainloss.domain.model.GainersLosers

fun CoinGeckoScraper.GainersLosers.toDomain(): GainersLosers {
    return GainersLosers(
        gainers = gainers,
        losers = losers
    )
}