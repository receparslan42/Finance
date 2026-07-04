package com.receparslan.finance.di

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.receparslan.finance.core.common.ApiConstants
import com.receparslan.finance.core.data.local.dao.CryptocurrencyDao
import com.receparslan.finance.core.data.local.database.CryptocurrencyDatabase
import com.receparslan.finance.feature.detail.data.remote.dto.KlineDataDto
import com.receparslan.finance.core.data.remote.serializer.KlineDataDeserializer
import com.receparslan.finance.core.data.remote.api.BinanceApiService
import com.receparslan.finance.core.data.remote.api.CoinGeckoApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Provide Application Context
    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context

    // Provide OkHttpClient with timeout settings
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // Custom Gson instance with KlineData deserializer
    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder()
        .registerTypeAdapter(
            KlineDataDto::class.java,
            KlineDataDeserializer()
        )
        .create()

    // Provide CoinGeckoApiService with Retrofit
    @Provides
    @Singleton
    fun provideCoinGeckoApiService(okHttpClient: OkHttpClient): CoinGeckoApiService =
        Retrofit.Builder()
            .baseUrl(ApiConstants.CoinGecko.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CoinGeckoApiService::class.java)

    // Provide BinanceApiService with Retrofit and custom Gson converter
    @Provides
    @Singleton
    fun provideBinanceApiService(okHttpClient: OkHttpClient, gson: Gson): BinanceApiService =
        Retrofit.Builder()
            .baseUrl(ApiConstants.Binance.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(BinanceApiService::class.java)

    // Provide CryptocurrencyDatabase using Room
    @Provides
    @Singleton
    fun provideCryptocurrencyDatabase(@ApplicationContext context: Context): CryptocurrencyDatabase =
        Room.databaseBuilder(
            context,
            CryptocurrencyDatabase::class.java,
            "finance_db"
        ).build()

    // Provide CryptocurrencyDao from the database
    @Provides
    @Singleton
    fun provideCryptocurrencyDao(database: CryptocurrencyDatabase): CryptocurrencyDao =
        database.cryptocurrencyDao()
}