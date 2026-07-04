package com.receparslan.finance.feature.search.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.receparslan.finance.core.data.remote.dto.CryptocurrencyDto

data class SearchResponseDto(
    @SerializedName("coins")
    val cryptocurrencies: List<CryptocurrencyDto>
)