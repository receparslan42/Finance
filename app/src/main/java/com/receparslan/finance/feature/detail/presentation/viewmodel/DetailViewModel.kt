package com.receparslan.finance.feature.detail.presentation.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.receparslan.finance.core.common.Resource
import com.receparslan.finance.core.domain.model.Cryptocurrency
import com.receparslan.finance.feature.detail.domain.usecase.*
import com.receparslan.finance.feature.detail.presentation.state.DetailUIState
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

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val getCryptoByIdUseCase: GetCryptoByIdUseCase,
    private val getHistoricalDataUseCase: GetHistoricalDataUseCase,
    private val saveFavoriteUseCase: SaveFavoriteUseCase,
    private val deleteFavoriteUseCase: DeleteFavoriteUseCase,
    private val getSavedCryptoIdsUseCase: GetSavedCryptoIdsUseCase,
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

        when (val historyResource = getHistoricalDataUseCase(
            symbol = currentState.cryptocurrency.symbol,
            timePeriod = timePeriod
        )) {
            is Resource.Success -> {
                _uiState.update { state ->
                    state.copy(
                        klineDataHistory = historyResource.data,
                        isRefreshing = false
                    )
                }
            }
            is Resource.Error -> {
                _uiState.update { state ->
                    state.copy(
                        errorMessage = historyResource.message,
                        isRefreshing = false
                    )
                }
            }
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

        when (val resource = saveFavoriteUseCase(currentState.cryptocurrency)) {
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

        when (val resource = deleteFavoriteUseCase(currentState.cryptocurrency)) {
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
        val savedIds = when (val resource = getSavedCryptoIdsUseCase().first()) {
            is Resource.Success -> resource.data
            else -> emptyList()
        }

        val cryptoData: Cryptocurrency = when (val resource = getCryptoByIdUseCase(cryptoId)) {
            is Resource.Success -> resource.data

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

        when (val historyResource = getHistoricalDataUseCase(
            symbol = cryptoData.symbol,
            timePeriod = uiState.value.timePeriod
        )) {
            is Resource.Success -> {
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        isRefreshing = false,
                        cryptocurrency = cryptoData,
                        klineDataHistory = historyResource.data,
                        isSaved = savedIds.contains(cryptoData.id)
                    )
                }
            }
            is Resource.Error -> {
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        isRefreshing = false,
                        cryptocurrency = cryptoData,
                        errorMessage = historyResource.message,
                        isSaved = savedIds.contains(cryptoData.id)
                    )
                }
            }
        }
    }

    // This function observes the saved cryptocurrency IDs from the database and updates the UI state accordingly.
    private fun observeSavedCryptocurrencies() =
        viewModelScope.launch {
            getSavedCryptoIdsUseCase().collect { resource ->
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