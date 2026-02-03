package com.receparslan.finance.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

// Data class to represent a list of Cryptocurrencies from API responses
data class CryptocurrencyList(
    @SerializedName("coins")
    val cryptocurrencies: List<Cryptocurrency>
)

// Data class to represent a Cryptocurrency entity in the Room database
@Entity(tableName = "cryptocurrency")
data class Cryptocurrency(
    // Using the 'id' field as the primary key for the Room database
    @PrimaryKey
    var id: String,

    // Basic cryptocurrency information
    var name: String,
    var symbol: String,
    var image: String,

    // Using SerializedName to map JSON keys to Kotlin properties when they differ
    @SerializedName("current_price")
    var currentPrice: Double,
    @SerializedName("price_change_percentage_24h")
    var priceChangePercentage24h: Double,
    @SerializedName("last_updated")
    var lastUpdated: String
)

// Data class to represent Kline data from Binance API responses
data class KlineData(
    val openTime: Long,
    val open: String,
    val high: String,
    val low: String,
    val close: String,
    val closeTime: Long
)