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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val cryptoRepository: CryptoRepository
) : ViewModel() {
    // This variable holds the list of cryptocurrencies that match the search query
    val cryptocurrencySearchList = mutableStateListOf<Cryptocurrency>()

    val query = mutableStateOf("") // This variable holds the current search query

    // This variable indicates whether the app is currently loading search results
    var isLoading = mutableStateOf(false)

    val isNotFound = mutableStateOf(false) // This variable indicates whether no results were found

    // This function fetches a list of cryptocurrencies based on the search query and returns the results.
    fun searchCryptocurrencies() = viewModelScope.launch {
        if (query.value.isEmpty()) return@launch // If the query is empty, do nothing

        isLoading.value = true // Set loading state to true

        repeat(5) {
            // Make a network request to search for cryptocurrencies
            val searchResource = withContext(Dispatchers.IO) {
                cryptoRepository.searchCrypto(query.value)
            }

            // Handle the result of the search request
            when (searchResource) {
                // If the search is successful, fetch the details of the cryptocurrencies by their IDs
                is Resource.Success -> {
                    // Extract the IDs of the cryptocurrencies from the search results
                    val ids = searchResource.data.joinToString(",") { it.id }

                    // Fetch the cryptocurrency details by their IDs
                    val listResource = withContext(Dispatchers.IO) {
                        cryptoRepository.getCryptoByIDs(ids)
                    }

                    // Handle the result of the fetch request
                    when (listResource) {
                        // If the fetch is successful, update the search list with the results
                        is Resource.Success -> {
                            cryptocurrencySearchList.clear()
                            cryptocurrencySearchList.addAll(listResource.data)
                            isLoading.value = false // Set loading state to false
                            return@launch // Exit the coroutine after successful fetch
                        }

                        // If there is an error fetching the details, log the error and retry after a delay
                        is Resource.Error -> {
                            Log.e("SearchViewModel", "Error: ${listResource.message}")
                            delay(2000) // Wait for 2 seconds before retrying
                        }
                    }
                }

                // If there is an error during the search, log the error and retry after a delay
                is Resource.Error -> {
                    Log.e("SearchViewModel", "Error: ${searchResource.message}")
                    delay(2000) // Wait for 2 seconds before retrying
                }
            }
        }

        isLoading.value = false // Set loading state to false when the search is completed

        isNotFound.value = cryptocurrencySearchList.isEmpty() // Update not found state
    }
}