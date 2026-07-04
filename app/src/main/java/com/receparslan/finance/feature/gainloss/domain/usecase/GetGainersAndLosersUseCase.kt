package com.receparslan.finance.feature.gainloss.domain.usecase

import com.receparslan.finance.core.common.Resource
import com.receparslan.finance.feature.gainloss.domain.model.GainersLosers
import com.receparslan.finance.core.domain.repository.CryptoRepository
import javax.inject.Inject

class GetGainersAndLosersUseCase @Inject constructor(
    private val repository: CryptoRepository
) {
    suspend operator fun invoke(): Resource<GainersLosers> {
        return repository.getGainersAndLosers()
    }
}