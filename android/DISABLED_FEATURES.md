# Disabled Features Documentation & Future Cleanup Guide

The following features have been disabled in the UI (UI entry points, navigation buttons, header actions, settings rows, preferences menu options, and acquisition options removed) while keeping the underlying code files intact on disk:

1. **Buy (Fiat / FiatConnect)**
2. **Swap**
3. **Rewards**
4. **Perpetuals**
5. **Stake**
6. **Support**

---

## Files and Modules to Delete in Future Refactoring

If a full removal/deletion of these features from the codebase is requested in the future, the following files and directories can be safely removed from disk:

### Feature Modules (`/android/features/`)
- `features/fiat_connect/`
- `features/swap/`
- `features/rewards/`
- `features/perpetuals/`
- `features/stake/`
- `features/support/`

### Navigation Routes (`/android/app/src/main/kotlin/com/gemwallet/android/ui/navigation/routes/`)
- `routes/Fiat.kt`
- `routes/Swap.kt`
- `routes/Rewards.kt`
- `routes/Perpetuals.kt`
- `routes/Stake.kt`

### Services & Stores (`/android/data/services/`)
- `data/coordinators/src/main/kotlin/com/gemwallet/android/data/coordinators/di/FiatModule.kt`
- `data/coordinators/src/main/kotlin/com/gemwallet/android/data/coordinators/di/SwapModule.kt`
- `data/services/gemstone/src/main/kotlin/com/gemwallet/android/data/services/gemstone/di/StakeModule.kt`
- `data/services/gemstone/src/main/kotlin/com/gemwallet/android/data/services/gemstone/stores/FiatStore.kt`
- `data/services/gemstone/src/main/kotlin/com/gemwallet/android/data/services/gemstone/stores/SwapStore.kt`
- `data/services/gemstone/src/main/kotlin/com/gemwallet/android/data/services/gemstone/stores/StakeStore.kt`
- `data/services/gemstone/src/main/kotlin/com/gemwallet/android/data/services/gemstone/stores/PerpetualStore.kt`

---

## Modified Files Disabling UI Access

The following UI files were modified to disable user access to these features while retaining all source files:

- `ui/src/main/kotlin/com/gemwallet/android/ui/components/list_head/AmountListHead.kt`: Filtered out `Buy` and `Swap` header action buttons.
- `features/settings/viewmodels/src/main/kotlin/com/gemwallet/android/features/settings/viewmodels/models/SettingsRowUIModel.kt`: Mapped `Support`, `Rewards`, and `Perpetuals` settings actions to `null`.
- `features/settings/presents/src/main/kotlin/com/gemwallet/android/features/settings/presents/SettingsScene.kt`: Filtered out settings rows with null actions.
- `features/settings/viewmodels/src/main/kotlin/com/gemwallet/android/features/settings/viewmodels/models/PreferencesRowAction.kt`: Mapped Perpetuals preferences actions to `null`.
- `features/settings/presents/src/main/kotlin/com/gemwallet/android/features/settings/presents/PreferencesScene.kt`: Filtered out null and Perpetuals/option rows so that the perpetuals toggle and options are completely hidden from the preferences menu.
- `features/wallet/presents/src/main/kotlin/com/gemwallet/android/features/wallet/presents/WalletScene.kt`: Hidden Perpetuals preview section on main wallet screen.
- `features/wallet/presents/src/main/kotlin/com/gemwallet/android/features/wallet/presents/WalletSearchScreen.kt`: Hidden Perpetuals content in search view.
- `features/assets/viewmodels/src/main/kotlin/com/gemwallet/android/features/assets/viewmodels/asset/models/AssetAction.kt`: Mapped `Stake` and `Earn` detail row actions to `null`.
- `ui/src/main/kotlin/com/gemwallet/android/ui/components/empty/EmptyContentView.kt`: Omitted `BUY` and `SWAP` action buttons from empty states.
- `features/transfer/presents/src/main/kotlin/com/gemwallet/android/features/transfer/presents/confirm/components/GetAssetSheet.kt`: Filtered out `Buy` and `Swap` options from the asset acquisition sheet.
- `app/src/main/kotlin/com/gemwallet/android/features/main/views/MainScreen.kt`: Made Buy, Swap, Perpetuals, and Support navigation actions no-ops.
