package com.gemwallet.android.data.services.gemstone.stores

import com.gemwallet.android.data.services.store.database.BalancesDao
import com.gemwallet.android.data.services.store.database.PerpetualDao
import com.gemwallet.android.data.services.store.database.PerpetualPositionDao
import com.gemwallet.android.data.services.store.database.StoreTransactionRunner
import uniffi.gemstone.GemPerpetualStore
import uniffi.gemstone.PerpetualProvider as GemPerpetualProvider

class GemstonePerpetualStore(
    private val perpetualDao: PerpetualDao,
    private val perpetualPositionDao: PerpetualPositionDao,
    private val balancesDao: BalancesDao,
    private val transactionRunner: StoreTransactionRunner,
) : GemPerpetualStore {
    override suspend fun getPerpetuals(names: List<String>): List<uniffi.gemstone.Perpetual> = emptyList()
    override suspend fun savePerpetuals(data: List<uniffi.gemstone.PerpetualData>) {}
    override suspend fun setPinned(perpetualIds: List<String>, pinned: Boolean) {}
    override suspend fun clearPerpetuals(collateralAssetIds: List<String>) {}
    override suspend fun getPositions(walletId: String, provider: GemPerpetualProvider): List<uniffi.gemstone.PerpetualPosition> = emptyList()
    override suspend fun updateMarket(market: uniffi.gemstone.PerpetualMarketData) {}
    override suspend fun updatePrices(prices: List<uniffi.gemstone.PerpetualPrice>) {}
    override suspend fun getPositionIds(walletId: String, provider: GemPerpetualProvider): List<String> = emptyList()
    override suspend fun updatePositions(walletId: String, positions: List<uniffi.gemstone.PerpetualPosition>, deleteIds: List<String>) {}
}
