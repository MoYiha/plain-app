package com.ismartcoding.plain.ui.page.audio

import android.content.ClipData
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ismartcoding.lib.extensions.formatBytes
import com.ismartcoding.lib.extensions.formatDuration
import com.ismartcoding.lib.extensions.getMimeType
import com.ismartcoding.lib.extensions.isUrl
import com.ismartcoding.plain.R
import com.ismartcoding.plain.clipboardManager
import com.ismartcoding.plain.db.DTag
import com.ismartcoding.plain.db.DTagRelation
import com.ismartcoding.plain.extensions.formatDateTime
import com.ismartcoding.plain.features.locale.LocaleHelper
import com.ismartcoding.plain.features.media.AudioMediaStoreHelper
import com.ismartcoding.plain.helpers.ShareHelper
import com.ismartcoding.plain.ui.base.ActionButtons
import com.ismartcoding.plain.ui.base.BottomSpace
import com.ismartcoding.plain.ui.base.IconTextDeleteButton
import com.ismartcoding.plain.ui.base.PCard
import com.ismartcoding.plain.ui.base.PIconButton
import com.ismartcoding.plain.ui.base.PListItem
import com.ismartcoding.plain.ui.base.PModalBottomSheet
import com.ismartcoding.plain.ui.base.Subtitle
import com.ismartcoding.plain.ui.base.VerticalSpace
import com.ismartcoding.plain.ui.base.dragselect.DragSelectState
import com.ismartcoding.plain.ui.components.FileRenameDialog
import com.ismartcoding.plain.ui.components.TagSelector
import com.ismartcoding.plain.ui.helpers.DialogHelper
import com.ismartcoding.plain.ui.models.AudioViewModel
import com.ismartcoding.plain.ui.models.TagsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.ismartcoding.plain.ui.base.IconTextSelectButton
import com.ismartcoding.plain.ui.base.IconTextShareButton
import com.ismartcoding.plain.ui.base.IconTextOpenWithButton
import com.ismartcoding.plain.ui.base.IconTextRenameButton


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewAudioBottomSheet(
    viewModel: AudioViewModel,
    tagsViewModel: TagsViewModel,
    tagsMapState: Map<String, List<DTagRelation>>,
    tagsState: List<DTag>,
    dragSelectState: DragSelectState,
) {
    val m = viewModel.selectedItem.value ?: return
    val context = LocalContext.current
    val onDismiss = {
        viewModel.selectedItem.value = null
    }

    val scope = rememberCoroutineScope()

    if (viewModel.showRenameDialog.value) {
        FileRenameDialog(path = m.path, onDismiss = {
            viewModel.showRenameDialog.value = false
        }, onDone = {
            scope.launch(Dispatchers.IO) { viewModel.loadAsync(context, tagsViewModel) }
            onDismiss()
        })
    }

    PModalBottomSheet(
        onDismissRequest = {
            onDismiss()
        },
    ) {
        LazyColumn {
            item {
                VerticalSpace(32.dp)
            }
            item {
                ActionButtons {
                    if (!viewModel.showSearchBar.value) {
                        IconTextSelectButton {
                            dragSelectState.enterSelectMode()
                            dragSelectState.select(m.id)
                            onDismiss()
                        }
                    }
                    IconTextShareButton {
                        ShareHelper.shareUris(context, listOf(AudioMediaStoreHelper.getItemUri(m.id)))
                        onDismiss()
                    }
                    if (!m.path.isUrl()) {
                        IconTextOpenWithButton {
                            ShareHelper.openPathWith(context, m.path)
                        }
                    }
                    IconTextRenameButton {
                        viewModel.showRenameDialog.value = true
                    }
                    IconTextDeleteButton {
                        DialogHelper.confirmToDelete {
                            viewModel.delete(context, tagsViewModel, setOf(m.id))
                            onDismiss()
                        }
                    }
                }
            }
            if (!viewModel.trash.value) {
                item {
                    VerticalSpace(dp = 16.dp)
                    Subtitle(text = stringResource(id = R.string.tags))
                    TagSelector(
                        data = m,
                        tagsViewModel = tagsViewModel,
                        tagsMap = tagsMapState,
                        tagsState = tagsState,
                        onChanged = {
                            scope.launch(Dispatchers.IO) {
                                viewModel.refreshTabsAsync(context, tagsViewModel)
                            }
                        }
                    )
                }
            }
            item {
                VerticalSpace(dp = 16.dp)
                PCard {
                    PListItem(title = m.path, action = {
                        PIconButton(icon = R.drawable.copy, contentDescription = stringResource(id = R.string.copy_path), click = {
                            val clip = ClipData.newPlainText(LocaleHelper.getString(R.string.file_path), m.path)
                            clipboardManager.setPrimaryClip(clip)
                            DialogHelper.showTextCopiedMessage(m.path)
                        })
                    })
                }
            }
            item {
                VerticalSpace(dp = 16.dp)
                PCard {
                    PListItem(title = stringResource(id = R.string.file_size), value = m.size.formatBytes())
                    PListItem(title = stringResource(id = R.string.type), value = m.path.getMimeType())
                    PListItem(title = stringResource(id = R.string.duration), value = m.duration.formatDuration())
                    PListItem(title = stringResource(id = R.string.created_at), value = m.createdAt.formatDateTime())
                    PListItem(title = stringResource(id = R.string.updated_at), value = m.updatedAt.formatDateTime())
                }
            }
            item {
                BottomSpace()
            }
        }
    }
}


