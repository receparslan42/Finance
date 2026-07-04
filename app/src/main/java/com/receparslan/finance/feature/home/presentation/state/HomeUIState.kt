package com.receparslan.finance.feature.home.presentation.state

import com.receparslan.finance.core.domain.model.Cryptocurrency

data class HomeUIState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val hasMore: Boolean = false,
    val cryptoList: List<Cryptocurrency> = emptyList(),
    val errorMessage: String = "",
    val page: Int = 1
)