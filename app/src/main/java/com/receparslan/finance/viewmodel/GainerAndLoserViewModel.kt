package com.receparslan.finance.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.receparslan.finance.repository.CryptoRepository
import com.receparslan.finance.util.Resource
import com.receparslan.finance.util.States.GainerAndLoserUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GainerAndLoserViewModel @Inject constructor(
    private val cryptoRepository: CryptoRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(GainerAndLoserUIState())
    val uiState = _uiState.asStateFlow()

    // This block is executed when the ViewModel is created. It sets the loading state to true and initiates the fetching of gainers and losers list.
    init {
        _uiState.update { currentState ->
            currentState.copy(
                isLoading = true
            )
        }

        initGainersAndLosersList()
    }

    // This function is used to refresh the gainers and losers list by clearing the existing data and fetching new data.
    fun refreshGainersAndLosers() {
        _uiState.update { currentState ->
            currentState.copy(
                isRefreshing = true,
                errorMessage = "",
                gainers = emptyList(),
                losers = emptyList()
            )
        }

        initGainersAndLosersList()
    }

    // This function is used to clear the error message from the UI state.
    fun clearErrorMessage() {
        _uiState.update { currentState ->
            currentState.copy(
                errorMessage = ""
            )
        }
    }

    // This function fetches the gainers and losers list from the repository and updates the UI state accordingly
    private fun initGainersAndLosersList() = viewModelScope.launch {
        when (val resource = cryptoRepository.getGainersAndLosers()) {
            is Resource.Success ->
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        isRefreshing = false,
                        gainers = resource.data.gainers,
                        losers = resource.data.losers,
                        errorMessage = ""
                    )

                }

            is Resource.Error ->
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        isRefreshing = false,
                        gainers = emptyList(),
                        losers = emptyList(),
                        errorMessage = resource.message
                    )
                }
        }
    }
}