package com.receparslan.finance.feature.favourites.domain.usecase

import com.receparslan.finance.core.common.Resource
import com.receparslan.finance.core.domain.model.Cryptocurrency
import com.receparslan.finance.core.domain.repository.CryptoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveFavoritesUseCase @Inject constructor(
    private val repository: CryptoRepository
) {
    operator fun invoke(): Flow<Resource<List<Cryptocurrency>>> {
        return repository.observeSavedCryptocurrencies()
    }
}