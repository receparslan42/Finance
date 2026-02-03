package com.receparslan.finance.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.receparslan.finance.model.Cryptocurrency
import com.receparslan.finance.repository.CryptoRepository
import com.receparslan.finance.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class GainerAndLoserViewModel @Inject constructor(
    private val cryptoRepository: CryptoRepository
) : ViewModel() {
    // This variable holds the list of cryptocurrencies that are gainers
    val cryptocurrencyGainerList = mutableStateListOf<Cryptocurrency>()

    // This variable holds the list of cryptocurrencies that are losers
    val cryptocurrencyLoserList = mutableStateListOf<Cryptocurrency>()

    var isLoading = mutableStateOf(false) // Loading state to show/hide loading indicators

    // Initialize the ViewModel by fetching the gainers and losers list
    init {
        setGainersAndLosersList()
    }

    // This function fetches the list of gainers and losers from the CoinGecko website using Jsoup.
    fun setGainersAndLosersList() = viewModelScope.launch {
        isLoading.value = true // Set loading state to true

        // Fetch gainers and losers data from the repository
        val resource = withContext(Dispatchers.IO) { cryptoRepository.getGainersAndLosers() }

        // Fetch gainers and losers data from the repository and update the state variables accordingly
        when (resource) {
            is Resource.Success -> {
                cryptocurrencyGainerList.addAll(resource.data["gainers"] ?: emptyList())
                cryptocurrencyLoserList.addAll(resource.data["losers"] ?: emptyList())
            }

            is Resource.Error -> Log.e(
                "GainerAndLoserVM",
                "Error fetching gainers and losers: ${resource.message}"
            )
        }

        isLoading.value = false
    }
}