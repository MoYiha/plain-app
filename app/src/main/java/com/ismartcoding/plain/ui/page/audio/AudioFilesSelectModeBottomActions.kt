package com.ismartcoding.plain.ui.page.audio

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.ismartcoding.lib.helpers.CoroutinesHelper.withIO
import com.ismartcoding.plain.R
import com.ismartcoding.plain.db.DTag
import com.ismartcoding.plain.features.media.AudioMediaStoreHelper
import com.ismartcoding.plain.helpers.ShareHelper
import com.ismartcoding.plain.preference.AudioPlaylistPreference
import com.ismartcoding.plain.ui.base.ActionButtons
import com.ismartcoding.plain.ui.base.IconDeleteButton
import com.ismartcoding.plain.ui.base.IconLabelButton
import com.ismartcoding.plain.ui.base.IconLabelOffButton
import com.ismartcoding.plain.ui.base.IconPlaylistAddButton
import com.ismartcoding.plain.ui.base.IconShareButton
import com.ismartcoding.plain.ui.base.PBottomAppBar
import com.ismartcoding.plain.ui.base.dragselect.DragSelectState
import com.ismartcoding.plain.ui.helpers.DialogHelper
import com.ismartcoding.plain.ui.models.AudioViewModel
import com.ismartcoding.plain.ui.models.TagsViewModel
import com.ismartcoding.plain.ui.page.tags.BatchSelectTagsDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AudioFilesSelectModeBottomActions(
    viewModel: AudioViewModel,
    tagsViewModel: TagsViewModel,
    tagsState: List<DTag>,
    dragSelectState: DragSelectState,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showSelectTagsDialog by remember { mutableStateOf(false) }
    var removeFromTags by remember { mutableStateOf(false) }

    if (showSelectTagsDialog) {
        val selectedIds = dragSelectState.selectedIds
        val selectedItems = viewModel.itemsFlow.value.filter { selectedIds.contains(it.id) }
        BatchSelectTagsDialog(tagsViewModel, tagsState, selectedItems, removeFromTags) {
            showSelectTagsDialog = false
            dragSelectState.exitSelectMode()
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
            IconPlaylistAddButton {
                scope.launch {
                    val selectedIds = dragSelectState.selectedIds
                    val selectedItems = viewModel.itemsFlow.value.filter { selectedIds.contains(it.id) }
                    withIO {
                        AudioPlaylistPreference.addAsync(context, selectedItems.map { it.toPlaylistAudio() })
                    }
                    dragSelectState.exitSelectMode()
                    DialogHelper.showMessage(R.string.added_to_playlist)
                }
            }
            IconShareButton {
                ShareHelper.shareUris(context, dragSelectState.selectedIds.map { AudioMediaStoreHelper.getItemUri(it) })
            }
            IconDeleteButton {
                DialogHelper.confirmToDelete {
                    viewModel.delete(context, tagsViewModel, dragSelectState.selectedIds.toSet())
                    dragSelectState.exitSelectMode()
                }
            }
        }
    }
} 