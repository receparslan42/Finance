package com.receparslan.finance.feature.detail.domain.usecase

import com.receparslan.finance.core.common.Resource
import com.receparslan.finance.core.domain.repository.CryptoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSavedCryptoIdsUseCase @Inject constructor(
    private val repository: CryptoRepository
) {
    operator fun invoke(): Flow<Resource<List<String>>> {
        return repository.getAllSavedCryptoIDsFlow()
    }
}