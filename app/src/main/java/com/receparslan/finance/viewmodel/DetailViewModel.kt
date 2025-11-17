package com.receparslan.finance.viewmodel

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.receparslan.finance.database.CryptocurrencyDatabase
import com.receparslan.finance.model.Cryptocurrency
import com.receparslan.finance.model.KlineData
import com.receparslan.finance.service.CryptocurrencyAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.ceil

class DetailViewModel(application: Application) : AndroidViewModel(application) {
    lateinit var cryptocurrency: MutableState<Cryptocurrency>

    val savedCryptocurrencyIds =
        mutableStateOf<List<String>>(emptyList()) // List to hold the saved cryptocurrency IDs

    val klineDataHistoryList =
        mutableStateOf<List<KlineData>>(emptyList()) // List to hold the historical Kline data

    val selectedTimePeriod = mutableStateOf("24H") // Selected time period for Kline data

    var isLoading = mutableStateOf(false)

    init {
        observeSavedCryptocurrencies()
    }

    // This function is used to refresh the detail screen by clearing the existing Kline data history list and fetching new data.
    fun refreshDetailScreen() {
        updateCryptocurrency()
        klineDataHistoryList.value = emptyList()
        setCryptocurrencyHistory()
    }

    // This function saves a cryptocurrency to the database.
    fun saveCryptocurrency() {
        viewModelScope.launch(Dispatchers.IO) {
            val cryptocurrencyDao =
                CryptocurrencyDatabase.getDatabase(getApplication<Application>().applicationContext)
                    .cryptocurrencyDao()

            cryptocurrencyDao.insertCryptocurrency(cryptocurrency.value)
        }
    }

    // This function deletes a cryptocurrency from the database.
    fun deleteCryptocurrency() {
        viewModelScope.launch(Dispatchers.IO) {
            val cryptocurrencyDao =
                CryptocurrencyDatabase.getDatabase(getApplication<Application>().applicationContext)
                    .cryptocurrencyDao()

            cryptocurrencyDao.deleteCryptocurrency(cryptocurrency.value)
        }
    }

    // This function fetches the updated cryptocurrency data from the API and updates the saved cryptocurrency list.
    fun updateCryptocurrency() {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading.value = true

            try {
                val response = CryptocurrencyAPI.retrofitService
                    .getCryptocurrencyByIds(cryptocurrency.value.id)

                if (response.isSuccessful) {
                    response.body()?.let {
                        cryptocurrency.value = it.firstOrNull()
                            ?: cryptocurrency.value // Get the updated cryptocurrency data
                    }
                }
            } catch (e: Exception) {
                println("API exception: ${e.message}")
            }
        }.invokeOnCompletion {
            isLoading.value = false
        } // Set loading state to false when the loading is completed
    }

    // This function fetches the historical Kline data for a given cryptocurrency symbol and returns the data as a list.
    fun setCryptocurrencyHistory() {
        val symbol = cryptocurrency.value.symbol // Get the symbol of the cryptocurrency

        // Create a mutable list to hold the historical Kline data
        val klineDataHistoryListHolder = mutableStateListOf<KlineData>()

        // Calculate the start time based on the selected time period
        val startTime = when (selectedTimePeriod.value) {
            "24H" -> System.currentTimeMillis() - 24 * 60 * 60 * 1000L
            "1W" -> System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
            "1M" -> System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000L
            "6M" -> System.currentTimeMillis() - 6 * 30 * 24 * 60 * 60 * 1000L
            "1Y" -> System.currentTimeMillis() - 365 * 24 * 60 * 60 * 1000L
            "5Y" -> System.currentTimeMillis() - 5 * 365 * 24 * 60 * 60 * 1000L
            else -> System.currentTimeMillis() - 24 * 60 * 60 * 1000L
        }

        // Calculate the end time (current time) rounded to the nearest second in milliseconds
        val endTime = (System.currentTimeMillis() / 1000) * 1000

        // Determine the interval string based on the selected time period
        val interval = when (selectedTimePeriod.value) {
            "24H" -> "1m"
            "1W" -> "1h"
            else -> "1d"
        }

        // Determine the interval time in milliseconds based on the selected time period
        val intervalTimeMillis = when (selectedTimePeriod.value) {
            "24H" -> 1 * 60 * 1000L            // 1 minute
            "1W" -> 60 * 60 * 1000L            // 1 hour
            else -> 24 * 60 * 60 * 1000L       // 1 day
        }

        // Calculate the repeat number based on the interval time
        val repeatNumber = ceil((endTime - startTime) / intervalTimeMillis / 1000F).toInt()

        viewModelScope.launch(Dispatchers.IO) {
            isLoading.value = true // Set loading state to true

            klineDataHistoryList.value = emptyList() // Clear the existing Kline data history list

            repeat(repeatNumber) { attempt ->
                try {
                    val response = CryptocurrencyAPI.binanceService.getHistoricalDataByRange(
                        symbol = if (symbol.uppercase() == "USDT") "BTCUSDT" else symbol.uppercase() + "USDT",
                        startTime = if (attempt == repeatNumber - 1) startTime - intervalTimeMillis else endTime - (attempt + 1) * intervalTimeMillis * 1000L,
                        endTime = endTime - attempt * intervalTimeMillis * 1000L,
                        interval = interval
                    )
                    if (response.isSuccessful) {
                        // Handle the successful response
                        response.body()?.let {
                            val list = it.map { array ->
                                val array = array as List<*>

                                val data = KlineData(
                                    openTime = (array[0] as Double).toLong(),
                                    open = if (symbol.uppercase() != "USDT") array[1] as String else "1.0",
                                    high = if (symbol.uppercase() != "USDT") array[2] as String else "1.0",
                                    low = if (symbol.uppercase() != "USDT") array[3] as String else "1.0",
                                    close = if (symbol.uppercase() != "USDT") array[4] as String else "1.0",
                                    closeTime = (array[6] as Double).toLong()
                                )

                                data
                            }

                            klineDataHistoryListHolder.addAll(list.filterNot { klineData -> klineData in klineDataHistoryListHolder }) // Set the historical Kline data
                        }
                    } else
                        println("Response unsuccessful: $response")
                } catch (e: Exception) {
                    println("Exception occurred: ${e.message}")
                }
            }
        }.invokeOnCompletion {
            isLoading.value = false // Set loading state to false when the loading is completed
            klineDataHistoryList.value =
                klineDataHistoryListHolder // Update the Kline data history list
        }
    }

    // This function sets the saved cryptocurrencies by fetching them from the database.
    private fun observeSavedCryptocurrencies() {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading.value = true

            val cryptocurrencyDao =
                CryptocurrencyDatabase.getDatabase(getApplication<Application>().applicationContext)
                    .cryptocurrencyDao()

            cryptocurrencyDao.getAllCryptocurrencies().collect { localList ->
                savedCryptocurrencyIds.value = localList.map { it.id }
            }
        }
    }
}