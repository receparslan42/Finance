package com.receparslan.finance.feature.detail.domain.model

data class KlineData(
    val openTime: Long,
    val open: String,
    val high: String,
    val low: String,
    val close: String,
    val closeTime: Long
)