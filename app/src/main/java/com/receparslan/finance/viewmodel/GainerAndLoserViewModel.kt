package com.receparslan.finance.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.receparslan.finance.model.Cryptocurrency
import com.receparslan.finance.service.CryptocurrencyAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.time.Instant
import java.time.format.DateTimeFormatter

class GainerAndLoserViewModel : ViewModel() {
    val cryptocurrencyGainerList = mutableStateOf<List<Cryptocurrency>>(emptyList())
    val cryptocurrencyLoserList = mutableStateOf<List<Cryptocurrency>>(emptyList())

    var isLoading = mutableStateOf(false) // Loading state to show/hide loading indicators

    init {
        setGainersAndLosersList()
    }

    // This function fetches the list of gainers and losers from the CoinGecko website using Jsoup.
    fun setGainersAndLosersList() {
        val url = "https://www.coingecko.com/en/crypto-gainers-losers" // URL for gainers and losers

        val client = OkHttpClient() // Create an instance of OkHttpClient

        // Create a request to fetch the HTML content from the URL
        val request = Request.Builder()
            .url(url)
            .build()

        // Make a network request to fetch the HTML content
        viewModelScope.launch(Dispatchers.IO) {
            isLoading.value = true // Set loading state to true

            try {
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val html: String =
                        response.body.string() // Get the HTML response as a string
                    val document = Jsoup.parse(html) // Parse the HTML response using Jsoup
                    val headers =
                        document.select("tbody") // Select the table body from the HTML document

                    val gainerList = mutableStateListOf<Cryptocurrency>()
                    val loserList = mutableStateListOf<Cryptocurrency>()

                    headers.forEachIndexed { index, item ->
                        item.select("tr").forEach {
                            val symbol = it.select("a").select("div > div > div")
                                .text() // Get the symbol of the cryptocurrency
                            val name =
                                it.select("a").select("div > div").text()
                                    .substringBeforeLast("$symbol $symbol")
                                    .trim() // Get the name of the cryptocurrency
                            val image = it.select("img").attr("src")
                                .substringBefore("?") // Get the image URL of the cryptocurrency
                            val price = it.select("td")[3].text().substringAfter("$")
                                .toDouble() // Get the price of the cryptocurrency
                            val change = it.select("td")[5].text().substringBefore("%")
                                .toDouble() // Get the change percentage of the cryptocurrency

                            val cryptocurrency = Cryptocurrency(
                                id = "",
                                name = name,
                                symbol = symbol,
                                image = image,
                                currentPrice = price,
                                priceChangePercentage24h = change,
                                lastUpdated = ""
                            )

                            // Add the cryptocurrency to the appropriate list based on the index
                            if (index == 0) {
                                gainerList.add(cryptocurrency)
                            } else {
                                loserList.add(cryptocurrency)
                            }
                        }

                        // Set the cryptocurrency lists to the ViewModel state
                        cryptocurrencyGainerList.value = gainerList
                        cryptocurrencyLoserList.value = loserList
                    }

                    // Set the cryptocurrency IDs by names
                    setCryptocurrencyIdsByNames(cryptocurrencyGainerList.value.map { it.name } + cryptocurrencyLoserList.value.map { it.name })
                } else
                    println("Response unsuccessful: $response")
            } catch (e: Exception) {
                println("Exception occurred: $e")
            }
        }.invokeOnCompletion {
            isLoading.value = false
        } // Set loading state to false when the loading is completed
    }

    // This function fetches the ID of a cryptocurrency by its name using the CoinGecko API.
    private suspend fun setCryptocurrencyIdsByNames(names: List<String>) {
        try {
            val query = names.joinToString(",")// Create a query string from the list of names

            val response = withContext(Dispatchers.IO) {
                CryptocurrencyAPI.retrofitService.getCryptocurrencyListByNames(query)
            }

            val gainerList = mutableStateListOf<Cryptocurrency>()
            val loserList = mutableStateListOf<Cryptocurrency>()

            if (response.isSuccessful) {
                response.body()?.let {
                    it.forEach { cryptocurrencyFromApi ->
                        for (cryptocurrency in cryptocurrencyGainerList.value) {
                            if (cryptocurrency.name == cryptocurrencyFromApi.name) {
                                cryptocurrency.id = cryptocurrencyFromApi.id
                                cryptocurrency.lastUpdated =
                                    DateTimeFormatter.ISO_INSTANT.format(Instant.now())
                                gainerList.add(cryptocurrency)
                            }
                        }

                        for (cryptocurrency in cryptocurrencyLoserList.value) {
                            if (cryptocurrency.name == cryptocurrencyFromApi.name) {
                                cryptocurrency.id = cryptocurrencyFromApi.id
                                cryptocurrency.lastUpdated =
                                    DateTimeFormatter.ISO_INSTANT.format(Instant.now())
                                loserList.add(cryptocurrency)
                            }
                        }
                    }

                    // Set the cryptocurrency lists to the ViewModel state
                    cryptocurrencyGainerList.value = gainerList
                    cryptocurrencyLoserList.value = loserList
                }
            } else {
                println("Response unsuccessful: $response")
            }
        } catch (e: Exception) {
            println("Exception occurred: ${e.message}")
        }
    }
}