package com.ismartcoding.plain.ui.page.feeds

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import com.ismartcoding.plain.ui.base.ActionButtons
import com.ismartcoding.plain.ui.base.IconDeleteButton
import com.ismartcoding.plain.ui.base.PBottomAppBar
import com.ismartcoding.plain.ui.models.FeedsViewModel
import com.ismartcoding.plain.ui.models.exitSelectMode

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FeedsSelectModeBottomActions(
    viewModel: FeedsViewModel,
) {
    PBottomAppBar {
        ActionButtons {
            IconDeleteButton {
                viewModel.delete(viewModel.selectedIds.toSet())
                viewModel.exitSelectMode()
            }
        }
    }
}