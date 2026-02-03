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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavouritesViewModel
@Inject constructor(
    private val cryptoRepository: CryptoRepository
) : ViewModel() {

    // This variable holds the list of saved cryptocurrencies
    val savedCryptocurrencyList = mutableStateListOf<Cryptocurrency>()

    var isLoading = mutableStateOf(false) // Loading state to show/hide loading indicators

    // Set the saved cryptocurrencies when the ViewModel is initialized
    init {
        observeSavedCryptocurrencies()
    }

    // This function observes the saved cryptocurrencies from the repository and updates the savedCryptocurrencyList
    private fun observeSavedCryptocurrencies() = viewModelScope.launch(Dispatchers.IO) {
        cryptoRepository.getAllSavedCryptoIDsFlow().collect { idsResource ->
            when (idsResource) {
                is Resource.Success -> {
                    // If there are saved IDs, fetch the corresponding cryptocurrency details
                    if (idsResource.data.isNotEmpty()) {
                        // Join the list of IDs into a comma-separated string for the API request
                        val ids = idsResource.data.joinToString(",")

                        when (val savedCryptosResource = cryptoRepository.getCryptoByIDs(ids)) {
                            is Resource.Success -> {
                                savedCryptocurrencyList.clear()
                                savedCryptocurrencyList.addAll(savedCryptosResource.data)
                            }

                            is Resource.Error -> Log.e(
                                "FavouritesViewModel",
                                savedCryptosResource.message
                            )
                        }
                    } else {
                        // If there are no saved IDs, clear the list
                        savedCryptocurrencyList.clear()
                    }
                }

                is Resource.Error -> Log.e("FavouritesViewModel", idsResource.message)
            }
        }
    }
}