package com.ismartcoding.plain.ui.page.file

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ismartcoding.lib.channel.Channel
import com.ismartcoding.lib.extensions.getFilenameFromPath
import com.ismartcoding.lib.helpers.CoroutinesHelper.withIO
import com.ismartcoding.plain.R
import com.ismartcoding.plain.enums.ActionSourceType
import com.ismartcoding.plain.features.ActionEvent
import com.ismartcoding.plain.features.Permission
import com.ismartcoding.plain.features.PermissionsResultEvent
import com.ismartcoding.plain.features.file.FileSystemHelper
import com.ismartcoding.plain.features.locale.LocaleHelper
import com.ismartcoding.plain.preference.FileSortByPreference
import com.ismartcoding.plain.preference.ShowHiddenFilesPreference
import com.ismartcoding.plain.ui.base.ActionButtonFolderKanban
import com.ismartcoding.plain.ui.base.ActionButtonMoreWithMenu
import com.ismartcoding.plain.ui.base.ActionButtonSearch
import com.ismartcoding.plain.ui.base.HorizontalSpace
import com.ismartcoding.plain.ui.base.NavigationBackIcon
import com.ismartcoding.plain.ui.base.NavigationCloseIcon
import com.ismartcoding.plain.ui.base.PDropdownMenuItem
import com.ismartcoding.plain.ui.base.PDropdownMenuItemCreateFile
import com.ismartcoding.plain.ui.base.PDropdownMenuItemCreateFolder
import com.ismartcoding.plain.ui.base.PDropdownMenuItemSelect
import com.ismartcoding.plain.ui.base.PDropdownMenuItemSort
import com.ismartcoding.plain.ui.base.PMiniOutlineButton
import com.ismartcoding.plain.ui.base.PScaffold
import com.ismartcoding.plain.ui.base.SearchableTopBar
import com.ismartcoding.plain.ui.base.TextFieldDialog
import com.ismartcoding.plain.ui.base.pullrefresh.PullToRefresh
import com.ismartcoding.plain.ui.base.pullrefresh.RefreshContentState
import com.ismartcoding.plain.ui.base.pullrefresh.rememberRefreshLayoutState
import com.ismartcoding.plain.ui.components.FileSortDialog
import com.ismartcoding.plain.ui.components.FolderKanbanDialog
import com.ismartcoding.plain.ui.components.mediaviewer.previewer.MediaPreviewer
import com.ismartcoding.plain.ui.components.mediaviewer.previewer.rememberPreviewerState
import com.ismartcoding.plain.ui.helpers.DialogHelper
import com.ismartcoding.plain.ui.models.BreadcrumbItem
import com.ismartcoding.plain.ui.models.DrawerMenuItemClickedEvent
import com.ismartcoding.plain.ui.models.FilesType
import com.ismartcoding.plain.ui.models.FilesViewModel
import com.ismartcoding.plain.ui.models.enterSearchMode
import com.ismartcoding.plain.ui.models.exitSearchMode
import com.ismartcoding.plain.ui.models.exitSelectMode
import com.ismartcoding.plain.ui.models.isAllSelected
import com.ismartcoding.plain.ui.models.showBottomActions
import com.ismartcoding.plain.ui.models.toggleSelectAll
import com.ismartcoding.plain.ui.models.toggleSelectMode
import com.ismartcoding.plain.ui.page.file.components.BreadcrumbView
import com.ismartcoding.plain.ui.page.file.components.FileListContent
import com.ismartcoding.plain.ui.page.file.components.FilePasteBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FilesPage(
    navController: NavHostController,
    fileType: FilesType = FilesType.INTERNAL_STORAGE,
    viewModel: FilesViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val previewerState = rememberPreviewerState()
    val sharedFlow = Channel.sharedFlow

    val itemsState by viewModel.itemsFlow.collectAsState()

    val topRefreshLayoutState = rememberRefreshLayoutState {
        scope.launch {
            withIO { viewModel.loadAsync(context) }
            setRefreshState(RefreshContentState.Finished)
        }
    }

    BackHandler(enabled = viewModel.selectMode.value || viewModel.showSearchBar.value || viewModel.showPasteBar.value) {
        when {
            viewModel.selectMode.value -> {
                viewModel.exitSelectMode()
            }

            viewModel.showSearchBar.value -> {
                viewModel.exitSearchMode()
                scope.launch {
                    viewModel.loadAsync(context)
                }
            }

            viewModel.showPasteBar.value -> {
                viewModel.cutFiles.clear()
                viewModel.copyFiles.clear()
                viewModel.showPasteBar.value = false
            }
        }
    }

    LaunchedEffect(fileType) {
        if (fileType == FilesType.APP) {
            viewModel.root = FileSystemHelper.getExternalFilesDirPath(context)
            viewModel.breadcrumbs.clear()
            viewModel.breadcrumbs.add(BreadcrumbItem(LocaleHelper.getString(R.string.file_transfer_assistant), viewModel.root))
            viewModel.path = viewModel.root
            viewModel.type = FilesType.APP
        }

        scope.launch {
            viewModel.loadAsync(context)
        }
    }

    LaunchedEffect(sharedFlow) {
        sharedFlow.collect { event ->
            when (event) {
                is PermissionsResultEvent -> {
                    scope.launch {
                        viewModel.loadAsync(context)
                    }
                }

                is DrawerMenuItemClickedEvent -> {
                    val m = event.model
                    viewModel.offset = 0
                    viewModel.root = m.data as String
                    viewModel.breadcrumbs.clear()
                    viewModel.breadcrumbs.add(BreadcrumbItem(m.title, viewModel.root))
                    viewModel.path = viewModel.root
                    viewModel.type = when (m.iconId) {
                        R.drawable.sd_card -> FilesType.SDCARD
                        R.drawable.usb -> FilesType.USB_STORAGE
                        R.drawable.app_icon -> FilesType.APP
                        R.drawable.history -> FilesType.RECENTS
                        else -> FilesType.INTERNAL_STORAGE
                    }

                    scope.launch {
                        viewModel.loadAsync(context)
                    }
                }

                is ActionEvent -> {
                    if (event.source == ActionSourceType.FILE) {
                        scope.launch {
                            viewModel.loadAsync(context)
                        }
                    }
                }
            }
        }
    }

    if (viewModel.showSortDialog.value) {
        FileSortDialog(viewModel.sortBy, onSelected = {
            scope.launch(Dispatchers.IO) {
                FileSortByPreference.putAsync(context, it)
                viewModel.sortBy.value = it
                viewModel.loadAsync(context)
            }
        }, onDismiss = {
            viewModel.showSortDialog.value = false
        })
    }

    if (viewModel.showCreateFolderDialog.value) {
        val folderNameValue = remember { mutableStateOf("") }
        TextFieldDialog(
            title = stringResource(id = R.string.create_folder),
            value = folderNameValue.value,
            placeholder = stringResource(id = R.string.name),
            onValueChange = { folderNameValue.value = it },
            onDismissRequest = { viewModel.showCreateFolderDialog.value = false },
            onConfirm = { name ->
                scope.launch {
                    DialogHelper.showLoading()
                    withIO { FileSystemHelper.createDirectory(viewModel.path + "/" + name) }
                    DialogHelper.hideLoading()
                    viewModel.loadAsync(context)
                    viewModel.showCreateFolderDialog.value = false
                }
            }
        )
    }

    if (viewModel.showCreateFileDialog.value) {
        val fileNameValue = remember { mutableStateOf("") }
        TextFieldDialog(
            title = stringResource(id = R.string.create_file),
            value = fileNameValue.value,
            placeholder = stringResource(id = R.string.name),
            onValueChange = { fileNameValue.value = it },
            onDismissRequest = { viewModel.showCreateFileDialog.value = false },
            onConfirm = { name ->
                scope.launch {
                    DialogHelper.showLoading()
                    withIO { FileSystemHelper.createFile(viewModel.path + "/" + name) }
                    DialogHelper.hideLoading()
                    viewModel.loadAsync(context)
                    viewModel.showCreateFileDialog.value = false
                }
            }
        )
    }

    if (viewModel.showFolderKanbanDialog.value) {
        FolderKanbanDialog(
            viewModel = viewModel,
            onDismiss = {
                viewModel.showFolderKanbanDialog.value = false
            }
        )
    }

    FileInfoBottomSheet(viewModel = viewModel)

    PScaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SearchableTopBar(
                navController = navController,
                viewModel = viewModel,
                scrollBehavior = scrollBehavior,
                title = when {
                    viewModel.selectMode.value -> LocaleHelper.getStringF(
                        R.string.x_selected,
                        "count",
                        viewModel.selectedIds.size
                    )

                    viewModel.type == FilesType.RECENTS -> stringResource(R.string.recents)
                    viewModel.path != viewModel.root -> viewModel.path.getFilenameFromPath()
                    else -> stringResource(R.string.files)
                },
                subtitle = if (!viewModel.selectMode.value) {
                    val foldersCount = itemsState.count { it.isDir }
                    val filesCount = itemsState.count { !it.isDir }
                    val strList = mutableListOf<String>()
                    if (foldersCount > 0) {
                        strList.add(LocaleHelper.getQuantityString(R.plurals.x_folders, foldersCount))
                    }
                    if (filesCount > 0) {
                        strList.add(LocaleHelper.getQuantityString(R.plurals.x_files, filesCount))
                    }
                    strList.joinToString(", ")
                } else "",
                navigationIcon = {
                    if (viewModel.selectMode.value) {
                        NavigationCloseIcon {
                            viewModel.exitSelectMode()
                        }
                    } else if (viewModel.path == viewModel.root) {
                        NavigationBackIcon {
                            navController.popBackStack()
                        }
                    } else {
                        NavigationBackIcon {
                            navigateTo(viewModel, viewModel.path.substringBeforeLast('/')) {
                                scope.launch {
                                    viewModel.loadAsync(context)
                                }
                            }
                        }
                    }
                },
                actions = {
                    if (!viewModel.selectMode.value) {
                        ActionButtonSearch {
                            viewModel.enterSearchMode()
                        }

                        ActionButtonFolderKanban {
                            viewModel.showFolderKanbanDialog.value = true
                        }

                        ActionButtonMoreWithMenu { dismiss ->
                            PDropdownMenuItemSelect(onClick = {
                                dismiss()
                                viewModel.toggleSelectMode()
                            })
                            PDropdownMenuItemSort(onClick = {
                                dismiss()
                                viewModel.showSortDialog.value = true
                            })

                            var showHiddenFiles by remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) {
                                showHiddenFiles = withContext(Dispatchers.IO) {
                                    ShowHiddenFilesPreference.getAsync(context)
                                }
                            }

                            PDropdownMenuItem(
                                text = { Text(stringResource(R.string.show_hidden_files)) },
                                leadingIcon = {
                                    Checkbox(
                                        checked = showHiddenFiles,
                                        onCheckedChange = null // handle in onClick
                                    )
                                },
                                onClick = {
                                    dismiss()
                                    scope.launch(Dispatchers.IO) {
                                        val newValue = !showHiddenFiles
                                        ShowHiddenFilesPreference.putAsync(context, newValue)
                                        showHiddenFiles = newValue
                                        viewModel.loadAsync(context)
                                    }
                                }
                            )

                            PDropdownMenuItemCreateFolder {
                                dismiss()
                                viewModel.showCreateFolderDialog.value = true
                            }

                            PDropdownMenuItemCreateFile {
                                dismiss()
                                viewModel.showCreateFileDialog.value = true
                            }
                        }
                    } else {
                        PMiniOutlineButton(
                            text = stringResource(if (viewModel.isAllSelected()) R.string.unselect_all else R.string.select_all),
                            onClick = {
                                viewModel.toggleSelectAll()
                            }
                        )
                        HorizontalSpace(dp = 8.dp)
                    }
                },
                onSearchAction = { query ->
                    viewModel.queryText.value = query
                    scope.launch {
                        viewModel.loadAsync(context)
                    }
                }
            )
        },
        bottomBar = {
            AnimatedVisibility(
                visible = viewModel.showBottomActions(),
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                FilesSelectModeBottomActions(
                    viewModel = viewModel,
                    onShowPasteBar = { viewModel.showPasteBar.value = it }
                )
            }

            AnimatedVisibility(
                visible = viewModel.showPasteBar.value,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                FilePasteBar(
                    viewModel = viewModel,
                    coroutineScope = scope,
                    onPasteComplete = {
                        scope.launch {
                            viewModel.loadAsync(context)
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (Permission.WRITE_EXTERNAL_STORAGE.can(context) && viewModel.type != FilesType.RECENTS) {
                BreadcrumbView(
                    breadcrumbs = viewModel.breadcrumbs,
                    selectedIndex = viewModel.selectedBreadcrumbIndex.value,
                    onItemClick = { item ->
                        navigateTo(viewModel, item.path) {
                            scope.launch {
                                viewModel.loadAsync(context)
                            }
                        }
                    }
                )
            }

            PullToRefresh(
                refreshLayoutState = topRefreshLayoutState,
            ) {
                FileListContent(
                    navController = navController,
                    viewModel = viewModel,
                    files = itemsState,
                    loadFiles = { _, _ ->
                        scope.launch {
                            viewModel.loadAsync(context)
                        }
                    },
                    previewerState = previewerState
                )
            }
        }
    }

    MediaPreviewer(
        state = previewerState
    )
}

fun navigateTo(
    viewModel: FilesViewModel,
    path: String,
    onComplete: () -> Unit
) {
    viewModel.path = path
    viewModel.getAndUpdateSelectedIndex()

    onComplete()
} 