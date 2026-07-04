package com.receparslan.finance.feature.detail.domain.usecase

import com.receparslan.finance.core.common.Resource
import com.receparslan.finance.core.domain.model.Cryptocurrency
import com.receparslan.finance.core.domain.repository.CryptoRepository
import javax.inject.Inject

class DeleteFavoriteUseCase @Inject constructor(
    private val repository: CryptoRepository
) {
    suspend operator fun invoke(cryptocurrency: Cryptocurrency): Resource<Unit> {
        return repository.deleteCryptoFromDb(cryptocurrency)
    }
}