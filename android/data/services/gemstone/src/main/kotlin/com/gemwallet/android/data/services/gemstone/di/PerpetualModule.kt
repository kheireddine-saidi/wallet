package com.gemwallet.android.data.services.gemstone.di

import com.gemwallet.android.data.services.gemstone.stores.GemstonePerpetualStore
import com.gemwallet.android.data.services.store.database.BalancesDao
import com.gemwallet.android.data.services.store.database.PerpetualDao
import com.gemwallet.android.data.services.store.database.PerpetualPositionDao
import com.gemwallet.android.data.services.store.database.StoreTransactionRunner
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import uniffi.gemstone.GemAssetsService
import uniffi.gemstone.GemBalanceService
import uniffi.gemstone.GemConfigService
import uniffi.gemstone.GemGateway
import uniffi.gemstone.GemPerpetualService
import uniffi.gemstone.GemPreferencesService
import uniffi.gemstone.GemPriceService
import uniffi.gemstone.GemRecentActivityService
import uniffi.gemstone.GemWalletPreferencesService
import uniffi.gemstone.GemWalletSessionService
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object PerpetualModule {

    @Provides
    @Singleton
    fun provideGemstonePerpetualStore(perpetualDao: PerpetualDao, perpetualPositionDao: PerpetualPositionDao, balancesDao: BalancesDao, transactionRunner: StoreTransactionRunner): GemstonePerpetualStore =
        GemstonePerpetualStore(perpetualDao, perpetualPositionDao, balancesDao, transactionRunner)

    @Provides
    @Singleton
    fun provideGemPerpetualService(
        gateway: GemGateway,
        priceService: GemPriceService,
        perpetualStore: GemstonePerpetualStore,
        assetsService: GemAssetsService,
        preferencesService: GemPreferencesService,
        balanceService: GemBalanceService,
        walletPreferencesService: GemWalletPreferencesService,
        walletSessionService: GemWalletSessionService,
        recentActivityService: GemRecentActivityService,
        configService: GemConfigService,
    ): GemPerpetualService = GemPerpetualService(
        gateway,
        priceService,
        perpetualStore,
        assetsService,
        preferencesService,
        balanceService,
        walletPreferencesService,
        walletSessionService,
        recentActivityService,
        configService,
    )
}
