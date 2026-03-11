package com.receparslan.finance.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.receparslan.finance.repository.CryptoRepository
import com.receparslan.finance.util.Resource
import com.receparslan.finance.util.States.HomeUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val cryptoRepository: CryptoRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUIState())
    val uiState = _uiState.asStateFlow()

    init {
        _uiState.update {
            it.copy(
                isLoading = true,
                hasMore = true,
            )
        }

        initCryptocurrenciesByPage(uiState.value.page)
    }

    // This function is used to clear the error message from the UI state, allowing the UI to hide any error dialogs or messages.
    fun clearErrorMessage() {
        _uiState.update { currentState ->
            currentState.copy(
                errorMessage = ""
            )
        }
    }

    // This function is used to refresh the home screen by clearing the existing list and fetching new data.
    fun refreshHomeScreen() {
        _uiState.update { currentState ->
            currentState.copy(
                isRefreshing = true,
                isLoadingMore = false,
                hasMore = true,
                errorMessage = "",
                cryptoList = emptyList(),
                page = 1
            )
        }

        initCryptocurrenciesByPage(uiState.value.page)
    }

    // This function is used to load more cryptocurrencies when the user scrolls to the bottom of the list.
    fun loadMore() {
        val currentState = _uiState.value

        if (currentState.isLoading || currentState.isLoadingMore || !currentState.hasMore) return

        _uiState.update { state ->
            state.copy(
                isLoadingMore = true,
                page = state.page + 1
            )
        }

        initCryptocurrenciesByPage(uiState.value.page)
    }

    // This function is responsible for fetching the list of cryptocurrencies based on the page number and updating the UI state accordingly.
    private fun initCryptocurrenciesByPage(page: Int) = viewModelScope.launch {
        try {
            when (val resource = cryptoRepository.getCryptoListByPage(page)) {
                is Resource.Success ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            isRefreshing = false,
                            hasMore = currentState.page < 73,
                            cryptoList = currentState.cryptoList + resource.data
                        )
                    }

                is Resource.Error ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            isRefreshing = false,
                            page = currentState.page - 1,
                            hasMore = currentState.page < 73,
                            errorMessage = resource.message
                        )
                    }
            }
        } catch (e: Exception) {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    isRefreshing = false,
                    page = currentState.page - 1,
                    hasMore = currentState.page < 73,
                    errorMessage = e.localizedMessage ?: "An unexpected error occurred!"
                )
            }
        }
    }
}