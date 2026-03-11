package com.receparslan.finance.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.receparslan.finance.repository.CryptoRepository
import com.receparslan.finance.util.Resource
import com.receparslan.finance.util.States.FavouriteUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavouritesViewModel
@Inject constructor(
    private val cryptoRepository: CryptoRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(FavouriteUIState())
    val uiState = _uiState.asStateFlow()

    // This state is used to manage the UI state of the favourites screen when observing the saved cryptocurrencies.
    init {
        _uiState.update { currentState ->
            currentState.copy(
                isLoading = true,
            )
        }

        viewModelScope.launch {
            when (val resource = cryptoRepository.observeSavedCryptocurrencies().first()) {
                is Resource.Success ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            errorMessage = "",
                            savedCryptocurrencies = resource.data
                        )
                    }

                is Resource.Error ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            errorMessage = resource.message
                        )
                    }
            }
        }

        observeSavedCryptocurrencies()
    }

    // This function is used to refresh the favourites screen by re-observing the saved cryptocurrencies.
    fun refreshFavouritesScreen() = observeSavedCryptocurrencies()

    // This function is used to clear the error message from the UI state when the user dismisses the error dialog.
    fun clearErrorMessage() {
        _uiState.update { currentState ->
            currentState.copy(
                errorMessage = ""
            )
        }
    }

    // This function is used to refresh the favourites screen by re-observing the saved cryptocurrencies.
    private fun observeSavedCryptocurrencies() = viewModelScope.launch {
        cryptoRepository.observeSavedCryptocurrencies()
            .collect { resource ->
                when (resource) {
                    is Resource.Success ->
                        _uiState.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                errorMessage = "",
                                savedCryptocurrencies = resource.data
                            )
                        }

                    is Resource.Error ->
                        _uiState.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                errorMessage = resource.message
                            )
                        }
                }
            }
    }
}