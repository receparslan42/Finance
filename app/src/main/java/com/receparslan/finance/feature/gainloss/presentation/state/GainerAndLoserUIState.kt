package com.receparslan.finance.feature.gainloss.presentation.state

import com.receparslan.finance.core.domain.model.Cryptocurrency

data class GainerAndLoserUIState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val gainers: List<Cryptocurrency> = emptyList(),
    val losers: List<Cryptocurrency> = emptyList(),
    val errorMessage: String = ""
)