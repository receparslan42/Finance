package com.receparslan.finance.di

import com.receparslan.finance.core.data.local.datasource.CryptoLocalDataSource
import com.receparslan.finance.core.data.local.datasource.CryptoLocalDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocalDataModule {

    @Binds
    @Singleton
    abstract fun bindCryptoLocalDataSource(
        impl: CryptoLocalDataSourceImpl
    ): CryptoLocalDataSource
}
