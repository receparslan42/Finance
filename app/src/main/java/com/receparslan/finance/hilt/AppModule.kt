package com.receparslan.finance.hilt

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.receparslan.finance.database.CryptocurrencyDao
import com.receparslan.finance.database.CryptocurrencyDatabase
import com.receparslan.finance.model.KlineData
import com.receparslan.finance.service.BinanceApiService
import com.receparslan.finance.service.CoinGeckoApiService
import com.receparslan.finance.util.Constants.BINANCE_BASE_URL
import com.receparslan.finance.util.Constants.COINGECKO_BASE_URL
import com.receparslan.finance.util.KlineDataDeserializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // Provide CryptocurrencyDatabase instance for database access
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CryptocurrencyDatabase =
        CryptocurrencyDatabase.getDatabase(context)

    // Provide CryptocurrencyDao instance for database operations
    @Provides
    @Singleton
    fun provideCryptocurrencyDao(database: CryptocurrencyDatabase): CryptocurrencyDao =
        database.cryptocurrencyDao()

    // Provide OkHttpClient instance for network operations with logging interceptor
    @Provides
    @Singleton
    fun provideClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.HEADERS
            })
            .build()

    // Provide Retrofit instance for CoinGecko API
    @Provides
    @Singleton
    @Named("CoinGeckoRetrofit")
    fun provideCoinGeckoRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(COINGECKO_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

    // Provide CoinGeckoApiService instance for network operations
    @Provides
    @Singleton
    fun provideCoinGeckoAPIService(
        @Named("CoinGeckoRetrofit") retrofit: Retrofit
    ): CoinGeckoApiService =
        retrofit.create(CoinGeckoApiService::class.java)

    // Provide Retrofit instance for Binance API
    @Provides
    @Singleton
    @Named("BinanceRetrofit")
    fun provideBinanceRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BINANCE_BASE_URL)
            .addConverterFactory(
                GsonConverterFactory.create(
                    // Custom Gson instance with KlineData deserializer
                    GsonBuilder()
                        .registerTypeAdapter(
                            KlineData::class.java,
                            KlineDataDeserializer()
                        ).create()
                )
            )
            .client(client)
            .build()

    // Provide BinanceApiService instance for network operations
    @Provides
    @Singleton
    fun provideBinanceAPIService(
        @Named("BinanceRetrofit") retrofit: Retrofit
    ): BinanceApiService =
        retrofit.create(BinanceApiService::class.java)

    @Provides
    @Singleton
    fun provideGsonBuilder(): GsonBuilder = GsonBuilder()

    @Provides
    @Singleton
    fun provideGson(gsonBuilder: GsonBuilder): Gson = gsonBuilder.create()
}