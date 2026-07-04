package com.receparslan.finance.feature.detail.presentation.state

import com.receparslan.finance.core.domain.model.Cryptocurrency
import com.receparslan.finance.feature.detail.domain.model.KlineData

data class DetailUIState(
    val isLoading: Boolean = false,
    val cryptocurrency: Cryptocurrency? = null,
    val klineDataHistory: List<KlineData> = emptyList(),
    val timePeriod: String = "24H",
    val isSaved: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String = ""
)