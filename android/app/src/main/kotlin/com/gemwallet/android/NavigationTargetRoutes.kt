package com.gemwallet.android

import androidx.navigation3.runtime.NavKey
import com.gemwallet.android.ext.toChain
import com.gemwallet.android.ext.toPrimitives
import com.gemwallet.android.ui.navigation.routes.AddressDetailsRoute
import com.gemwallet.android.ui.navigation.routes.AssetRoute
import com.gemwallet.android.ui.navigation.routes.ChartRoute
import com.gemwallet.android.ui.navigation.routes.ReceiveRoute
import com.gemwallet.android.ui.navigation.routes.TransactionRoute
import com.wallet.core.primitives.ChainAddress
import com.wallet.core.primitives.TransactionId
import com.wallet.core.primitives.WalletId
import uniffi.gemstone.GemAssetDestination
import uniffi.gemstone.GemNavigationTarget

internal fun GemNavigationTarget.destination(): PendingNavigation.Routes = PendingNavigation.Routes(routes(), tab())

internal fun GemNavigationTarget.routes(): List<NavKey> = when (this) {
    is GemNavigationTarget.Asset -> destination.routes()
    is GemNavigationTarget.Chart -> listOf(ChartRoute(asset.toPrimitives().id))
    is GemNavigationTarget.Receive -> listOf(ReceiveRoute(asset.toPrimitives().id))
    is GemNavigationTarget.Fiat -> emptyList()
    is GemNavigationTarget.Swap -> emptyList()
    GemNavigationTarget.Perpetuals -> emptyList()
    is GemNavigationTarget.Rewards -> emptyList()
    GemNavigationTarget.Support -> emptyList()
    is GemNavigationTarget.Transaction -> destination.routes() + TransactionRoute(WalletId(walletId), TransactionId(transactionId))
    is GemNavigationTarget.Address -> listOf(AddressDetailsRoute(ChainAddress(chain.toChain(), address)))
    GemNavigationTarget.None -> emptyList()
}

internal fun GemNavigationTarget.walletId(): String? = when (this) {
    is GemNavigationTarget.Asset -> walletId

    is GemNavigationTarget.Transaction -> walletId

    is GemNavigationTarget.Chart,
    is GemNavigationTarget.Receive,
    is GemNavigationTarget.Fiat,
    is GemNavigationTarget.Swap,
    GemNavigationTarget.Perpetuals,
    is GemNavigationTarget.Rewards,
    GemNavigationTarget.Support,
    is GemNavigationTarget.Address,
    GemNavigationTarget.None,
    -> null
}

private fun GemAssetDestination.routes(): List<NavKey> = when (this) {
    is GemAssetDestination.Asset -> listOf(AssetRoute(asset.toPrimitives().id))
    is GemAssetDestination.Perpetual -> emptyList()
}
