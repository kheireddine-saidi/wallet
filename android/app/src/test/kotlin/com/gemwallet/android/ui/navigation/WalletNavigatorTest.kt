package com.gemwallet.android.ui.navigation

import androidx.compose.runtime.mutableStateOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.gemwallet.android.ext.toGem
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.features.onboarding.presents.authentication.EnableAuthenticationRoute
import com.gemwallet.android.features.onboarding.presents.create_wallet.CreateWalletRoute
import com.gemwallet.android.features.onboarding.presents.create_wallet.CreateWalletSecurityReminderRoute
import com.gemwallet.android.features.onboarding.presents.import_wallet.ImportWalletRoute
import com.gemwallet.android.features.onboarding.presents.import_wallet.ImportWalletTypeRoute
import com.gemwallet.android.features.onboarding.presents.terms.AcceptTermsDestination
import com.gemwallet.android.features.onboarding.presents.terms.AcceptTermsRoute
import com.gemwallet.android.model.Session
import com.gemwallet.android.testkit.mockAsset
import com.gemwallet.android.testkit.mockAssetId
import com.gemwallet.android.testkit.mockSession
import com.gemwallet.android.testkit.mockTransactionId
import com.gemwallet.android.testkit.mockWallet
import com.gemwallet.android.ui.models.navigation.RouteMessage
import com.gemwallet.android.ui.navigation.routes.AssetPriceAlertsRoute
import com.gemwallet.android.ui.navigation.routes.AssetRoute
import com.gemwallet.android.ui.navigation.routes.ConfirmTransferRoute
import com.gemwallet.android.ui.navigation.routes.ReceiveRoute
import com.gemwallet.android.ui.navigation.routes.SetPriceAlertRoute
import com.gemwallet.android.ui.navigation.routes.SettingsRoute
import com.gemwallet.android.ui.navigation.routes.TransactionRoute
import com.gemwallet.android.ui.navigation.routes.WalletConnectorRequestRoute
import com.gemwallet.android.ui.navigation.routes.WalletRoute
import com.gemwallet.android.ui.navigation.routes.WalletsRoute
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.WalletId
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import uniffi.gemstone.GemAssetDestination
import uniffi.gemstone.GemDeeplinkService
import uniffi.gemstone.GemNavigationServiceInterface
import uniffi.gemstone.GemNavigationTarget
import uniffi.gemstone.GemServiceException
import uniffi.gemstone.UrlAction

class WalletNavigatorTest {
    @Test
    fun openAsset_leavesTheStackAloneWhenCoreFailsToOpenIt() = runTest {
        val assetId = mockAssetId(Chain.Tron)
        val navigationService = mockk<GemNavigationServiceInterface> {
            coEvery { openAsset(assetId.toIdentifier()) } throws GemServiceException.Api("offline")
        }
        val navigator = navigatorWith(WalletRootRoute, navigationService = navigationService, scope = this)

        navigator.openAsset(assetId).join()

        assertEquals(listOf(WalletRootRoute), navigator.backStack.toList())
    }

    @Test
    fun openAsset_pushesOnlyAssetsCoreOpens() = runTest {
        val blocked = mockAssetId(Chain.Tempo)
        val opened = mockAssetId(Chain.Tron)
        val navigationService = mockk<GemNavigationServiceInterface> {
            coEvery { openAsset(blocked.toIdentifier()) } returns GemNavigationTarget.None
            coEvery { openAsset(opened.toIdentifier()) } returns GemNavigationTarget.Asset(GemAssetDestination.Asset(mockAsset(id = mockAssetId(chain = Chain.Tron)).toGem()), walletId = null)
        }
        val navigator = navigatorWith(WalletRootRoute, navigationService = navigationService, scope = this)

        navigator.openAsset(blocked).join()
        navigator.openAsset(opened).join()

        assertEquals(listOf(WalletRootRoute, AssetRoute(opened)), navigator.backStack.toList())
    }

    @Test
    fun finishAcceptTerms_replacesCreateTermsRoute() {
        val navigator = navigatorWith(OnboardingRoute, AcceptTermsRoute(AcceptTermsDestination.Create))

        navigator.finishAcceptTerms(AcceptTermsDestination.Create)

        assertEquals(listOf(OnboardingRoute, CreateWalletSecurityReminderRoute), navigator.backStack.toList())
    }

    @Test
    fun finishAcceptTerms_replacesImportTermsRoute() {
        val navigator = navigatorWith(OnboardingRoute, AcceptTermsRoute(AcceptTermsDestination.Import))

        navigator.finishAcceptTerms(AcceptTermsDestination.Import)

        assertEquals(listOf(OnboardingRoute, ImportWalletTypeRoute), navigator.backStack.toList())
    }

    @Test
    fun resetToWallet_landsOnTheWalletFromCreateOrImport() {
        val createNavigator = navigatorWith(OnboardingRoute, CreateWalletRoute)
        val importNavigator = navigatorWith(OnboardingRoute, ImportWalletRoute.MulticoinWallet)

        createNavigator.resetToWallet()
        importNavigator.resetToWallet()

        assertEquals(listOf(WalletRootRoute), createNavigator.backStack.toList())
        assertEquals(listOf(WalletRootRoute), importNavigator.backStack.toList())
    }

    @Test
    fun resetToWallet_returnsToTheWalletTabAndDropsResultsLeftForTheOldStack() {
        val assetId = mockAssetId(Chain.Solana)
        val target = AssetPriceAlertsRoute(assetId)
        val navigator = navigatorWith(WalletRootRoute, target, SetPriceAlertRoute(assetId))
        navigator.currentTab.value = SettingsRoute
        navigator.popWithToast("Created")

        navigator.resetToWallet()

        assertEquals(WalletRoute, navigator.currentTab.value)
        assertNull(navigator.routeMessage(target))
    }

    @Test
    fun openEnableAuthentication_offersOverTheWalletSoARestoreKeepsTheOffer() {
        val navigator = navigatorWith(OnboardingRoute, CreateWalletRoute)

        navigator.openEnableAuthentication()

        val stack = navigator.backStack.toList()
        assertEquals(listOf(WalletRootRoute, EnableAuthenticationRoute), stack)
    }

    @Test
    fun openPendingNavigation_resetsWalletStackAndOpensRoute() {
        val navigator = navigatorWith(WalletRootRoute, WalletsRoute)
        val route = AssetRoute(mockAssetId(Chain.Solana))

        val opened = navigator.openPendingNavigation(listOf(route))

        assertTrue(opened)
        assertEquals(listOf(WalletRootRoute, route), navigator.backStack.toList())
    }

    @Test
    fun openPendingNavigation_waitsWhenWalletRootIsUnavailable() {
        val navigator = navigatorWith(OnboardingRoute)

        val opened = navigator.openPendingNavigation(listOf(AssetRoute(mockAssetId(Chain.Solana))))

        assertFalse(opened)
        assertEquals(listOf(OnboardingRoute), navigator.backStack.toList())
    }

    @Test
    fun popConfirmFlow_popsToRootWhenNoAssetUnderneath() {
        val navigator = navigatorWith(
            WalletRootRoute,
            ConfirmTransferRoute("confirm"),
        )

        navigator.popConfirmFlow()

        assertEquals(listOf(WalletRootRoute), navigator.backStack.toList())
    }

    @Test
    fun popWithToast_scopesMessageToPreviousRoute() {
        val assetId = mockAssetId(Chain.Solana)
        val target = AssetPriceAlertsRoute(assetId)
        val navigator = navigatorWith(
            WalletRootRoute,
            target,
            SetPriceAlertRoute(assetId),
        )

        navigator.popWithToast("Created")

        assertEquals(listOf(WalletRootRoute, target), navigator.backStack.toList())
        assertEquals(RouteMessage.Toast("Created"), navigator.routeMessage(target))

        navigator.clearRouteMessage(target)

        assertNull(navigator.routeMessage(target))
    }

    @Test
    fun showWalletConnectorRequest_pushesOneRouteAndReplacesItForTheNextRequest() {
        val navigator = navigatorWith(WalletRootRoute)

        navigator.showWalletConnectorRequest("request/topic/1")
        navigator.showWalletConnectorRequest("request/topic/1")
        assertEquals(listOf(WalletRootRoute, WalletConnectorRequestRoute("request/topic/1")), navigator.backStack.toList())

        navigator.showWalletConnectorRequest("request/topic/2")
        assertEquals(listOf(WalletRootRoute, WalletConnectorRequestRoute("request/topic/2")), navigator.backStack.toList())
    }

    @Test
    fun showWalletConnectorRequest_removesTheRouteUnderneathAScreenItOpened() {
        val navigator = navigatorWith(WalletRootRoute, WalletConnectorRequestRoute("request/topic/1"), ReceiveRoute(mockAssetId(Chain.Tron)))

        navigator.showWalletConnectorRequest(null)

        assertEquals(listOf(WalletRootRoute, ReceiveRoute(mockAssetId(Chain.Tron))), navigator.backStack.toList())
    }

    @Test
    fun showWalletConnectorRequest_leavesTheShownRequestUnderTheScreenItOpened() {
        val request = WalletConnectorRequestRoute("request/topic/1")
        val navigator = navigatorWith(WalletRootRoute, request, ReceiveRoute(mockAssetId(Chain.Tron)))

        navigator.showWalletConnectorRequest("request/topic/1")

        assertEquals(listOf(WalletRootRoute, request, ReceiveRoute(mockAssetId(Chain.Tron))), navigator.backStack.toList())
    }

    @Test
    fun observeCurrentWallet_leavesTheStackAloneForTheWalletItStartsWith() = runTest {
        val request = WalletConnectorRequestRoute("request/topic/1")
        val session = MutableStateFlow<Session?>(mockSession(wallet = mockWallet(id = WalletId("current"))))
        val navigator = navigatorWith(WalletRootRoute, request, ReceiveRoute(mockAssetId(Chain.Tron)), session = session)

        backgroundScope.launch { navigator.observeCurrentWallet() }

        assertEquals(listOf(WalletRootRoute, request, ReceiveRoute(mockAssetId(Chain.Tron))), navigator.backStack.toList())
    }

    @Test
    fun openTransaction_keysTheDetailsToTheWalletTheyAreOpenedFor() {
        val wallet = mockWallet(id = WalletId("shown"))
        val transactionId = mockTransactionId()
        val navigator = navigatorWith(WalletRootRoute, session = MutableStateFlow(mockSession(wallet = wallet)))

        navigator.openTransaction(transactionId)

        assertEquals(listOf(WalletRootRoute, TransactionRoute(wallet.id, transactionId)), navigator.backStack.toList())
    }

    private fun navigatorWith(
        vararg routes: NavKey,
        navigationService: GemNavigationServiceInterface = mockk(relaxed = true),
        session: StateFlow<Session?> = MutableStateFlow(null),
        scope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined),
        onOpenAction: (UrlAction) -> Unit = {},
    ): WalletNavigator = WalletNavigator(
        backStack = NavBackStack(*routes),
        currentTab = mutableStateOf(WalletRoute),
        deeplinkService = GemDeeplinkService(),
        navigationService = navigationService,
        session = session,
        scope = scope,
        onOpenAction = onOpenAction,
    )
}
