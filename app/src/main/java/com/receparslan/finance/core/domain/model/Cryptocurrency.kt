package com.receparslan.finance.core.domain.model

data class Cryptocurrency(
    val id: String,
    val name: String,
    val symbol: String,
    val image: String,
    val currentPrice: Double,
    val priceChangePercentage24h: Double,
    val lastUpdated: String
)