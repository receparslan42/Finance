package com.receparslan.finance.core.data.local.datasource

import com.receparslan.finance.core.data.local.dao.CryptocurrencyDao
import com.receparslan.finance.core.data.local.entity.CryptocurrencyEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementation of [CryptoLocalDataSource] that uses [CryptocurrencyDao] for database operations.
 * This is the only class that should directly interact with the Room DAO.
 */
class CryptoLocalDataSourceImpl @Inject constructor(
    private val dao: CryptocurrencyDao
) : CryptoLocalDataSource {

    override suspend fun saveCryptocurrency(cryptocurrency: CryptocurrencyEntity) {
        dao.insertCryptocurrency(cryptocurrency)
    }

    override suspend fun deleteCryptocurrency(cryptocurrency: CryptocurrencyEntity) {
        dao.deleteCryptocurrency(cryptocurrency)
    }

    override fun observeSavedCryptocurrencies(): Flow<List<CryptocurrencyEntity>> {
        return dao.getAllCryptocurrencies()
    }

    override fun observeSavedCryptoIds(): Flow<List<String>> {
        return dao.getAllCryptocurrencies().map { list ->
            list.map { it.id }
        }
    }
}
