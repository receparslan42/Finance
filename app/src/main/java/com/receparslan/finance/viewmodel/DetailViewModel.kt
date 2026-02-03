package com.receparslan.finance.viewmodel

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.receparslan.finance.model.Cryptocurrency
import com.receparslan.finance.model.KlineData
import com.receparslan.finance.repository.CryptoRepository
import com.receparslan.finance.util.Constants.TimeMillis
import com.receparslan.finance.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import javax.inject.Inject
import kotlin.math.ceil

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val cryptoRepository: CryptoRepository
) : ViewModel() {
    // This variable holds the selected cryptocurrency details
    var cryptocurrency: MutableState<Cryptocurrency> = mutableStateOf(
        Cryptocurrency(
            id = "",
            symbol = "",
            name = "",
            image = "",
            currentPrice = 0.0,
            lastUpdated = "",
            priceChangePercentage24h = 0.0
        )
    )

    // This variable holds the list of saved cryptocurrency IDs from the database
    val savedCryptocurrencyIds = mutableStateListOf<String>()

    // This variable holds the historical Kline data for the selected cryptocurrency
    val klineDataHistoryList = mutableStateListOf<KlineData>()

    val selectedTimePeriod = mutableStateOf("24H") // Selected time period for Kline data

    var isLoading = mutableStateOf(false) // Loading state for data fetching

    // Initialize the ViewModel by observing saved cryptocurrencies
    init {
        observeSavedCryptocurrencies()
    }

    // This function is used to refresh the detail screen by clearing the existing Kline data history list and fetching new data.
    fun refreshDetailScreen() {
        updateCryptocurrency() // Update the cryptocurrency details
        setCryptocurrencyHistory() // Fetch the historical Kline data
    }

    // This function formats a decimal number according to the specified pattern.
    fun decimalFormatter(pattern: String, number: Double): String {
        return DecimalFormat(pattern, DecimalFormatSymbols(Locale.US))
            .format(number)
    }

    // This function saves a cryptocurrency to the database.
    fun saveCryptocurrency() = viewModelScope.launch {
        // Save the cryptocurrency to the database on a background thread
        val resource = withContext(Dispatchers.IO) {
            cryptoRepository.saveCryptoToDb(cryptocurrency.value)
        }

        // Log an error message if the save operation fails
        if (resource is Resource.Error)
            Log.e("DetailViewModel", resource.message)
    }

    // This function deletes a cryptocurrency from the database.
    fun deleteCryptocurrency() = viewModelScope.launch {
        // Delete the cryptocurrency from the database on a background thread
        val resource = withContext(Dispatchers.IO) {
            cryptoRepository.deleteCryptoFromDb(cryptocurrency.value)
        }

        // Log an error message if the deletion fails
        if (resource is Resource.Error)
            Log.e("DetailViewModel", resource.message)
    }

    // This function fetches the updated cryptocurrency data from the API and updates the saved cryptocurrency list.
    fun updateCryptocurrency() = viewModelScope.launch {
        isLoading.value = true // Set loading state to true

        // Fetch the updated cryptocurrency data from the repository
        val resource = withContext(Dispatchers.IO) {
            cryptoRepository.getCryptoByIDs(cryptocurrency.value.id)
        }

        // Update the cryptocurrency value based on the API response or keep the existing value in case of an error
        cryptocurrency.value =
            when (resource) {
                is Resource.Success -> resource.data[0] // Update with the new data if the API call is successful

                // Log the error message and keep the existing data in case of an error
                is Resource.Error -> {
                    Log.e("DetailViewModel", resource.message)

                    cryptocurrency.value
                }
            }

        isLoading.value = false // Set loading state to false
    }

    // This function fetches the historical Kline data for a given cryptocurrency symbol and returns the data as a list.
    fun setCryptocurrencyHistory() {
        // Temporary holder for Kline data history
        val klineDataHistoryListHolder = mutableStateListOf<KlineData>()

        // Calculate the start time based on the selected time period
        val startTime = when (selectedTimePeriod.value) {
            "24H" -> System.currentTimeMillis() - TimeMillis.MILLIS_PER_DAY
            "1W" -> System.currentTimeMillis() - TimeMillis.MILLIS_PER_WEEK
            "1M" -> System.currentTimeMillis() - TimeMillis.MILLIS_PER_MONTH
            "6M" -> System.currentTimeMillis() - TimeMillis.MILLIS_PER_6MONTH
            "1Y" -> System.currentTimeMillis() - TimeMillis.MILLIS_PER_YEAR
            "5Y" -> System.currentTimeMillis() - TimeMillis.MILLIS_PER_5YEAR
            else -> System.currentTimeMillis() - TimeMillis.MILLIS_PER_DAY
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
            "24H" -> TimeMillis.MILLIS_PER_MINUTE
            "1W" -> TimeMillis.MILLIS_PER_HOUR
            else -> TimeMillis.MILLIS_PER_DAY
        }

        val maxPointsPerRequest = 1000L // Binance API limit for Kline data points per request

        // Calculate the number of chunks needed to fetch all data within API limits
        val chunks =
            ceil(((endTime - startTime).toDouble() / intervalTimeMillis) / maxPointsPerRequest).toInt()

        viewModelScope.launch {
            repeat(chunks) { chunkIndex ->
                // Calculate the start and end time for the current chunk
                val chunkStart = startTime + (chunkIndex * maxPointsPerRequest * intervalTimeMillis)
                val chunkEnd =
                    minOf(endTime, chunkStart + (maxPointsPerRequest * intervalTimeMillis) - 1)

                // Fetch the Kline data for the current chunk from the repository
                val resource: Resource<List<KlineData>> = withContext(Dispatchers.IO) {
                    cryptoRepository.getHistoricalDataByRange(
                        symbol = cryptocurrency.value.symbol,
                        startTime = chunkStart,
                        endTime = chunkEnd,
                        interval = interval,
                    )
                }

                when (resource) {
                    // Update the Kline data history list on successful data retrieval
                    is Resource.Success -> klineDataHistoryListHolder.addAll(resource.data)

                    // Log the error message if fetching fails
                    is Resource.Error -> Log.e("DetailViewModel", resource.message)
                }
            }
        }.invokeOnCompletion {
            klineDataHistoryList.clear()
            klineDataHistoryList.addAll(klineDataHistoryListHolder)
        }
    }

    // This function observes the saved cryptocurrencies from the database and updates the savedCryptocurrencyIds list
    private fun observeSavedCryptocurrencies() =
        viewModelScope.launch(Dispatchers.IO) {
            cryptoRepository.getAllSavedCryptoIDsFlow().collect { resource ->
                when (resource) {
                    // Update the savedCryptocurrencyIds list on successful data retrieval
                    is Resource.Success -> withContext(Dispatchers.Main) {
                        savedCryptocurrencyIds.clear()
                        savedCryptocurrencyIds.addAll(resource.data)
                    }

                    // Log the error message if fetching fails
                    is Resource.Error -> Log.e(
                        "Error",
                        resource.message
                    )
                }
            }
        }
}