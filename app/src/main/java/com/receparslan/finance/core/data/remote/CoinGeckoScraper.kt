package com.receparslan.finance.core.data.remote

import com.receparslan.finance.core.common.ApiConstants.CoinGecko.GAINERS_LOSERS_URL
import com.receparslan.finance.core.domain.model.Cryptocurrency
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import javax.inject.Inject

class CoinGeckoScraper @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    data class GainersLosers(
        val gainers: List<Cryptocurrency>,
        val losers: List<Cryptocurrency>
    )

    suspend fun scrapeGainersAndLosers(): GainersLosers = try {
        val request = Request.Builder()
            .url(GAINERS_LOSERS_URL)
            .build()

        withContext(Dispatchers.IO) {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful)
                    return@withContext GainersLosers(
                        gainers = emptyList(),
                        losers = emptyList()
                    )

                val body = response.body
                val html = body.string()
                val document = Jsoup.parse(html)
                val headers = document.select("tbody")

                val gainerList = mutableListOf<Cryptocurrency>()
                val loserList = mutableListOf<Cryptocurrency>()

                headers.forEachIndexed { index, item ->
                    item.select("tr").forEach {
                        val symbol = it.select("a")
                            .select("div > div > div")
                            .text()

                        val name = it.select("a")
                            .select("div > div")
                            .text()
                            .substringBeforeLast("$symbol $symbol")
                            .trim()

                        val image = it.select("img")
                            .attr("src")
                            .substringBefore("?")

                        val price = it.select("td")[3]
                            .text()
                            .substringAfter("$")
                            .replace(",", "")
                            .toDouble()

                        val change = it.select("td")[5]
                            .text().substringBefore("%")
                            .toDouble()

                        val cryptocurrency = Cryptocurrency(
                            id = "",
                            name = name,
                            symbol = symbol,
                            image = image,
                            currentPrice = price,
                            priceChangePercentage24h = change,
                            lastUpdated = ""
                        )

                        if (index == 0)
                            gainerList.add(cryptocurrency)
                        else
                            loserList.add(cryptocurrency)
                    }
                }

                GainersLosers(
                    gainers = gainerList,
                    losers = loserList
                )
            }
        }

    } catch (_: Exception) {
        GainersLosers(
            gainers = emptyList(),
            losers = emptyList()
        )
    }
}