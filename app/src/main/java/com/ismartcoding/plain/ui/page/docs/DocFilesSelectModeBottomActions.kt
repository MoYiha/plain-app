package com.ismartcoding.plain.ui.page.docs

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.ismartcoding.plain.helpers.ShareHelper
import com.ismartcoding.plain.ui.base.ActionButtons
import com.ismartcoding.plain.ui.base.IconDeleteButton
import com.ismartcoding.plain.ui.base.IconShareButton
import com.ismartcoding.plain.ui.base.PBottomAppBar
import com.ismartcoding.plain.ui.helpers.DialogHelper
import com.ismartcoding.plain.ui.models.DocsViewModel
import com.ismartcoding.plain.ui.models.exitSelectMode

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DocFilesSelectModeBottomActions(
    viewModel: DocsViewModel,
) {
    val context = LocalContext.current
    PBottomAppBar {
        ActionButtons {
            IconShareButton {
                ShareHelper.sharePaths(context, viewModel.selectedIds.toSet())
            }
            IconDeleteButton {
                DialogHelper.confirmToDelete {
                    viewModel.delete(viewModel.selectedIds.toSet())
                    viewModel.exitSelectMode()
                }
            }
        }
    }
}