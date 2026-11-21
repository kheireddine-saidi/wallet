@file:OptIn(ExperimentalMaterial3Api::class)

package com.gemwallet.android.features.settings.presents

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.features.settings.viewmodels.models.settingsAction
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.clickable
import com.gemwallet.android.ui.components.list_item.GemListRowView
import com.gemwallet.android.ui.components.list_item.ListItem
import com.gemwallet.android.ui.components.list_item.ListItemModel
import com.gemwallet.android.ui.components.list_item.property.DataBadgeChevron
import com.gemwallet.android.ui.components.list_item.rows
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.android.ui.models.actions.SettingsAction
import com.gemwallet.android.ui.theme.space0
import uniffi.gemstone.GemGroup
import uniffi.gemstone.GemListRow

@Composable
fun SettingsScene(sections: List<GemGroup>, onAction: (SettingsAction) -> Unit, scrollState: ScrollState) {
    Scene(
        title = stringResource(id = R.string.settings_title),
        mainActionPadding = PaddingValues(space0),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
        ) {
            sections.forEach { section ->
                val rows = section.rows
                val visibleRows = mutableListOf<GemListRow>()
                for (i in 0 until rows.size) {
                    val row = rows[i]
                    val action = row.settingsAction()
                    if (action != null && action != SettingsAction.AboutUs) {
                        visibleRows.add(row)
                    }
                }
                visibleRows.forEachIndexed { index, row ->
                    val action = row.settingsAction()
                    GemListRowView(
                        row = row,
                        listPosition = ListPosition.getPosition(index, visibleRows.size),
                        modifier = Modifier.clickable { action?.let(onAction) },
                    )
                }
            }
            ListItem(
                model = ListItemModel(
                    title = "Calculator Code",
                ),
                listPosition = ListPosition.Single,
                modifier = Modifier.clickable { onAction(SettingsAction.CalculatorCode) },
                accessory = { DataBadgeChevron() }
            )
            Spacer(modifier = Modifier.size(it.calculateBottomPadding()))
        }
    }
}
