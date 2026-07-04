package com.receparslan.finance.di

import com.receparslan.finance.core.data.remote.datasource.CryptoRemoteDataSource
import com.receparslan.finance.core.data.remote.datasource.CryptoRemoteDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RemoteDataModule {

    @Binds
    @Singleton
    abstract fun bindCryptoRemoteDataSource(
        impl: CryptoRemoteDataSourceImpl
    ): CryptoRemoteDataSource
}