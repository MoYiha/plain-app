package com.ismartcoding.plain.ui.page.audio

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.automirrored.outlined.LabelOff
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ismartcoding.lib.helpers.CoroutinesHelper.withIO
import com.ismartcoding.plain.R
import com.ismartcoding.plain.db.DTag
import com.ismartcoding.plain.features.locale.LocaleHelper
import com.ismartcoding.plain.features.media.AudioMediaStoreHelper
import com.ismartcoding.plain.helpers.ShareHelper
import com.ismartcoding.plain.preference.AudioPlaylistPreference
import com.ismartcoding.plain.ui.base.ActionButtons
import com.ismartcoding.plain.ui.base.PIconTextActionButton
import com.ismartcoding.plain.ui.base.dragselect.DragSelectState
import com.ismartcoding.plain.ui.extensions.collectAsStateValue
import com.ismartcoding.plain.ui.helpers.DialogHelper
import com.ismartcoding.plain.ui.models.AudioViewModel
import com.ismartcoding.plain.ui.models.TagsViewModel
import com.ismartcoding.plain.ui.page.tags.BatchSelectTagsDialog
import com.ismartcoding.plain.ui.theme.bottomAppBarContainer
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
    
    BottomAppBar(
        modifier = Modifier.height(120.dp),
        tonalElevation = 0.dp,
        containerColor = MaterialTheme.colorScheme.bottomAppBarContainer(),
    ) {
        ActionButtons {
            PIconTextActionButton(
                icon = Icons.AutoMirrored.Outlined.Label,
                text = LocaleHelper.getString(R.string.add_to_tags),
                click = {
                    showSelectTagsDialog = true
                    removeFromTags = false
                }
            )
            PIconTextActionButton(
                icon = Icons.AutoMirrored.Outlined.LabelOff,
                text = LocaleHelper.getString(R.string.remove_from_tags),
                click = {
                    showSelectTagsDialog = true
                    removeFromTags = true
                }
            )
            PIconTextActionButton(
                icon = painterResource(R.drawable.list_plus),
                text = LocaleHelper.getString(R.string.add_to_playlist),
                click = {
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
            )
            PIconTextActionButton(
                icon = Icons.Outlined.Share,
                text = LocaleHelper.getString(R.string.share),
                click = {
                    ShareHelper.shareUris(context, dragSelectState.selectedIds.map { AudioMediaStoreHelper.getItemUri(it) })
                }
            )
            PIconTextActionButton(
                icon = Icons.Outlined.DeleteForever,
                text = LocaleHelper.getString(R.string.delete),
                click = {
                    DialogHelper.confirmToDelete {
                        viewModel.delete(context, tagsViewModel, dragSelectState.selectedIds.toSet())
                        dragSelectState.exitSelectMode()
                    }
                }
            )
        }
    }
} 