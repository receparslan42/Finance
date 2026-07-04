package com.receparslan.finance.feature.home.domain.usecase

import com.receparslan.finance.core.common.Resource
import com.receparslan.finance.core.domain.model.Cryptocurrency
import com.receparslan.finance.core.domain.repository.CryptoRepository
import javax.inject.Inject

class GetCryptoListUseCase @Inject constructor(
    private val repository: CryptoRepository
) {
    suspend operator fun invoke(page: Int): Resource<List<Cryptocurrency>> {
        return repository.getCryptoListByPage(page)
    }
}