package com.receparslan.finance.core.data.mapper

import com.receparslan.finance.core.data.local.entity.CryptocurrencyEntity
import com.receparslan.finance.core.data.remote.dto.CryptocurrencyDto
import com.receparslan.finance.core.domain.model.Cryptocurrency

fun CryptocurrencyDto.toDomain(): Cryptocurrency {
    return Cryptocurrency(
        id = id ?: "",
        name = name ?: "",
        symbol = symbol ?: "",
        image = image ?: "",
        currentPrice = currentPrice ?: 0.0,
        priceChangePercentage24h = priceChangePercentage24h ?: 0.0,
        lastUpdated = lastUpdated ?: ""
    )
}

fun CryptocurrencyEntity.toDomain(): Cryptocurrency {
    return Cryptocurrency(
        id = id,
        name = name,
        symbol = symbol,
        image = image,
        currentPrice = currentPrice,
        priceChangePercentage24h = priceChangePercentage24h,
        lastUpdated = lastUpdated
    )
}

fun Cryptocurrency.toEntity(): CryptocurrencyEntity {
    return CryptocurrencyEntity(
        id = id,
        name = name,
        symbol = symbol,
        image = image,
        currentPrice = currentPrice,
        priceChangePercentage24h = priceChangePercentage24h,
        lastUpdated = lastUpdated
    )
}