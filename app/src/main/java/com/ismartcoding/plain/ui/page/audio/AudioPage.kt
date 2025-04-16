package com.ismartcoding.plain.ui.page.audio

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
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
import com.ismartcoding.plain.enums.AudioAction
import com.ismartcoding.plain.features.AudioActionEvent
import com.ismartcoding.plain.features.AudioPlayer
import com.ismartcoding.plain.features.PermissionsResultEvent
import com.ismartcoding.plain.preference.AudioPlayingPreference
import com.ismartcoding.plain.preference.AudioSortByPreference
import com.ismartcoding.plain.ui.base.NeedPermissionColumn
import com.ismartcoding.plain.ui.base.NoDataColumn
import com.ismartcoding.plain.ui.base.PFilterChip
import com.ismartcoding.plain.ui.base.PScaffold
import com.ismartcoding.plain.ui.base.TopSpace
import com.ismartcoding.plain.ui.base.VerticalSpace
import com.ismartcoding.plain.ui.base.dragselect.listDragSelect
import com.ismartcoding.plain.ui.base.fastscroll.LazyColumnScrollbar
import com.ismartcoding.plain.ui.base.pullrefresh.LoadMoreRefreshContent
import com.ismartcoding.plain.ui.base.pullrefresh.PullToRefresh
import com.ismartcoding.plain.ui.base.pullrefresh.RefreshContentState
import com.ismartcoding.plain.ui.base.pullrefresh.rememberRefreshLayoutState
import com.ismartcoding.plain.ui.base.tabs.PScrollableTabRow
import com.ismartcoding.plain.ui.components.AudioListItem
import com.ismartcoding.plain.ui.components.AudioPlayerBar
import com.ismartcoding.plain.ui.components.CastDialog
import com.ismartcoding.plain.ui.components.FileSortDialog
import com.ismartcoding.plain.ui.extensions.reset
import com.ismartcoding.plain.ui.models.AudioViewModel
import com.ismartcoding.plain.ui.models.CastViewModel
import com.ismartcoding.plain.ui.models.MediaFoldersViewModel
import com.ismartcoding.plain.ui.models.TagsViewModel
import com.ismartcoding.plain.ui.page.root.TopBarAudio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AudioPage(
    navController: NavHostController,
    bucketId: String = "",
    audioViewModel: AudioViewModel = viewModel(),
    tagsViewModel: TagsViewModel = viewModel(),
    castViewModel: CastViewModel = viewModel(),
    bucketViewModel: MediaFoldersViewModel = viewModel(),
) {
    val view = LocalView.current
    val window = (view.context as Activity).window
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val audioState = AudioPageState.create(
        audioViewModel = audioViewModel,
        tagsViewModel = tagsViewModel,
        bucketViewModel = bucketViewModel,
    )

    val pagerState = audioState.pagerState
    val scrollBehavior = audioState.scrollBehavior
    val tagsState = audioState.tagsState
    val previewerState = audioState.previewerState
    val tagsMapState = audioState.tagsMapState
    val dragSelectState = audioState.dragSelectState
    val itemsState = audioState.itemsState
    val scrollState = audioState.scrollState

    // Track the current playing state at this level
    var currentPlayingPath by remember { mutableStateOf("") }
    val isAudioPlaying by AudioPlayer.isPlayingFlow.collectAsState()
    
    // Function to update the playing state
    val updatePlayingState = {
        scope.launch {
            currentPlayingPath = withIO { AudioPlayingPreference.getValueAsync(context) }
        }
    }

    val events = remember { mutableStateListOf<Job>() }

    var isFirstTime by remember { mutableStateOf(true) }

    val topRefreshLayoutState =
        rememberRefreshLayoutState {
            scope.launch {
                withIO {
                    audioViewModel.loadAsync(context, tagsViewModel)
                    bucketViewModel.loadAsync(context)
                }
                setRefreshState(RefreshContentState.Finished)
            }
        }

    val once = rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!once.value) {
            once.value = true
            audioViewModel.hasPermission.value = AppFeatureType.FILES.hasPermission(context)
            audioViewModel.bucketId.value = bucketId
            tagsViewModel.dataType.value = audioViewModel.dataType
            bucketViewModel.dataType.value = audioViewModel.dataType
            if (audioViewModel.hasPermission.value) {
                scope.launch(Dispatchers.IO) {
                    audioViewModel.sortBy.value = AudioSortByPreference.getValueAsync(context)
                    audioViewModel.loadAsync(context, tagsViewModel)
                    bucketViewModel.loadAsync(context)
                }
            }
            // Initial load of playing state
            updatePlayingState()
        } else {
            scope.launch(Dispatchers.IO) {
                audioViewModel.refreshTabsAsync(context, tagsViewModel)
            }
        }
        events.add(
            receiveEventHandler<PermissionsResultEvent> {
                audioViewModel.hasPermission.value = AppFeatureType.FILES.hasPermission(context)
                scope.launch(Dispatchers.IO) {
                    audioViewModel.sortBy.value = AudioSortByPreference.getValueAsync(context)
                    audioViewModel.loadAsync(context, tagsViewModel)
                }
            })
            
        // Add event handler for audio playing state changes
        events.add(
            receiveEventHandler<AudioActionEvent> { event ->
                if (event.action == AudioAction.MEDIA_ITEM_TRANSITION) {
                    updatePlayingState()
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
        val tab = audioViewModel.tabs.value.getOrNull(pagerState.currentPage) ?: return@LaunchedEffect
        if (tab.value == "all") {
            audioViewModel.trash.value = false
            audioViewModel.tag.value = null
        } else {
            val tag = tagsState.find { it.id == tab.value }
            audioViewModel.trash.value = false
            audioViewModel.tag.value = tag
        }
        scope.launch {
            scrollBehavior.reset()
            audioViewModel.scrollStateMap[pagerState.currentPage]?.scrollToItem(0) ?: scrollState.scrollToItem(0)
        }
        scope.launch(Dispatchers.IO) {
            audioViewModel.loadAsync(context, tagsViewModel)
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
        } else if (audioViewModel.showSearchBar.value) {
            if (!audioViewModel.searchActive.value || audioViewModel.queryText.value.isEmpty()) {
                audioViewModel.showSearchBar.value = false
                audioViewModel.queryText.value = ""
                scope.launch(Dispatchers.IO) {
                    audioViewModel.loadAsync(context, tagsViewModel)
                }
            }
        } else {
            navController.popBackStack()
        }
    }

    val audioTagsMap = remember(tagsMapState, tagsState) {
        tagsMapState.mapValues { entry ->
            entry.value.mapNotNull { relation ->
                tagsState.find { it.id == relation.tagId }
            }
        }
    }

    if (audioViewModel.showSortDialog.value) {
        FileSortDialog(audioViewModel.sortBy, onSelected = {
            scope.launch(Dispatchers.IO) {
                AudioSortByPreference.putAsync(context, it)
                audioViewModel.sortBy.value = it
                audioViewModel.loadAsync(context, tagsViewModel)
            }
        }, onDismiss = {
            audioViewModel.showSortDialog.value = false
        })
    }

    ViewAudioBottomSheet(
        viewModel = audioViewModel,
        tagsViewModel = tagsViewModel,
        tagsMapState = tagsMapState,
        tagsState = tagsState,
        dragSelectState = dragSelectState,
    )

    CastDialog(castViewModel)

    PScaffold(
        topBar = {
            TopBarAudio(
                navController = navController,
                audioViewModel = audioViewModel,
                tagsViewModel = tagsViewModel,
                castViewModel = castViewModel,
                bucketId = bucketId,
                audioState = audioState
            )
        },
        bottomBar = {
            AnimatedVisibility(
                visible = dragSelectState.showBottomActions(),
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                AudioFilesSelectModeBottomActions(
                    viewModel = audioViewModel,
                    tagsViewModel = tagsViewModel,
                    tagsState = tagsState,
                    dragSelectState = dragSelectState
                )
            }
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (!audioViewModel.hasPermission.value) {
                    NeedPermissionColumn(R.drawable.music, AppFeatureType.FILES.getPermission()!!)
                    return@Column
                }
    
                if (!dragSelectState.selectMode) {
                    PScrollableTabRow(
                        selectedTabIndex = pagerState.currentPage,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        audioViewModel.tabs.value.forEachIndexed { index, s ->
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
                                        Text(if (audioViewModel.bucketId.value.isNotEmpty() || audioViewModel.queryText.value.isNotEmpty()) s.title else "${s.title} (${s.count})")
                                    }
                                }
                            )
                        }
                    }
                }
    
                if (pagerState.pageCount == 0) {
                    NoDataColumn(loading = audioViewModel.showLoading.value, search = audioViewModel.showSearchBar.value)
                    return@Column
                }
    
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { index ->
                    PullToRefresh(
                        refreshLayoutState = topRefreshLayoutState,
                        userEnable = !dragSelectState.selectMode,
                    ) {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            if (itemsState.isNotEmpty()) {
                                val scrollState = rememberLazyListState()
                                audioViewModel.scrollStateMap[index] = scrollState
                                LazyColumnScrollbar(
                                    state = scrollState,
                                ) {
                                    LazyColumn(
                                        Modifier
                                            .fillMaxSize()
                                            .nestedScroll(scrollBehavior.nestedScrollConnection)
                                            .listDragSelect(
                                                items = itemsState,
                                                state = dragSelectState
                                            ),
                                        state = scrollState,
                                        contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding() + 80.dp)
                                    ) {
                                        item {
                                            TopSpace()
                                        }
                                        items(
                                            items = itemsState,
                                            key = { it.id }
                                        ) { item ->
                                            val tags = audioTagsMap[item.id] ?: emptyList()
                                            AudioListItem(
                                                item = item,
                                                viewModel = audioViewModel,
                                                tags = tags,
                                                dragSelectState = dragSelectState,
                                                isCurrentlyPlaying = isAudioPlaying && currentPlayingPath == item.path,
                                                tagsViewModel = tagsViewModel,
                                                modifier = Modifier.fillMaxWidth(),
                                            )
                                            VerticalSpace(dp = 8.dp)
                                        }
    
                                        item(key = "loadMore") {
                                            if (itemsState.isNotEmpty() && !audioViewModel.noMore.value) {
                                                LaunchedEffect(Unit) {
                                                    scope.launch(Dispatchers.IO) {
                                                        withIO { audioViewModel.moreAsync(context, tagsViewModel) }
                                                    }
                                                }
                                            }
                                            LoadMoreRefreshContent(audioViewModel.noMore.value)
                                        }
                                    }
                                }
                            } else {
                                NoDataColumn(loading = audioViewModel.showLoading.value, search = audioViewModel.showSearchBar.value)
                            }
                        }
                    }
                }
            }

            AudioPlayerBar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
            )
        }
    }
}
