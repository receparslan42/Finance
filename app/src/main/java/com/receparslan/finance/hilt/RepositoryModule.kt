package com.receparslan.finance.hilt

import com.receparslan.finance.repository.CryptoRepository
import com.receparslan.finance.repository.CryptoRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// This Hilt module is responsible for providing dependencies related to the CryptoRepository interface
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindCryptoRepository(
        impl: CryptoRepositoryImpl
    ): CryptoRepository
}