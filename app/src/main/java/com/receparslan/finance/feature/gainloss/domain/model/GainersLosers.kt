package com.receparslan.finance.feature.gainloss.domain.model

import com.receparslan.finance.core.domain.model.Cryptocurrency

data class GainersLosers(
    val gainers: List<Cryptocurrency>,
    val losers: List<Cryptocurrency>
)