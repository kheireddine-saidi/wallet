package com.gemwallet.android.features.settings.presents

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.features.settings.viewmodels.models.PreferencesAction
import com.gemwallet.android.features.settings.viewmodels.models.PreferencesRowAction
import com.gemwallet.android.features.settings.viewmodels.models.preferencesAction
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.GemListRowView
import com.gemwallet.android.ui.components.list_item.OptionPickerRow
import com.gemwallet.android.ui.components.list_item.property.itemsPositioned
import com.gemwallet.android.ui.components.list_item.rows
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.localization.stringRes
import com.wallet.core.primitives.Appearance
import uniffi.gemstone.GemGroup
import uniffi.gemstone.GemListRow

@Composable
internal fun PreferencesScene(sections: List<GemGroup>, appearance: Appearance, onAction: (PreferencesAction) -> Unit) {
    val context = LocalContext.current

    Scene(
        title = stringResource(id = (R.string.settings_preferences_title)),
        onClose = { onAction(PreferencesAction.Cancel) },
    ) {
        LazyColumn {
            sections.forEach { section ->
                // NOTE: Perpetuals and unmapped/null preference rows are hidden.
                val rows = section.rows
                val visibleRows = mutableListOf<GemListRow>()
                for (i in 0 until rows.size) {
                    val row = rows[i]
                    val action = row.preferencesAction()
                    if (action != null && action !is PreferencesRowAction.Perpetuals && action !is PreferencesRowAction.Option) {
                        visibleRows.add(row)
                    }
                }
                itemsPositioned(visibleRows) { position, row ->
                    when (val action = row.preferencesAction()) {
                        is PreferencesRowAction.Open -> GemListRowView(
                            row = row,
                            listPosition = position,
                            modifier = Modifier.clickable { onAction(action.action) },
                        )

                        PreferencesRowAction.Language -> GemListRowView(
                            row = row,
                            listPosition = position,
                            modifier = Modifier.clickable {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val intent = Intent(Settings.ACTION_APP_LOCALE_SETTINGS)
                                    intent.data = Uri.fromParts("package", context.packageName, null)
                                    context.startActivity(intent)
                                }
                            },
                        )

                        PreferencesRowAction.Appearance -> OptionPickerRow(
                            row = row,
                            listPosition = position,
                            current = appearance,
                            options = Appearance.entries,
                            label = { stringResource(it.stringRes()) },
                            onSelect = { onAction(PreferencesAction.SetAppearance(it)) },
                        )

                        else -> Unit
                    }
                }
            }
        }
    }
}
