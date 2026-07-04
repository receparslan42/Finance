package com.receparslan.finance.feature.detail.data.mapper

import com.receparslan.finance.feature.detail.data.remote.dto.KlineDataDto
import com.receparslan.finance.feature.detail.domain.model.KlineData

fun KlineDataDto.toDomain(): KlineData {
    return KlineData(
        openTime = openTime,
        open = open,
        high = high,
        low = low,
        close = close,
        closeTime = closeTime
    )
}

fun KlineData.toDto(): KlineDataDto {
    return KlineDataDto(
        openTime = openTime,
        open = open,
        high = high,
        low = low,
        close = close,
        closeTime = closeTime
    )
}