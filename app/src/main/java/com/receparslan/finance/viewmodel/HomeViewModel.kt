package com.receparslan.finance.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.receparslan.finance.model.Cryptocurrency
import com.receparslan.finance.repository.CryptoRepository
import com.receparslan.finance.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val cryptoRepository: CryptoRepository
) : ViewModel() {
    // This variable holds the list of cryptocurrencies that will be displayed on the home screen.
    val cryptocurrencyList = mutableStateListOf<Cryptocurrency>()

    // This variable is used to track the loading state of the cryptocurrency list
    var isLoading = mutableStateOf(false)

    @Inject
    lateinit var gson: Gson // Gson instance for JSON serialization/deserialization

    // This variable is used to keep track of the current page number for pagination
    private var page = 1

    // The init block is called when the ViewModel is created.
    init {
        initCryptocurrenciesByPage(page) // Fetch the initial list of cryptocurrencies for page 1
    }

    // This function encodes a Cryptocurrency object to a JSON string using Gson
    fun encodeToString(cryptocurrency: Cryptocurrency): String {
        return URLEncoder.encode(gson.toJson(cryptocurrency), "utf-8")
    }

    // This function is used to refresh the home screen by clearing the existing list and fetching new data.
    fun refreshHomeScreen() {
        page = 1
        cryptocurrencyList.clear()
        initCryptocurrenciesByPage(page)
    }

    // This function is used to set the current page number for pagination.
    fun loadMore() {
        if (!isLoading.value) {
            page++
            initCryptocurrenciesByPage(page)
        }
    }

    // This function fetches the list of cryptocurrencies from the API and updates the cryptocurrencyList variable.
    private fun initCryptocurrenciesByPage(page: Int) = viewModelScope.launch {
        isLoading.value = true // Set loading state to true

        // Retry mechanism: Try up to 15 times with a 2-second delay between attempts
        repeat(15) {
            // Make a network request to fetch the list of cryptocurrencies
            val resource = withContext(Dispatchers.IO) {
                cryptoRepository.getCryptoListByPage(page)
            }

            // Handle the response based on its type (Success or Error)
            when (resource) {
                // If the response is successful, add the cryptocurrencies to the list and exit the loop
                is Resource.Success -> {
                    cryptocurrencyList.addAll(resource.data)
                    isLoading.value = false // Set loading state to false
                    return@launch // Exit the repeat loop on success
                }

                // If there is an error, log the error message and wait for 2 seconds before retrying
                is Resource.Error -> {
                    Log.e("HomeViewModel", "Error fetching data: ${resource.message}")
                    delay(3000)
                }
            }
        }

        isLoading.value = false // Set loading state to false when the loading is completed
    }
}