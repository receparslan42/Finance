package com.receparslan.finance.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.receparslan.finance.model.Cryptocurrency
import com.receparslan.finance.service.CryptocurrencyAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {
    // This variable holds the list of cryptocurrencies that match the search query.
    val cryptocurrencySearchList = mutableStateOf<List<Cryptocurrency>>(emptyList())

    var isLoading = mutableStateOf(false) // This variable is used to track the loading state of the search results.

    // This function fetches a list of cryptocurrencies based on the search query and returns the results.
    fun searchCryptocurrencies(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading.value = true // Set loading state to true

            // Make a network request to fetch the list of cryptocurrencies
            repeat(15) {
                try {
                    val response = CryptocurrencyAPI.retrofitService.searchCryptocurrency(query)

                    if (response.isSuccessful) {
                        // If the response is successful, add the cryptocurrencies to the search list
                        response.body()?.let { cryptocurrencyList ->
                            val ids = cryptocurrencyList.cryptocurrencies.joinToString(",") { it.id }// Get the list of cryptocurrency IDs

                            try {
                                val response = CryptocurrencyAPI.retrofitService.getCryptocurrencyByIds(ids)

                                // If the response is successful, add the cryptocurrencies to the search list
                                if (response.isSuccessful) {
                                    response.body()?.let {
                                        cryptocurrencySearchList.value = it
                                    }
                                    return@launch
                                } else {
                                    println("Response unsuccessful: $response")
                                    delay(5000) // Wait for 5 second before retrying
                                }
                            } catch (e: Exception) {
                                println("Exception occurred: ${e.message}")
                            }
                        }
                    } else {
                        println("Response unsuccessful: $response")
                        delay(5000) // Wait for 5 seconds before retrying
                    }
                } catch (e: Exception) {
                    println("Exception occurred: ${e.message}")
                }
            }
        }.invokeOnCompletion { isLoading.value = false } // Set loading state to false when the search is completed
    }
}