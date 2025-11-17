package com.receparslan.finance.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.receparslan.finance.model.Cryptocurrency
import com.receparslan.finance.service.CryptocurrencyAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeViewModel() : ViewModel() {
    // This variable holds the list of cryptocurrencies that will be displayed on the home screen.
    val cryptocurrencyList = mutableStateListOf<Cryptocurrency>()

    var isLoading = mutableStateOf(false) // This variable is used to track the loading state of the cryptocurrency list.

    private var page = 1 // This variable is used to keep track of the current page number for pagination.

    init {
        addCryptocurrenciesByPage(page) // Fetch the initial list of cryptocurrencies
    }

    // This function is used to refresh the home screen by clearing the existing list and fetching new data.
    fun refreshHomeScreen() {
        page = 1
        cryptocurrencyList.clear()
        addCryptocurrenciesByPage(page)
    }

    // This function is used to set the current page number for pagination.
    fun loadMore() {
        if (!isLoading.value) {
            page++
            addCryptocurrenciesByPage(page)
        }
    }

    // This function fetches the list of cryptocurrencies from the API and updates the cryptocurrencyList variable.
    private fun addCryptocurrenciesByPage(page: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading.value = true // Set loading state to true

            // Make a network request to fetch the list of cryptocurrencies
            repeat(15) {
                try {
                    val response = CryptocurrencyAPI.retrofitService.getCryptocurrencyListByPage(page)

                    if (response.isSuccessful) {
                        response.body()?.let {
                            cryptocurrencyList.addAll(it.filter { newItem -> newItem !in cryptocurrencyList })
                        }
                        return@launch
                    }
                } catch (e: Exception) {
                    println("Exception occurred: ${e.message}")
                }

                delay(5000) // Wait for 5 seconds before retrying
            }
        }.invokeOnCompletion { isLoading.value = false } // Set loading state to false when the loading is completed
    }
}