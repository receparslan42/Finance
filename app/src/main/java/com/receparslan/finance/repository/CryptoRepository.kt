package com.receparslan.finance.repository

import com.receparslan.finance.database.CryptocurrencyDao
import com.receparslan.finance.model.Cryptocurrency
import com.receparslan.finance.model.KlineData
import com.receparslan.finance.service.BinanceApiService
import com.receparslan.finance.service.CoinGeckoApiService
import com.receparslan.finance.util.Constants.COINGECKO_GAINERS_LOSERS_URL
import com.receparslan.finance.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.time.Instant
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class CryptoRepository @Inject constructor(
    private val dao: CryptocurrencyDao,
    private val coingeckoAPIService: CoinGeckoApiService,
    private val binanceApiService: BinanceApiService,
    private val okHttpClient: OkHttpClient
) {
    // Get a paginated list of cryptocurrencies with error handling and resource wrapping
    suspend fun getCryptoListByPage(page: Int): Resource<List<Cryptocurrency>> =
        try {
            val response = coingeckoAPIService.getCryptoListByPage(page = page)

            if (response.isSuccessful) {
                val data = response.body()

                if (!data.isNullOrEmpty()) Resource.Success(data) else Resource.Error("No data available")
            } else Resource.Error("Api error: ${response.code()}")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "An unexpected error occurred")
        }


    // Get a list of cryptocurrencies by names with error handling and resource wrapping
    private suspend fun getCryptoListByNames(names: String): Resource<List<Cryptocurrency>> =
        try {
            val response = coingeckoAPIService.getCryptoListByNames(names = names)

            if (response.isSuccessful) {
                val data = response.body()

                if (!data.isNullOrEmpty()) Resource.Success(data) else Resource.Error("No data available")
            } else Resource.Error("Api error: ${response.code()}")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "An unexpected error occurred")
        }

    // Get cryptocurrency details by ID with error handling and resource wrapping
    suspend fun getCryptoByIDs(ids: String): Resource<List<Cryptocurrency>> =
        try {
            val response = coingeckoAPIService.getCryptoByIDs(ids = ids)

            if (response.isSuccessful) {
                val bodyData = response.body()

                val data = if (!bodyData.isNullOrEmpty()) bodyData else null

                if (data != null) Resource.Success(data) else Resource.Error("No data available")
            } else {
                Resource.Error("Api error: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "An unexpected error occurred")
        }

    // Search cryptocurrencies by query with error handling and resource wrapping
    suspend fun searchCrypto(query: String): Resource<List<Cryptocurrency>> =
        try {
            val response = coingeckoAPIService.searchCrypto(query = query)

            if (response.isSuccessful) {
                val bodyData = response.body()

                val data = bodyData?.cryptocurrencies

                if (!data.isNullOrEmpty()) Resource.Success(data) else Resource.Error("No data available")
            } else
                Resource.Error("Api error: ${response.code()}")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "An unexpected error occurred")
        }

    // Get top gainers and losers by web scraping with error handling and resource wrapping without ids
    suspend fun getGainersAndLosers(): Resource<Map<String, List<Cryptocurrency>>> =
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(COINGECKO_GAINERS_LOSERS_URL)
                    .build()

                val response = okHttpClient.newCall(request).execute()

                if (response.isSuccessful) {
                    val html: String =
                        response.body.string() // Get the HTML response as a string
                    val document = Jsoup.parse(html) // Parse the HTML response using Jsoup
                    val headers =
                        document.select("tbody") // Select the table body from the HTML document

                    // Create lists to hold gainers and losers
                    val gainerList = mutableListOf<Cryptocurrency>()
                    val loserList = mutableListOf<Cryptocurrency>()

                    headers.forEachIndexed { index, item ->
                        item.select("tr").forEach {
                            // Extract cryptocurrency details from the HTML elements
                            val symbol = it.select("a").select("div > div > div").text()
                            val name = it.select("a").select("div > div").text()
                                .substringBeforeLast("$symbol $symbol").trim()
                            val image = it.select("img").attr("src").substringBefore("?")
                            val price = it.select("td")[3].text()
                                .substringAfter("$").replace(",", "").toDouble()
                            val change = it.select("td")[5].text().substringBefore("%").toDouble()

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
                    }

                    // Get cryptocurrency IDs by names and set them
                    val resource = setGainerLoserCryptoIDsByNames(
                        mapOf(
                            "gainers" to gainerList,
                            "losers" to loserList
                        )
                    )

                    // Set the cryptocurrency IDs by names and return the result
                    when (resource) {
                        is Resource.Success -> Resource.Success(resource.data)
                        is Resource.Error -> Resource.Error(resource.message)
                    }
                } else
                    Resource.Error("Api error: ${response.code}")
            } catch (e: Exception) {
                Resource.Error(e.localizedMessage ?: "An unexpected error occurred")
            }
        }

    private suspend fun setGainerLoserCryptoIDsByNames(gainersLosers: Map<String, List<Cryptocurrency>>): Resource<Map<String, List<Cryptocurrency>>> =
        withContext(Dispatchers.IO) {
            try {
                // Create gainer list from the map
                val gainerList = mutableListOf<Cryptocurrency>().apply {
                    addAll(
                        gainersLosers["gainers"] ?: emptyList()
                    )
                }

                // Create loser list from the map
                val loserList = mutableListOf<Cryptocurrency>().apply {
                    addAll(
                        gainersLosers["losers"] ?: emptyList()
                    )
                }

                // Combine names from both lists for querying
                val names = (gainerList + loserList).joinToString(",") { it.name }

                when (val resource = getCryptoListByNames(names)) {
                    is Resource.Success -> {
                        // Update gainerList with IDs
                        gainerList.replaceAll { crypto ->
                            val matchedCrypto = resource.data.find { it.name == crypto.name }
                            if (matchedCrypto != null) {
                                crypto.copy(
                                    id = matchedCrypto.id,
                                    lastUpdated = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
                                )
                            } else
                                crypto
                        }

                        // Update loser list with IDs
                        loserList.replaceAll { crypto ->
                            val matchedCrypto = resource.data.find { it.name == crypto.name }
                            if (matchedCrypto != null) {
                                crypto.copy(
                                    id = matchedCrypto.id,
                                    lastUpdated = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
                                )
                            } else
                                crypto
                        }

                        Resource.Success(
                            mapOf(
                                "gainers" to gainerList,
                                "losers" to loserList
                            )
                        )
                    }

                    is Resource.Error -> {
                        Resource.Error(resource.message)
                    }
                }
            } catch (e: Exception) {
                Resource.Error(e.localizedMessage ?: "An unexpected error occurred")
            }
        }

    // Get historical Kline data by range with error handling and resource wrapping
    suspend fun getHistoricalDataByRange(
        symbol: String,
        startTime: Long,
        endTime: Long,
        interval: String
    ): Resource<List<KlineData>> =
        try {
            val response: List<KlineData> = binanceApiService.getHistoricalDataByRange(
                symbol = if (symbol.uppercase() == "USDT") "BTCUSDT" else symbol.uppercase() + "USDT",
                startTime = startTime,
                endTime = endTime,
                interval = interval
            )

            if (response.isNotEmpty()) {
                val klineDataList = if (symbol.uppercase() == "USDT") {
                    response.map { klineData ->
                        klineData.copy(
                            open = "1.0",
                            high = "1.0",
                            low = "1.0",
                            close = "1.0"
                        )
                    }
                } else response

                Resource.Success(klineDataList)
            } else Resource.Error("No data available")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "An unexpected error occurred")
        }

    // Save cryptocurrency to t he database with error handling and resource wrapping
    suspend fun saveCryptoToDb(cryptocurrency: Cryptocurrency): Resource<Unit> =
        try {
            Resource.Success(dao.insertCryptocurrency(cryptocurrency))
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "An unexpected error occurred")
        }


    // Delete cryptocurrency from the database with error handling and resource wrapping
    suspend fun deleteCryptoFromDb(cryptocurrency: Cryptocurrency): Resource<Unit> =
        try {
            Resource.Success(dao.deleteCryptocurrency(cryptocurrency))
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "An unexpected error occurred")
        }


    // Flow to get all saved cryptocurrency IDs from the database with resource wrapping and error handling
    fun getAllSavedCryptoIDsFlow(): Flow<Resource<List<String>>> {
        return dao.getAllCryptocurrencies()
            .map<List<Cryptocurrency>, Resource<List<String>>> { list ->
                Resource.Success(list.map { cryptocurrency -> cryptocurrency.id })
            }.catch { e ->
                emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
            }
    }
}