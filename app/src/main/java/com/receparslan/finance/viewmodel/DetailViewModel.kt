package com.receparslan.finance.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.receparslan.finance.model.Cryptocurrency
import com.receparslan.finance.model.KlineData
import com.receparslan.finance.repository.CryptoRepository
import com.receparslan.finance.util.Constants.TimeMillis
import com.receparslan.finance.util.Resource
import com.receparslan.finance.util.States.DetailUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import javax.inject.Inject
import kotlin.math.ceil

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val cryptoRepository: CryptoRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _uiState = MutableStateFlow(DetailUIState())
    val uiState = _uiState.asStateFlow()

    val cryptoId: String = checkNotNull(
        Uri.decode(savedStateHandle["cryptoId"])
    )

    init {
        _uiState.update { currentState ->
            currentState.copy(
                isLoading = true,
            )
        }

        observeSavedCryptocurrencies()
        initCryptocurrency()
    }

    fun updateTimePeriod(timePeriod: String) = viewModelScope.launch {
        val currentState = _uiState.value

        if (currentState.isLoading)
            return@launch

        if (currentState.cryptocurrency == null)
            return@launch

        _uiState.update { state ->
            state.copy(
                timePeriod = timePeriod,
                klineDataHistory = emptyList()
            )
        }

        val historyData: MutableList<KlineData> = getCryptocurrencyHistory(
            symbol = currentState.cryptocurrency.symbol,
            timePeriod = timePeriod
        )

        _uiState.update { state ->
            state.copy(
                klineDataHistory = historyData,
                isRefreshing = false
            )
        }
    }

    // This function clears the error message from the UI state.
    fun clearErrorMessage() = _uiState.update { currentState ->
        currentState.copy(
            errorMessage = ""
        )
    }

    // This function refreshes the detail screen by re-fetching the cryptocurrency data and its historical Kline data based on the current time period.
    fun refreshDetailScreen() {
        val currentState = _uiState.value

        if (currentState.cryptocurrency == null)
            return

        _uiState.update { currentState ->
            currentState.copy(
                isRefreshing = true,
                errorMessage = "",
                cryptocurrency = null,
                klineDataHistory = emptyList()
            )
        }

        initCryptocurrency()
    }


    // This function formats a decimal number according to the specified pattern.
    fun decimalFormatter(pattern: String, number: Double): String =
        DecimalFormat(
            pattern, DecimalFormatSymbols(Locale.US)
        ).format(number)

    // This function saves a cryptocurrency to the database.
    fun saveCryptocurrency() = viewModelScope.launch {
        val currentState = _uiState.value

        if (currentState.cryptocurrency == null)
            return@launch

        when (val resource = cryptoRepository.saveCryptoToDb(currentState.cryptocurrency)) {
            is Resource.Success ->
                _uiState.update { state ->
                    state.copy(
                        isSaved = true,
                        errorMessage = ""
                    )
                }

            is Resource.Error ->
                _uiState.update { state ->
                    state.copy(
                        errorMessage = resource.message,
                        isSaved = false
                    )
                }
        }
    }

    // This function deletes a cryptocurrency from the database.
    fun deleteCryptocurrency() = viewModelScope.launch {
        val currentState = _uiState.value

        if (currentState.cryptocurrency == null)
            return@launch

        when (val resource = cryptoRepository.deleteCryptoFromDb(currentState.cryptocurrency)) {
            is Resource.Success ->
                _uiState.update { state ->
                    state.copy(
                        isSaved = false,
                        errorMessage = ""
                    )
                }

            is Resource.Error ->
                _uiState.update { state ->
                    state.copy(
                        errorMessage = resource.message,
                        isSaved = true
                    )
                }
        }
    }

    // This function initializes the cryptocurrency data and its historical Kline data based on the selected time period.
    fun initCryptocurrency() = viewModelScope.launch {
        val savedIds = when (val resource = cryptoRepository.getAllSavedCryptoIDsFlow().first()) {
            is Resource.Success -> resource.data
            else -> emptyList()
        }

        val resource = cryptoRepository.getCryptoByIDs(cryptoId)

        val cryptoData: Cryptocurrency = when (resource) {
            is Resource.Success -> resource.data.firstOrNull() ?: run {
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = "Cryptocurrency not found."
                    )
                }

                return@launch
            }

            is Resource.Error -> {
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = resource.message
                    )
                }
                return@launch
            }
        }

        val historyData: MutableList<KlineData> = getCryptocurrencyHistory(
            symbol = cryptoData.symbol,
            timePeriod = uiState.value.timePeriod
        )

        _uiState.update { currentState ->
            currentState.copy(
                isLoading = false,
                isRefreshing = false,
                cryptocurrency = cryptoData,
                klineDataHistory = historyData,
                isSaved = savedIds.contains(cryptoData.id)
            )
        }
    }

    private suspend fun getCryptocurrencyHistory(
        symbol: String,
        timePeriod: String
    ): MutableList<KlineData> {
        val klineDataHistoryListHolder = mutableListOf<KlineData>()

        val startTime = when (timePeriod) {
            "24H" -> System.currentTimeMillis() - TimeMillis.MILLIS_PER_DAY
            "1W" -> System.currentTimeMillis() - TimeMillis.MILLIS_PER_WEEK
            "1M" -> System.currentTimeMillis() - TimeMillis.MILLIS_PER_MONTH
            "6M" -> System.currentTimeMillis() - TimeMillis.MILLIS_PER_6MONTH
            "1Y" -> System.currentTimeMillis() - TimeMillis.MILLIS_PER_YEAR
            "5Y" -> System.currentTimeMillis() - TimeMillis.MILLIS_PER_5YEAR
            else -> System.currentTimeMillis() - TimeMillis.MILLIS_PER_DAY
        }

        val endTime = (System.currentTimeMillis() / 1000) * 1000

        val interval = when (timePeriod) {
            "24H" -> "1m"
            "1W" -> "1h"
            else -> "1d"
        }

        val intervalTimeMillis = when (timePeriod) {
            "24H" -> TimeMillis.MILLIS_PER_MINUTE
            "1W" -> TimeMillis.MILLIS_PER_HOUR
            else -> TimeMillis.MILLIS_PER_DAY
        }

        val maxPointsPerRequest = 1000L // Binance API limit for Kline data points per request

        val chunks =
            ceil(((endTime - startTime).toDouble() / intervalTimeMillis) / maxPointsPerRequest)
                .toInt()

        repeat(chunks) { chunkIndex ->
            val chunkStart = startTime + (chunkIndex * maxPointsPerRequest * intervalTimeMillis)
            val chunkEnd =
                minOf(endTime, chunkStart + (maxPointsPerRequest * intervalTimeMillis) - 1)

            val resource: Resource<List<KlineData>> =
                cryptoRepository.getHistoricalDataByRange(
                    symbol = symbol,
                    startTime = chunkStart,
                    endTime = chunkEnd,
                    interval = interval,
                )

            when (resource) {
                is Resource.Success -> klineDataHistoryListHolder.addAll(resource.data)

                is Resource.Error -> {
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = resource.message
                        )
                    }

                    return@repeat
                }
            }
        }

        return klineDataHistoryListHolder
    }

    // This function observes the saved cryptocurrency IDs from the database and updates the UI state accordingly.
    private fun observeSavedCryptocurrencies() =
        viewModelScope.launch {
            cryptoRepository.getAllSavedCryptoIDsFlow().collect { resource ->
                when (resource) {
                    is Resource.Success -> resource.data.let {
                        val isSaved = it.contains(uiState.value.cryptocurrency?.id)

                        _uiState.update { currentState ->
                            currentState.copy(
                                isSaved = isSaved
                            )
                        }
                    }

                    is Resource.Error -> Log.e(
                        "DetailViewModel",
                        "Error observing saved cryptocurrencies: ${resource.message}"
                    )
                }
            }
        }
}