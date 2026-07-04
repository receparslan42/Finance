package com.receparslan.finance.feature.search.presentation.state

import com.receparslan.finance.core.domain.model.Cryptocurrency

data class SearchUIState(
    val isLoading: Boolean = false,
    val searchResults: List<Cryptocurrency> = emptyList(),
    val query: String = "",
    val isNotFound: Boolean = false,
    val errorMessage: String = ""
)