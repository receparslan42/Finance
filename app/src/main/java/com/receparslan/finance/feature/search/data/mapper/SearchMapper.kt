package com.receparslan.finance.feature.search.data.mapper

import com.receparslan.finance.core.data.mapper.toDomain
import com.receparslan.finance.core.domain.model.Cryptocurrency
import com.receparslan.finance.feature.search.data.remote.dto.SearchResponseDto

fun SearchResponseDto.toDomain(): List<Cryptocurrency> {
    return cryptocurrencies.map { it.toDomain() }
}