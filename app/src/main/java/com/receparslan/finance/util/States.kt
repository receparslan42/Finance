package com.receparslan.finance.util

import com.receparslan.finance.model.Cryptocurrency
import com.receparslan.finance.model.KlineData

object States {
    // This data class represents the state of the home screen UI.
    data class HomeUIState(
        val isLoading: Boolean = false,
        val isLoadingMore: Boolean = false,
        val isRefreshing: Boolean = false,
        val hasMore: Boolean = false,
        val cryptoList: List<Cryptocurrency> = emptyList(),
        val errorMessage: String = "",
        val page: Int = 1
    )

    // This data class represents the state of the detail screen UI.
    data class DetailUIState(
        val isLoading: Boolean = false,
        val cryptocurrency: Cryptocurrency? = null,
        val klineDataHistory: List<KlineData> = emptyList(),
        val timePeriod: String = "24H",
        val isSaved: Boolean = false,
        val isRefreshing: Boolean = false,
        val errorMessage: String = ""
    )

    // This data class represents the state of the search screen UI.
    data class SearchUIState(
        val isLoading: Boolean = false,
        val searchResults: List<Cryptocurrency> = emptyList(),
        val query: String = "",
        val isNotFound: Boolean = false,
        val errorMessage: String = ""
    )

    // This data class represents the state of the favourite screen UI.
    data class FavouriteUIState(
        val isLoading: Boolean = false,
        val savedCryptocurrencies: List<Cryptocurrency> = emptyList(),
        val errorMessage: String = ""
    )

    // This data class represents the state of the gainer and loser screen UI.
    data class GainerAndLoserUIState(
        val isLoading: Boolean = false,
        val isRefreshing: Boolean = false,
        val gainers: List<Cryptocurrency> = emptyList(),
        val losers: List<Cryptocurrency> = emptyList(),
        val errorMessage: String = ""
    )
}