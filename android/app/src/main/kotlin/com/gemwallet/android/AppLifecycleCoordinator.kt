package com.gemwallet.android

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.gemwallet.android.data.coordinators.wallet_connect.WalletConnectCoordinator
import com.gemwallet.android.data.services.gemstone.connection.ConnectionStatusObserver
import com.gemwallet.android.data.services.gemstone.device.DeviceObserverService
import com.gemwallet.android.data.services.gemstone.stream.StreamObserverService
import com.gemwallet.android.data.services.gemstone.transactions.TransactionStatusService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLifecycleCoordinator @Inject constructor(
    private val deviceObserver: DeviceObserverService,
    private val streamObserver: StreamObserverService,
    private val connectionStatusObserver: ConnectionStatusObserver,
    private val transactionStatusService: TransactionStatusService,
    private val walletConnectCoordinator: WalletConnectCoordinator,
) : DefaultLifecycleObserver {

    override fun onStart(owner: LifecycleOwner) {
        deviceObserver.start()
        streamObserver.start()
        connectionStatusObserver.start()
        transactionStatusService.start()
        walletConnectCoordinator.start()
    }

    override fun onStop(owner: LifecycleOwner) {
        deviceObserver.stop()
        streamObserver.stop()
        connectionStatusObserver.stop()
        transactionStatusService.stop()
        walletConnectCoordinator.stop()
    }
}
