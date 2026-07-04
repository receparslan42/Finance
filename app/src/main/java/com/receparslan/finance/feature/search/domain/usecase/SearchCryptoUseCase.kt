package com.receparslan.finance.feature.search.domain.usecase

import com.receparslan.finance.core.common.Resource
import com.receparslan.finance.core.domain.model.Cryptocurrency
import com.receparslan.finance.core.domain.repository.CryptoRepository
import javax.inject.Inject

class SearchCryptoUseCase @Inject constructor(
    private val repository: CryptoRepository
) {
    suspend operator fun invoke(query: String): Resource<List<Cryptocurrency>> {
        if (query.isBlank()) {
            return Resource.Success(emptyList())
        }

        return when (val searchResource = repository.searchCrypto(query)) {
            is Resource.Success -> {
                val ids = searchResource.data.joinToString(",") { it.id }
                if (ids.isEmpty()) {
                    Resource.Success(emptyList())
                } else {
                    repository.getCryptoByIDs(ids)
                }
            }
            is Resource.Error -> Resource.Error(searchResource.message)
        }
    }
}