package com.receparslan.finance.feature.detail.data.remote.dto

data class KlineDataDto(
    val openTime: Long,
    val open: String,
    val high: String,
    val low: String,
    val close: String,
    val closeTime: Long
)