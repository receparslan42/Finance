package com.receparslan.finance.feature.favourites.presentation.state

import com.receparslan.finance.core.domain.model.Cryptocurrency

data class FavouriteUIState(
    val isLoading: Boolean = false,
    val savedCryptocurrencies: List<Cryptocurrency> = emptyList(),
    val errorMessage: String = ""
)