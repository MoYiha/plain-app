package com.ismartcoding.plain.ui.page.chat

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.ismartcoding.plain.ui.base.ActionButtons
import com.ismartcoding.plain.ui.base.IconDeleteButton
import com.ismartcoding.plain.ui.base.PBottomAppBar
import com.ismartcoding.plain.ui.models.ChatViewModel
import com.ismartcoding.plain.ui.models.exitSelectMode

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChatSelectModeBottomActions(
    viewModel: ChatViewModel,
) {
    val context = LocalContext.current

    PBottomAppBar {
        ActionButtons {
            IconDeleteButton {
                viewModel.delete(context, viewModel.selectedIds.toSet())
                viewModel.exitSelectMode()
            }
        }
    }
}