package com.receparslan.finance.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cryptocurrency")
data class CryptocurrencyEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val symbol: String,
    val image: String,
    val currentPrice: Double,
    val priceChangePercentage24h: Double,
    val lastUpdated: String
)