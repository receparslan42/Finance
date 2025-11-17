package com.receparslan.finance.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.receparslan.finance.database.CryptocurrencyDatabase
import com.receparslan.finance.model.Cryptocurrency
import com.receparslan.finance.service.CryptocurrencyAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FavouritesViewModel(application: Application) : AndroidViewModel(application) {
    val savedCryptocurrencyList = mutableStateOf<List<Cryptocurrency>>(emptyList())

    var isLoading = mutableStateOf(false) // Loading state to show/hide loading indicators

    // Set the saved cryptocurrencies when the ViewModel is initialized
    init {
        observeSavedCryptocurrencies()
    }

    // This function sets the saved cryptocurrencies by fetching them from the database.
    private fun observeSavedCryptocurrencies() {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading.value = true

            val cryptocurrencyDao = CryptocurrencyDatabase.getDatabase(getApplication<Application>().applicationContext).cryptocurrencyDao()

            cryptocurrencyDao.getAllCryptocurrencies().collect { localList ->
                val ids = localList.map { it.id }

                if (ids.isEmpty())
                    savedCryptocurrencyList.value = emptyList()
                else
                    try {
                        val response = CryptocurrencyAPI.retrofitService
                            .getCryptocurrencyByIds(ids.joinToString(","))

                        if (response.isSuccessful) {
                            response.body()?.let {
                                savedCryptocurrencyList.value = it
                            }
                        } else {
                            println("API response unsuccessful: ${response.code()}")
                        }
                    } catch (e: Exception) {
                        println("API exception: ${e.message}")
                    } finally {
                        isLoading.value = false // Set loading state to false when the loading is completed
                    }
            }
        }
    }
}