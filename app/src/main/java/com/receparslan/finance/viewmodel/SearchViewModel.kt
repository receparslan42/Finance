package com.receparslan.finance.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.receparslan.finance.repository.CryptoRepository
import com.receparslan.finance.util.Resource
import com.receparslan.finance.util.States.SearchUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val cryptoRepository: CryptoRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUIState())
    val uiState = _uiState.asStateFlow()

    // This function clears the error message from the UI state when the user dismisses the error dialog.
    fun clearErrorMessage() {
        _uiState.update { currentState ->
            currentState.copy(
                errorMessage = ""
            )
        }
    }

    // This function updates the search query when the user types in the search bar.
    fun updateQuery(newQuery: String) {
        _uiState.update { currentState ->
            currentState.copy(
                query = newQuery
            )
        }
    }

    // This function fetches a list of cryptocurrencies based on the search query and returns the results.
    fun searchCryptocurrencies() = viewModelScope.launch {
        val currentQuery = uiState.value.query

        if (currentQuery.isEmpty()) return@launch

        _uiState.update { currentState ->
            currentState.copy(
                isLoading = true,
                errorMessage = "",
                searchResults = emptyList(),
                isNotFound = false
            )
        }

        when (val searchResource = cryptoRepository.searchCrypto(currentQuery)) {
            is Resource.Success -> {
                val ids = searchResource.data.joinToString(",") { it.id }

                if (ids.isEmpty()) {
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            searchResults = emptyList(),
                            isNotFound = true
                        )
                    }
                    return@launch
                }

                when (val listResource = cryptoRepository.getCryptoByIDs(ids)) {
                    is Resource.Success ->
                        _uiState.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                searchResults = listResource.data,
                            )
                        }

                    is Resource.Error ->
                        _uiState.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                errorMessage = listResource.message
                            )
                        }
                }
            }

            is Resource.Error ->
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        errorMessage = searchResource.message
                    )
                }
        }
    }
}