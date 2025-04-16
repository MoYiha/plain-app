package com.ismartcoding.plain.ui.page.root

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.navigation.NavHostController
import com.ismartcoding.lib.channel.receiveEventHandler
import com.ismartcoding.lib.extensions.isGestureInteractionMode
import com.ismartcoding.lib.helpers.CoroutinesHelper.withIO
import com.ismartcoding.plain.R
import com.ismartcoding.plain.enums.AppFeatureType
import com.ismartcoding.plain.features.PermissionsResultEvent
import com.ismartcoding.plain.preference.ImageGridCellsPerRowPreference
import com.ismartcoding.plain.preference.ImageSortByPreference
import com.ismartcoding.plain.ui.base.BottomSpace
import com.ismartcoding.plain.ui.base.NeedPermissionColumn
import com.ismartcoding.plain.ui.base.NoDataColumn
import com.ismartcoding.plain.ui.base.PFilterChip
import com.ismartcoding.plain.ui.base.RadioDialog
import com.ismartcoding.plain.ui.base.RadioDialogOption
import com.ismartcoding.plain.ui.base.dragselect.gridDragSelect
import com.ismartcoding.plain.ui.base.fastscroll.LazyVerticalGridScrollbar
import com.ismartcoding.plain.ui.base.pullrefresh.LoadMoreRefreshContent
import com.ismartcoding.plain.ui.base.pullrefresh.PullToRefresh
import com.ismartcoding.plain.ui.base.pullrefresh.RefreshContentState
import com.ismartcoding.plain.ui.base.pullrefresh.rememberRefreshLayoutState
import com.ismartcoding.plain.ui.base.tabs.PScrollableTabRow
import com.ismartcoding.plain.ui.components.CastDialog
import com.ismartcoding.plain.ui.components.FileSortDialog
import com.ismartcoding.plain.ui.components.ImageGridItem
import com.ismartcoding.plain.ui.extensions.reset
import com.ismartcoding.plain.ui.models.CastViewModel
import com.ismartcoding.plain.ui.models.ImagesViewModel
import com.ismartcoding.plain.ui.models.MediaFoldersViewModel
import com.ismartcoding.plain.ui.models.TagsViewModel
import com.ismartcoding.plain.ui.models.exitSearchMode
import com.ismartcoding.plain.ui.page.images.ImagesPageState
import com.ismartcoding.plain.ui.page.images.ViewImageBottomSheet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TabContentImages(
    navController: NavHostController,
    bucketId: String,
    imagesState: ImagesPageState,
    imagesViewModel: ImagesViewModel,
    tagsViewModel: TagsViewModel,
    bucketViewModel: MediaFoldersViewModel,
    castViewModel: CastViewModel,
    paddingValues: PaddingValues
) {
    val context = LocalContext.current
    val view = LocalView.current
    val window = (view.context as Activity).window
    val itemsState = imagesState.itemsState
    val configuration = LocalConfiguration.current
    val pagerState = imagesState.pagerState
    val scrollBehavior = imagesState.scrollBehavior
    val tagsState = imagesState.tagsState
    val previewerState = imagesState.previewerState
    val tagsMapState = imagesState.tagsMapState
    val dragSelectState = imagesState.dragSelectState
    val cellsPerRow = imagesState.cellsPerRow

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
                    imagesViewModel.loadAsync(context, tagsViewModel)
                    bucketViewModel.loadAsync(context)
                }
                setRefreshState(RefreshContentState.Finished)
            }
        }

    val once = rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!once.value) {
            once.value = true
            imagesViewModel.bucketId.value = bucketId
            imagesViewModel.hasPermission.value = AppFeatureType.FILES.hasPermission(context)
            tagsViewModel.dataType.value = imagesViewModel.dataType
            bucketViewModel.dataType.value = imagesViewModel.dataType
            if (imagesViewModel.hasPermission.value) {
                scope.launch(Dispatchers.IO) {
                    cellsPerRow.value = ImageGridCellsPerRowPreference.getAsync(context)
                    imagesViewModel.sortBy.value = ImageSortByPreference.getValueAsync(context)
                    imagesViewModel.loadAsync(context, tagsViewModel)
                    bucketViewModel.loadAsync(context)
                }
            }
        } else {
            // refresh tabs in case tag name changed in tags page
            scope.launch(Dispatchers.IO) {
                imagesViewModel.refreshTabsAsync(context, tagsViewModel)
            }
        }
        events.add(
            receiveEventHandler<PermissionsResultEvent> {
                imagesViewModel.hasPermission.value = AppFeatureType.FILES.hasPermission(context)
                scope.launch(Dispatchers.IO) {
                    imagesViewModel.sortBy.value = ImageSortByPreference.getValueAsync(context)
                    imagesViewModel.loadAsync(context, tagsViewModel)
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
        imagesViewModel.searchActive.value = false
        imagesViewModel.showLoading.value = true
        scope.launch(Dispatchers.IO) {
            imagesViewModel.loadAsync(context, tagsViewModel)
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
        val tab = imagesViewModel.tabs.value.getOrNull(pagerState.currentPage) ?: return@LaunchedEffect
        if (tab.value == "all") {
            imagesViewModel.trash.value = false
            imagesViewModel.tag.value = null
        } else {
            val tag = tagsState.find { it.id == tab.value }
            imagesViewModel.trash.value = false
            imagesViewModel.tag.value = tag
        }
        scope.launch {
            scrollBehavior.reset()
            imagesViewModel.scrollStateMap[pagerState.currentPage]?.scrollToItem(0)
        }
        scope.launch(Dispatchers.IO) {
            imagesViewModel.loadAsync(context, tagsViewModel)
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
        } else if (imagesViewModel.showSearchBar.value) {
            if (!imagesViewModel.searchActive.value || imagesViewModel.queryText.value.isEmpty()) {
                imagesViewModel.exitSearchMode()
                onSearch("")
            }
        } else {
            navController.popBackStack()
        }
    }

    if (imagesViewModel.showCellsPerRowDialog.value) {
        RadioDialog(
            title = stringResource(R.string.cells_per_row),
            options = IntRange(2, 10).map { value ->
                RadioDialogOption(
                    text = value.toString(),
                    selected = value == cellsPerRow.value,
                ) {
                    scope.launch(Dispatchers.IO) {
                        ImageGridCellsPerRowPreference.putAsync(context, value)
                        cellsPerRow.value = value
                    }
                }
            },
        ) {
            imagesViewModel.showCellsPerRowDialog.value = false
        }
    }

    ViewImageBottomSheet(imagesViewModel, tagsViewModel, tagsMapState, tagsState, dragSelectState)

    if (imagesViewModel.showSortDialog.value) {
        FileSortDialog(imagesViewModel.sortBy, onSelected = {
            scope.launch(Dispatchers.IO) {
                ImageSortByPreference.putAsync(context, it)
                imagesViewModel.sortBy.value = it
                imagesViewModel.loadAsync(context, tagsViewModel)
            }
        }, onDismiss = {
            imagesViewModel.showSortDialog.value = false
        })
    }

    CastDialog(castViewModel)
    Column(modifier = Modifier.fillMaxSize()) {

        if (!imagesViewModel.hasPermission.value) {
            NeedPermissionColumn(R.drawable.image, AppFeatureType.FILES.getPermission()!!)
            return
        }

        if (!dragSelectState.selectMode) {
            PScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                imagesViewModel.tabs.value.forEachIndexed { index, s ->
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
                                Text(if (imagesViewModel.bucketId.value.isNotEmpty() || imagesViewModel.queryText.value.isNotEmpty()) s.title else "${s.title} (${s.count})")
                            }
                        }
                    )
                }
            }
        }
        if (pagerState.pageCount == 0) {
            NoDataColumn(loading = imagesViewModel.showLoading.value, search = imagesViewModel.showSearchBar.value)
            return
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
                        imagesViewModel.scrollStateMap[index] = scrollState
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
                                items(
                                    itemsState,
                                    key = {
                                        it.id
                                    },
                                    contentType = {
                                        "image"
                                    },
                                    span = {
                                        GridItemSpan(1)
                                    }) { m ->
                                    ImageGridItem(
                                        modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null),
                                        imagesViewModel,
                                        castViewModel,
                                        m,
                                        showSize = cellsPerRow.value < 6,
                                        previewerState,
                                        dragSelectState,
                                        imageWidthPx,
                                    )
                                }
                                item(
                                    span = { GridItemSpan(maxLineSpan) },
                                    key = "loadMore"
                                ) {
                                    if (itemsState.isNotEmpty() && !imagesViewModel.noMore.value) {
                                        LaunchedEffect(Unit) {
                                            scope.launch(Dispatchers.IO) {
                                                withIO { imagesViewModel.moreAsync(context, tagsViewModel) }
                                            }
                                        }
                                    }
                                    LoadMoreRefreshContent(imagesViewModel.noMore.value)
                                }
                                item(
                                    span = { GridItemSpan(maxLineSpan) },
                                    key = "bottomSpace"
                                ) {
                                    BottomSpace(paddingValues)
                                }
                            }
                        }
                    } else {
                        NoDataColumn(loading = imagesViewModel.showLoading.value, search = imagesViewModel.showSearchBar.value)
                    }
                }
            }
        }
    }
}
