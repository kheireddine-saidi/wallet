package com.gemwallet.android.features.settings.viewmodels.models

import com.gemwallet.android.ui.models.actions.SettingsAction
import uniffi.gemstone.GemListRow
import uniffi.gemstone.GemRowAction

fun GemListRow.settingsAction(): SettingsAction? = when (action()) {
    GemRowAction.Wallets -> SettingsAction.Wallets
    GemRowAction.Security -> SettingsAction.Security
    GemRowAction.Notifications -> SettingsAction.Notifications
    GemRowAction.Preferences -> SettingsAction.Preferences
    GemRowAction.WalletConnect -> SettingsAction.Connections
    // NOTE: Rewards, Perpetuals, and Support are disabled.
    GemRowAction.Support -> null
    GemRowAction.Rewards -> null
    GemRowAction.Perpetuals -> null
    GemRowAction.AboutUs -> SettingsAction.AboutUs
    GemRowAction.Developer -> SettingsAction.Developer
    else -> null
}
