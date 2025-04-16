package com.ismartcoding.plain.ui.base

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.ismartcoding.plain.R
import com.ismartcoding.plain.data.DMediaBucket
import com.ismartcoding.plain.data.IData
import com.ismartcoding.plain.enums.DataType
import com.ismartcoding.plain.features.locale.LocaleHelper
import com.ismartcoding.plain.features.media.CastPlayer
import com.ismartcoding.plain.ui.base.dragselect.DragSelectState
import com.ismartcoding.plain.ui.models.CastViewModel
import com.ismartcoding.plain.ui.models.IMediaSearchableViewModel
import com.ismartcoding.plain.ui.models.TagsViewModel
import com.ismartcoding.plain.ui.models.enterSearchMode
import com.ismartcoding.plain.ui.nav.navigateMediaFolders
import com.ismartcoding.plain.ui.nav.navigateTags

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun <T : IData> MediaTopBar(
    navController: NavHostController,
    bucketId: String,
    viewModel: IMediaSearchableViewModel<T>,
    tagsViewModel: TagsViewModel,
    castViewModel: CastViewModel,
    dragSelectState: DragSelectState,
    scrollBehavior: TopAppBarScrollBehavior,
    bucketsMap: Map<String, DMediaBucket>,
    itemsState: List<T>,
    currentPage: Int,
    scrollToTop: (Int) -> Unit,
    showCellsPerRowDialog: Boolean = true,
    onCellsPerRowClick: (() -> Unit)? = null,
    onSearchAction: (context: android.content.Context, tagsViewModel: TagsViewModel) -> Unit
) {
    val context = LocalContext.current
    
    val title = getMediaPageTitle(viewModel.dataType, castViewModel, bucket = bucketsMap[bucketId], dragSelectState, viewModel.tag, viewModel.trash)
    
    SearchableTopBar(
        navController = navController,
        viewModel = viewModel,
        scrollBehavior = scrollBehavior,
        title = title,
        currentPage = currentPage,
        scrollToTop = scrollToTop,
        navigationIcon = {
            if (dragSelectState.selectMode) {
                NavigationCloseIcon {
                    dragSelectState.exitSelectMode()
                }
            } else if (castViewModel.castMode.value) {
                NavigationCloseIcon {
                    castViewModel.exitCastMode()
                }
            } else {
                if (bucketId != "") {
                    NavigationBackIcon {
                        navController.popBackStack()
                    }
                }
            }
        },
        actions = {
            if (!viewModel.hasPermission.value) {
                return@SearchableTopBar
            }
            if (castViewModel.castMode.value) {
                return@SearchableTopBar
            }
            if (dragSelectState.selectMode) {
                PMiniOutlineButton(
                    text = stringResource(if (dragSelectState.isAllSelected(itemsState)) R.string.unselect_all else R.string.select_all),
                    onClick = {
                        dragSelectState.toggleSelectAll(itemsState)
                    },
                )
                HorizontalSpace(dp = 8.dp)
            } else {
                ActionButtonSearch {
                    viewModel.enterSearchMode()
                }
                if (viewModel.bucketId.value.isEmpty()) {
                    PIconButton(
                        icon = Icons.Outlined.Folder,
                        contentDescription = stringResource(R.string.folders),
                        tint = MaterialTheme.colorScheme.onSurface,
                    ) {
                        navController.navigateMediaFolders(viewModel.dataType)
                    }
                }
                ActionButtonMoreWithMenu { dismiss ->
                    PDropdownMenuItemSelect(onClick = {
                        dismiss()
                        dragSelectState.enterSelectMode()
                    })
                    PDropdownMenuItemTags(onClick = {
                        dismiss()
                        navController.navigateTags(viewModel.dataType)
                    })
                    PDropdownMenuItemSort(onClick = {
                        dismiss()
                        viewModel.showSortDialog.value = true
                    })
                    PDropdownMenuItemCast(onClick = {
                        dismiss()
                        castViewModel.showCastDialog.value = true
                    })
                    if (showCellsPerRowDialog && onCellsPerRowClick != null) {
                        PDropdownMenuItemCellsPerRow(onClick = {
                            dismiss()
                            onCellsPerRowClick()
                        })
                    }
                }
            }
        },
        onSearchAction = {
            viewModel.showLoading.value = true
            onSearchAction(context, tagsViewModel)
        }
    )
}

@Composable
private fun getMediaPageTitle(
    mediaType: DataType,
    castViewModel: CastViewModel,
    bucket: DMediaBucket?,
    dragSelectState: DragSelectState,
    tag: MutableState<com.ismartcoding.plain.db.DTag?>,
    trash: MutableState<Boolean>
): String {
    val resourceId = when (mediaType) {
        DataType.IMAGE -> R.string.images
        DataType.VIDEO -> R.string.videos
        DataType.AUDIO -> R.string.audios
        else -> R.string.files
    }
    
    val mediaName = bucket?.name ?: stringResource(id = resourceId)
    return if (castViewModel.castMode.value) {
        stringResource(id = R.string.cast_mode) + " - " + CastPlayer.currentDevice?.description?.device?.friendlyName
    } else if (dragSelectState.selectMode) {
        LocaleHelper.getStringF(R.string.x_selected, "count", dragSelectState.selectedIds.size)
    } else if (tag.value != null) {
        mediaName + " - " + tag.value!!.name
    } else if (trash.value) {
        stringResource(id = resourceId) + " - " + stringResource(id = R.string.trash)
    } else {
        mediaName
    }
} 