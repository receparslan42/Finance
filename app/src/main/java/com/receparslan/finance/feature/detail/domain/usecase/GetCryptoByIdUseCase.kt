package com.receparslan.finance.feature.detail.domain.usecase

import com.receparslan.finance.core.common.Resource
import com.receparslan.finance.core.domain.model.Cryptocurrency
import com.receparslan.finance.core.domain.repository.CryptoRepository
import javax.inject.Inject

class GetCryptoByIdUseCase @Inject constructor(
    private val repository: CryptoRepository
) {
    suspend operator fun invoke(id: String): Resource<Cryptocurrency> {
        return when (val resource = repository.getCryptoByIDs(id)) {
            is Resource.Success -> {
                val crypto = resource.data.firstOrNull()
                if (crypto != null) {
                    Resource.Success(crypto)
                } else {
                    Resource.Error("Cryptocurrency not found.")
                }
            }
            is Resource.Error -> Resource.Error(resource.message)
        }
    }
}