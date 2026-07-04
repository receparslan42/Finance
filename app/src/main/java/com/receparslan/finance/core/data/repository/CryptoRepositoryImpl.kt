package com.receparslan.finance.core.data.repository

import com.receparslan.finance.core.data.local.datasource.CryptoLocalDataSource
import com.receparslan.finance.core.domain.model.Cryptocurrency
import com.receparslan.finance.feature.detail.domain.model.KlineData
import com.receparslan.finance.feature.gainloss.domain.model.GainersLosers
import com.receparslan.finance.core.data.remote.datasource.CryptoRemoteDataSource
import com.receparslan.finance.core.data.remote.CoinGeckoScraper
import com.receparslan.finance.core.data.mapper.toDomain
import com.receparslan.finance.core.data.mapper.toEntity
import com.receparslan.finance.feature.detail.data.mapper.toDomain
import com.receparslan.finance.feature.search.data.mapper.toDomain
import com.receparslan.finance.feature.gainloss.data.mapper.toDomain
import com.receparslan.finance.core.common.Constants.MAX_RETRY
import com.receparslan.finance.core.common.Constants.RETRY_DELAY
import com.receparslan.finance.core.common.Resource
import com.receparslan.finance.core.domain.repository.CryptoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

class CryptoRepositoryImpl @Inject constructor(
    private val remoteDataSource: CryptoRemoteDataSource,
    private val coingeckoScraper: CoinGeckoScraper,
    private val localDataSource: CryptoLocalDataSource
) : CryptoRepository {
    // Get a list of cryptocurrencies by IDs with error handling and resource wrapping
    override suspend fun getCryptoByIDs(ids: String): Resource<List<Cryptocurrency>> =
        withContext(Dispatchers.IO) {
            repeat(5) { attempt ->
                try {
                    val bodyData = remoteDataSource.getCryptoByIDs(ids = ids)

                    val data = if (bodyData.isNotEmpty())
                        bodyData.map { it.toDomain() }
                    else
                        throw Exception("No data available")

                    return@withContext Resource.Success(data)
                } catch (e: Exception) {
                    if (attempt == 4)
                        return@withContext Resource.Error(
                            e.localizedMessage ?: "An unexpected error occurred"
                        )
                    else
                        delay(RETRY_DELAY.milliseconds)
                }
            }

            return@withContext Resource.Error("Failed to fetch data after multiple attempts")
        }

    // Get a list of cryptocurrencies by page with retry mechanism, error handling, and resource wrapping
    override suspend fun getCryptoListByPage(page: Int): Resource<List<Cryptocurrency>> =
        withContext(Dispatchers.IO) {
            repeat(MAX_RETRY) { attempt ->
                try {
                    val data = remoteDataSource.getCryptoListByPage(page = page)

                    if (data.isNotEmpty())
                        return@withContext Resource.Success(data.map { it.toDomain() })
                    else
                        throw Exception("No data available")
                } catch (e: Exception) {
                    if (attempt == MAX_RETRY - 1)
                        return@withContext Resource.Error(e.localizedMessage ?: "An unexpected error occurred")
                    else
                        delay(RETRY_DELAY.milliseconds)
                }
            }

            return@withContext Resource.Error("Failed to fetch data after multiple attempts")
        }

    // Get a list of cryptocurrencies by names with error handling and resource wrapping
    override suspend fun getCryptoListByNames(names: String): Resource<List<Cryptocurrency>> =
        withContext(Dispatchers.IO) {
            try {
                val data = remoteDataSource.getCryptoListByNames(names = names)

                if (data.isNotEmpty())
                    Resource.Success(data.map { it.toDomain() })
                else
                    throw Exception("No data available")
            } catch (e: Exception) {
                Resource.Error(e.localizedMessage ?: "An unexpected error occurred")
            }
        }


    // Search for cryptocurrencies by query with error handling and resource wrapping
    override suspend fun searchCrypto(query: String): Resource<List<Cryptocurrency>> =
        withContext(Dispatchers.IO) {
            if (query.isEmpty())
                return@withContext Resource.Success(emptyList())

            repeat(5) { attempt ->
                try {
                    val bodyData = remoteDataSource.searchCrypto(query = query)

                    val data = bodyData.toDomain()

                    if (data.isNotEmpty())
                        return@withContext Resource.Success(data)
                    else
                        throw Exception("No data available")
                } catch (e: Exception) {
                    if (attempt == 4)
                        return@withContext Resource.Error(
                            e.localizedMessage ?: "An unexpected error occurred"
                        )
                    else
                        delay(RETRY_DELAY.milliseconds)
                }
            }

            return@withContext Resource.Error("Failed to fetch data after multiple attempts")
        }

    // Get gainers and losers by scraping CoinGecko with error handling and resource wrapping, and set cryptocurrency IDs by names
    override suspend fun getGainersAndLosers(): Resource<GainersLosers> =
        withContext(Dispatchers.IO) {
            try {
                val scraperResult = coingeckoScraper.scrapeGainersAndLosers()
                val gainersLosers = scraperResult.toDomain()

                val gainerList = mutableListOf<Cryptocurrency>().apply {
                    addAll(gainersLosers.gainers)
                }

                val loserList = mutableListOf<Cryptocurrency>().apply {
                    addAll(gainersLosers.losers)
                }

                if (gainerList.isEmpty() && loserList.isEmpty())
                    return@withContext Resource.Success(
                        GainersLosers(
                            gainers = emptyList(),
                            losers = emptyList()
                        )
                    )

                val names = (gainerList + loserList).joinToString(",") { it.name }

                // Get cryptocurrency IDs by names and set them
                when (val resource = getCryptoListByNames(names)) {
                    is Resource.Success -> {
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

                        return@withContext Resource.Success(
                            GainersLosers(
                                gainers = gainerList,
                                losers = loserList
                            )
                        )
                    }

                    is Resource.Error -> return@withContext Resource.Error(resource.message)
                }
            } catch (e: Exception) {
                return@withContext Resource.Error(
                    e.localizedMessage ?: "An unexpected error occurred"
                )
            }
        }


    // Get historical Kline data for a cryptocurrency by symbol and time range with error handling and resource wrapping
    override suspend fun getHistoricalDataByRange(
        symbol: String,
        startTime: Long,
        endTime: Long,
        interval: String
    ): Resource<List<KlineData>> = withContext(Dispatchers.IO) {
        try {
            val responseDto = remoteDataSource.getHistoricalDataByRange(
                symbol = if (symbol.uppercase() == "USDT") "BTCUSDT" else symbol.uppercase() + "USDT",
                startTime = startTime,
                endTime = endTime,
                interval = interval
            )

            if (responseDto.isNotEmpty()) {
                val klineDataList = if (symbol.uppercase() == "USDT") {
                    responseDto.map { dto ->
                        dto.toDomain().copy(
                            open = "1.0",
                            high = "1.0",
                            low = "1.0",
                            close = "1.0"
                        )
                    }
                } else responseDto.map { it.toDomain() }

                Resource.Success(klineDataList)
            } else
                Resource.Error("No data available")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unexpected error occurred")
        }
    }


    // Save cryptocurrency to the database with error handling and resource wrapping
    override suspend fun saveCryptoToDb(cryptocurrency: Cryptocurrency): Resource<Unit> =
        withContext(Dispatchers.IO) {
            try {
                Resource.Success(localDataSource.saveCryptocurrency(cryptocurrency.toEntity()))
            } catch (e: Exception) {
                Resource.Error(e.localizedMessage ?: "An unexpected error occurred")
            }
        }

    // Delete cryptocurrency from the database with error handling and resource wrapping
    override suspend fun deleteCryptoFromDb(cryptocurrency: Cryptocurrency): Resource<Unit> =
        withContext(Dispatchers.IO) {
            try {
                Resource.Success(localDataSource.deleteCryptocurrency(cryptocurrency.toEntity()))
            } catch (e: Exception) {
                Resource.Error(e.localizedMessage ?: "An unexpected error occurred")
            }
        }

    // Get a flow of all saved cryptocurrency IDs from the database with error handling and resource wrapping
    override fun getAllSavedCryptoIDsFlow(): Flow<Resource<List<String>>> =
        localDataSource.observeSavedCryptoIds()
            .map<List<String>, Resource<List<String>>> { list ->
                Resource.Success(list)
            }.catch { e ->
                emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
            }

    // Observe saved cryptocurrencies by their IDs with error handling and resource wrapping
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeSavedCryptocurrencies(): Flow<Resource<List<Cryptocurrency>>> =
        getAllSavedCryptoIDsFlow()
            .flatMapLatest { idsResource ->
                when (idsResource) {
                    is Resource.Success -> {
                        if (idsResource.data.isEmpty()) {
                            flowOf(Resource.Success(emptyList()))
                        } else {
                            val ids = idsResource.data.joinToString(",")
                            flow {
                                emit(getCryptoByIDs(ids))
                            }
                        }
                    }

                    is Resource.Error -> flowOf(Resource.Error(idsResource.message))
                }
            }
}