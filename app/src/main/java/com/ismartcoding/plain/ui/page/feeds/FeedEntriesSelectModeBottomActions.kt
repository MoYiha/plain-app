package com.ismartcoding.plain.ui.page.feeds

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.ismartcoding.plain.db.DTag
import com.ismartcoding.plain.ui.base.ActionButtons
import com.ismartcoding.plain.ui.base.IconDeleteButton
import com.ismartcoding.plain.ui.base.IconLabelButton
import com.ismartcoding.plain.ui.base.IconLabelOffButton
import com.ismartcoding.plain.ui.base.PBottomAppBar
import com.ismartcoding.plain.ui.models.FeedEntriesViewModel
import com.ismartcoding.plain.ui.models.TagsViewModel
import com.ismartcoding.plain.ui.models.exitSelectMode
import com.ismartcoding.plain.ui.models.getSelectedItems
import com.ismartcoding.plain.ui.page.tags.BatchSelectTagsDialog

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FeedEntriesSelectModeBottomActions(
    viewModel: FeedEntriesViewModel,
    tagsViewModel: TagsViewModel,
    tagsState: List<DTag>,
) {
    var showSelectTagsDialog by remember {
        mutableStateOf(false)
    }
    var removeFromTags by remember {
        mutableStateOf(false)
    }

    if (showSelectTagsDialog) {
        BatchSelectTagsDialog(tagsViewModel, tagsState, viewModel.getSelectedItems(), removeFromTags) {
            showSelectTagsDialog = false
            viewModel.exitSelectMode()
        }
    }

    PBottomAppBar {
        ActionButtons {
            IconLabelButton {
                showSelectTagsDialog = true
                removeFromTags = false
            }
            IconLabelOffButton {
                showSelectTagsDialog = true
                removeFromTags = true
            }
            IconDeleteButton {
                viewModel.delete(tagsViewModel, viewModel.selectedIds.toSet())
                viewModel.exitSelectMode()
            }
        }
    }
}