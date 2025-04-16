package com.ismartcoding.plain.ui.page.videos

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ismartcoding.lib.channel.receiveEventHandler
import com.ismartcoding.lib.extensions.isGestureInteractionMode
import com.ismartcoding.lib.helpers.CoroutinesHelper.withIO
import com.ismartcoding.plain.R
import com.ismartcoding.plain.enums.AppFeatureType
import com.ismartcoding.plain.features.PermissionsResultEvent
import com.ismartcoding.plain.preference.VideoGridCellsPerRowPreference
import com.ismartcoding.plain.preference.VideoSortByPreference
import com.ismartcoding.plain.ui.base.NeedPermissionColumn
import com.ismartcoding.plain.ui.base.NoDataColumn
import com.ismartcoding.plain.ui.base.PFilterChip
import com.ismartcoding.plain.ui.base.PScaffold
import com.ismartcoding.plain.ui.base.RadioDialog
import com.ismartcoding.plain.ui.base.RadioDialogOption
import com.ismartcoding.plain.ui.base.VerticalSpace
import com.ismartcoding.plain.ui.base.dragselect.gridDragSelect
import com.ismartcoding.plain.ui.base.fastscroll.LazyVerticalGridScrollbar
import com.ismartcoding.plain.ui.base.pullrefresh.LoadMoreRefreshContent
import com.ismartcoding.plain.ui.base.pullrefresh.PullToRefresh
import com.ismartcoding.plain.ui.base.pullrefresh.RefreshContentState
import com.ismartcoding.plain.ui.base.pullrefresh.rememberRefreshLayoutState
import com.ismartcoding.plain.ui.base.tabs.PScrollableTabRow
import com.ismartcoding.plain.ui.components.CastDialog
import com.ismartcoding.plain.ui.components.FileSortDialog
import com.ismartcoding.plain.ui.components.VideoGridItem
import com.ismartcoding.plain.ui.components.mediaviewer.previewer.MediaPreviewer
import com.ismartcoding.plain.ui.extensions.reset
import com.ismartcoding.plain.ui.models.CastViewModel
import com.ismartcoding.plain.ui.models.MediaFoldersViewModel
import com.ismartcoding.plain.ui.models.TagsViewModel
import com.ismartcoding.plain.ui.models.VideosViewModel
import com.ismartcoding.plain.ui.models.exitSearchMode
import com.ismartcoding.plain.ui.page.root.TopBarVideos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun VideosPage(
    navController: NavHostController,
    bucketId: String,
    viewModel: VideosViewModel = viewModel(),
    tagsViewModel: TagsViewModel = viewModel(),
    bucketViewModel: MediaFoldersViewModel = viewModel(),
    castViewModel: CastViewModel = viewModel(),
) {
    val context = LocalContext.current
    val view = LocalView.current
    val window = (view.context as Activity).window
    val videosState = VideosPageState.create(
        videosViewModel = viewModel,
        tagsViewModel = tagsViewModel,
        bucketViewModel = bucketViewModel,
    )
    val pagerState = videosState.pagerState
    val scrollBehavior = videosState.scrollBehavior
    val tagsState = videosState.tagsState
    val previewerState = videosState.previewerState
    val tagsMapState = videosState.tagsMapState
    val dragSelectState = videosState.dragSelectState
    val itemsState = videosState.itemsState
    val cellsPerRow = videosState.cellsPerRow
    val bucketsMap = videosState.bucketsMap

    val configuration = LocalConfiguration.current
    val scope = rememberCoroutineScope()
    var isFirstTime by remember { mutableStateOf(true) }

    val density = LocalDensity.current
    val imageWidthPx = remember(cellsPerRow.value) {
        density.run { ((configuration.screenWidthDp.dp - ((cellsPerRow.value - 1) * 2).dp) / cellsPerRow.value).toPx().toInt() }
    }

    val events = remember { mutableStateListOf<Job>() }

    val topRefreshLayoutState =
        rememberRefreshLayoutState {
            scope.launch {
                withIO {
                    viewModel.loadAsync(context, tagsViewModel)
                    bucketViewModel.loadAsync(context)
                }
                setRefreshState(RefreshContentState.Finished)
            }
        }

    val once = rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!once.value) {
            once.value = true
            viewModel.hasPermission.value = AppFeatureType.FILES.hasPermission(context)
            viewModel.bucketId.value = bucketId
            bucketViewModel.dataType.value = viewModel.dataType
            tagsViewModel.dataType.value = viewModel.dataType
            if (viewModel.hasPermission.value) {
                scope.launch(Dispatchers.IO) {
                    cellsPerRow.value = VideoGridCellsPerRowPreference.getAsync(context)
                    viewModel.sortBy.value = VideoSortByPreference.getValueAsync(context)
                    viewModel.loadAsync(context, tagsViewModel)
                    bucketViewModel.loadAsync(context)
                }
            }
        } else {
            // refresh tabs in case tag name changed in tags page
            scope.launch(Dispatchers.IO) {
                viewModel.refreshTabsAsync(context, tagsViewModel)
            }
        }
        events.add(
            receiveEventHandler<PermissionsResultEvent> {
                viewModel.hasPermission.value = AppFeatureType.FILES.hasPermission(context)
                scope.launch(Dispatchers.IO) {
                    viewModel.sortBy.value = VideoSortByPreference.getValueAsync(context)
                    viewModel.loadAsync(context, tagsViewModel)
                }
            })
    }

    val insetsController = WindowCompat.getInsetsController(window, view)
    LaunchedEffect(dragSelectState.selectMode, (previewerState.visible && !context.isGestureInteractionMode())) {
        if (dragSelectState.selectMode || (previewerState.visible && !context.isGestureInteractionMode())) {
            scrollBehavior.reset()
            insetsController.hide(WindowInsetsCompat.Type.navigationBars())
        } else {
            insetsController.show(WindowInsetsCompat.Type.navigationBars())
        }
    }

    val onSearch: (String) -> Unit = {
        viewModel.searchActive.value = false
        viewModel.showLoading.value = true
        scope.launch(Dispatchers.IO) {
            viewModel.loadAsync(context, tagsViewModel)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            insetsController.show(WindowInsetsCompat.Type.navigationBars())
            events.forEach { it.cancel() }
            events.clear()
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (isFirstTime) {
            isFirstTime = false
            return@LaunchedEffect
        }
        val tab = viewModel.tabs.value.getOrNull(pagerState.currentPage) ?: return@LaunchedEffect
        if (tab.value == "all") {
            viewModel.trash.value = false
            viewModel.tag.value = null
        } else {
            val tag = tagsState.find { it.id == tab.value }
            viewModel.trash.value = false
            viewModel.tag.value = tag
        }
        scope.launch {
            scrollBehavior.reset()
            viewModel.scrollStateMap[pagerState.currentPage]?.scrollToItem(0)
        }
        scope.launch(Dispatchers.IO) {
            viewModel.loadAsync(context, tagsViewModel)
        }
    }

    BackHandler {
        if (previewerState.visible) {
            scope.launch {
                previewerState.closeTransform()
            }
        } else if (dragSelectState.selectMode) {
            dragSelectState.exitSelectMode()
        } else if (castViewModel.castMode.value) {
            castViewModel.exitCastMode()
        } else if (viewModel.showSearchBar.value) {
            if (!viewModel.searchActive.value || viewModel.queryText.value.isEmpty()) {
                viewModel.exitSearchMode()
                onSearch("")
            }
        } else {
            navController.popBackStack()
        }
    }

    if (viewModel.showCellsPerRowDialog.value) {
        RadioDialog(
            title = stringResource(R.string.cells_per_row),
            options = IntRange(2, 10).map { value ->
                RadioDialogOption(
                    text = value.toString(),
                    selected = value == cellsPerRow.value,
                ) {
                    scope.launch(Dispatchers.IO) {
                        VideoGridCellsPerRowPreference.putAsync(context, value)
                        cellsPerRow.value = value
                    }
                }
            },
        ) {
            viewModel.showCellsPerRowDialog.value = false
        }
    }

    ViewVideoBottomSheet(viewModel, tagsViewModel, tagsMapState, tagsState, dragSelectState)

    if (viewModel.showSortDialog.value) {
        FileSortDialog(viewModel.sortBy, onSelected = {
            scope.launch(Dispatchers.IO) {
                VideoSortByPreference.putAsync(context, it)
                viewModel.sortBy.value = it
                viewModel.loadAsync(context, tagsViewModel)
            }
        }, onDismiss = {
            viewModel.showSortDialog.value = false
        })
    }

    CastDialog(castViewModel)

    PScaffold(
        topBar = {
            TopBarVideos(
                navController = navController,
                bucketId = bucketId,
                videosState = videosState,
                videosViewModel = viewModel,
                tagsViewModel = tagsViewModel,
                castViewModel = castViewModel,
            )
        },
        bottomBar = {
            AnimatedVisibility(
                visible = dragSelectState.showBottomActions(),
                enter = slideInVertically { it },
                exit = slideOutVertically { it }) {
                VideoFilesSelectModeBottomActions(viewModel, tagsViewModel, tagsState, dragSelectState)
            }
        },
    ) { paddingValues ->
        if (!viewModel.hasPermission.value) {
            NeedPermissionColumn(R.drawable.video, AppFeatureType.FILES.getPermission()!!)
            return@PScaffold
        }

        if (!dragSelectState.selectMode) {
            PScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                viewModel.tabs.value.forEachIndexed { index, s ->
                    PFilterChip(
                        modifier = Modifier.padding(start = if (index == 0) 0.dp else 8.dp),
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch {
                                pagerState.scrollToPage(index)
                            }
                        },
                        label = {
                            if (index == 0) {
                                Text(text = s.title + " (" + s.count + ")")
                            } else {
                                Text(if (viewModel.bucketId.value.isNotEmpty() || viewModel.queryText.value.isNotEmpty()) s.title else "${s.title} (${s.count})")
                            }
                        }
                    )
                }
            }
        }

        if (pagerState.pageCount == 0) {
            NoDataColumn(loading = viewModel.showLoading.value, search = viewModel.showSearchBar.value)
            return@PScaffold
        }

        HorizontalPager(state = pagerState) { index ->
            PullToRefresh(
                refreshLayoutState = topRefreshLayoutState,
            ) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    if (itemsState.isNotEmpty()) {
                        val scrollState = rememberLazyGridState()
                        viewModel.scrollStateMap[index] = scrollState
                        LazyVerticalGridScrollbar(
                            state = scrollState,
                        ) {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(cellsPerRow.value),
                                state = scrollState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                                    .gridDragSelect(
                                        items = itemsState,
                                        state = dragSelectState,
                                    ),
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                items(itemsState,
                                    key = {
                                        it.id
                                    },
                                    contentType = {
                                        "video"
                                    },
                                    span = {
                                        GridItemSpan(1)
                                    }) { m ->
                                    VideoGridItem(
                                        modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null),
                                        viewModel,
                                        castViewModel,
                                        m,
                                        showSize = cellsPerRow.value < 6,
                                        previewerState,
                                        dragSelectState,
                                        imageWidthPx,
                                        sort = viewModel.sortBy.value,
                                    )
                                }
                                item(
                                    span = { GridItemSpan(maxLineSpan) },
                                    key = "loadMore"
                                ) {
                                    if (itemsState.isNotEmpty() && !viewModel.noMore.value) {
                                        LaunchedEffect(Unit) {
                                            scope.launch(Dispatchers.IO) {
                                                withIO { viewModel.moreAsync(context, tagsViewModel) }
                                            }
                                        }
                                    }
                                    LoadMoreRefreshContent(viewModel.noMore.value)
                                }
                                item(
                                    span = { GridItemSpan(maxLineSpan) },
                                    key = "bottomSpace"
                                ) {
                                    VerticalSpace(dp = paddingValues.calculateBottomPadding())
                                }
                            }
                        }
                    } else {
                        NoDataColumn(loading = viewModel.showLoading.value, search = viewModel.showSearchBar.value)
                    }
                }
            }
        }
    }
    MediaPreviewer(
        state = previewerState,
        tagsViewModel = tagsViewModel,
        tagsMap = tagsMapState,
        tagsState = tagsState,
        onRenamed = {
            scope.launch(Dispatchers.IO) {
                viewModel.loadAsync(context, tagsViewModel)
            }
        },
        deleteAction = { item ->
            scope.launch(Dispatchers.IO) {
                viewModel.delete(context, tagsViewModel, setOf(item.mediaId))
                previewerState.closeTransform()
            }
        },
        onTagsChanged = {
            scope.launch(Dispatchers.IO) {
                viewModel.loadAsync(context, tagsViewModel)
            }
        },
    )
}
