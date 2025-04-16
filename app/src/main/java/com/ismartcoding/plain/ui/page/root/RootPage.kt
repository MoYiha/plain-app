package com.ismartcoding.plain.ui.page.root

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ismartcoding.plain.R
import com.ismartcoding.plain.ui.base.PDraggableElement
import com.ismartcoding.plain.ui.base.PScaffold
import com.ismartcoding.plain.ui.components.mediaviewer.previewer.MediaPreviewer
import com.ismartcoding.plain.ui.models.CastViewModel
import com.ismartcoding.plain.ui.models.ImagesViewModel
import com.ismartcoding.plain.ui.models.MainViewModel
import com.ismartcoding.plain.ui.models.MediaFoldersViewModel
import com.ismartcoding.plain.ui.models.TagsViewModel
import com.ismartcoding.plain.ui.models.VideosViewModel
import com.ismartcoding.plain.ui.models.AudioViewModel
import com.ismartcoding.plain.ui.nav.Routing
import com.ismartcoding.plain.ui.page.images.ImageFilesSelectModeBottomActions
import com.ismartcoding.plain.ui.page.images.ImagesPageState
import com.ismartcoding.plain.ui.page.videos.VideoFilesSelectModeBottomActions
import com.ismartcoding.plain.ui.page.videos.VideosPageState
import com.ismartcoding.plain.ui.page.audio.AudioPageState
import com.ismartcoding.plain.ui.page.audio.AudioFilesSelectModeBottomActions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RootPage(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    imagesViewModel: ImagesViewModel = viewModel(),
    tagsViewModel: TagsViewModel = viewModel(),
    bucketViewModel: MediaFoldersViewModel = viewModel(),
    castViewModel: CastViewModel = viewModel(),
    videosViewModel: VideosViewModel = viewModel(),
    audioViewModel: AudioViewModel = viewModel(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // Create pager state with 4 pages (HOME, IMAGES, AUDIO, VIDEOS)
    val pagerState = rememberPagerState(
        initialPage = RootTabType.HOME.value,
        pageCount = { 4 }
    )

    val imagesState = ImagesPageState.create(
        imagesViewModel = imagesViewModel,
        tagsViewModel = tagsViewModel,
        bucketViewModel = bucketViewModel,
    )

    val videosState = VideosPageState.create(
        videosViewModel = videosViewModel,
        tagsViewModel = tagsViewModel,
        bucketViewModel = bucketViewModel,
    )

    val audioState = AudioPageState.create(
        audioViewModel = audioViewModel,
        tagsViewModel = tagsViewModel,
        bucketViewModel = bucketViewModel,
    )

    PScaffold(
        topBar = {
            when (pagerState.currentPage) {
                RootTabType.HOME.value -> {
                    TopBarHome(
                        navController = navController,
                    )
                }

                RootTabType.IMAGES.value -> {
                    TopBarImages(
                        navController = navController,
                        "",
                        imagesState = imagesState,
                        imagesViewModel = imagesViewModel,
                        tagsViewModel = tagsViewModel,
                        castViewModel = castViewModel,
                    )
                }

                RootTabType.AUDIO.value -> {
                    TopBarAudio(
                        navController = navController,
                        audioViewModel = audioViewModel,
                        tagsViewModel = tagsViewModel,
                        castViewModel = castViewModel,
                        audioState = audioState
                    )
                }

                RootTabType.VIDEOS.value -> {
                    TopBarVideos(
                        navController = navController,
                        "",
                        videosState = videosState,
                        videosViewModel = videosViewModel,
                        tagsViewModel = tagsViewModel,
                        castViewModel = castViewModel,
                    )
                }
            }

        },
        floatingActionButton = {
            if (pagerState.currentPage == RootTabType.HOME.value) {
                PDraggableElement {
                    FloatingActionButton(
                        onClick = {
                            navController.navigate(Routing.Chat)
                        },
                    ) {
                        Icon(
                            Icons.AutoMirrored.Outlined.Chat,
                            stringResource(R.string.file_transfer_assistant),
                        )
                    }
                }
            }
        },
        bottomBar = {
            RootNavigationBar(
                selectedTab = pagerState.currentPage,
                onTabSelected = {
                    scope.launch {
                        pagerState.animateScrollToPage(it)
                    }
                },
            )

            AnimatedVisibility(
                visible = imagesState.dragSelectState.showBottomActions(),
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                ImageFilesSelectModeBottomActions(
                    imagesViewModel,
                    tagsViewModel,
                    imagesState.tagsState,
                    imagesState.dragSelectState
                )
            }
            AnimatedVisibility(
                visible = videosState.dragSelectState.showBottomActions(),
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                VideoFilesSelectModeBottomActions(
                    videosViewModel,
                    tagsViewModel,
                    videosState.tagsState,
                    videosState.dragSelectState
                )
            }
            AnimatedVisibility(
                visible = audioState.dragSelectState.showBottomActions(),
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                AudioFilesSelectModeBottomActions(
                    audioViewModel,
                    tagsViewModel,
                    audioState.tagsState,
                    audioState.dragSelectState
                )
            }
        },
    ) { paddingValues ->
        // Use HorizontalPager with content caching
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 3, // Cache all pages (we have 4 tabs total)
            userScrollEnabled = false, // Disable swipe to navigate, use bottom tabs instead
        ) { page ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                when (page) {
                    RootTabType.HOME.value -> {
                        TabContentHome(
                            navController = navController,
                            viewModel = mainViewModel,
                            paddingValues
                        )
                    }

                    RootTabType.IMAGES.value -> {
                        TabContentImages(
                            navController = navController,
                            bucketId = "",
                            imagesState, imagesViewModel, tagsViewModel, bucketViewModel, castViewModel,
                            paddingValues
                        )
                    }

                    RootTabType.AUDIO.value -> {
                        TabContentAudio(
                            navController = navController,
                            bucketId = "",
                            audioState = audioState,
                            audioViewModel = audioViewModel,
                            tagsViewModel = tagsViewModel,
                            bucketViewModel = bucketViewModel,
                            castViewModel = castViewModel,
                            paddingValues = paddingValues
                        )
                    }

                    RootTabType.VIDEOS.value -> {
                        TabContentVideos(
                            navController = navController,
                            bucketId = "",
                            videosState, videosViewModel, tagsViewModel, bucketViewModel, castViewModel,
                            paddingValues
                        )
                    }
                }
            }
        }
    }

    MediaPreviewer(
        state = imagesState.previewerState,
        tagsViewModel = tagsViewModel,
        tagsMap = imagesState.tagsMapState,
        tagsState = imagesState.tagsState,
        onRenamed = {
            scope.launch(Dispatchers.IO) {
                imagesViewModel.loadAsync(context, tagsViewModel)
            }
        },
        deleteAction = { item ->
            scope.launch(Dispatchers.IO) {
                imagesViewModel.delete(context, tagsViewModel, setOf(item.mediaId))
                imagesState.previewerState.closeTransform()
            }
        },
        onTagsChanged = {
            scope.launch(Dispatchers.IO) {
                imagesViewModel.refreshTabsAsync(context, tagsViewModel)
            }
        }
    )

    MediaPreviewer(
        state = videosState.previewerState,
        tagsViewModel = tagsViewModel,
        tagsMap = videosState.tagsMapState,
        tagsState = videosState.tagsState,
        onRenamed = {
            scope.launch(Dispatchers.IO) {
                videosViewModel.loadAsync(context, tagsViewModel)
            }
        },
        deleteAction = { item ->
            scope.launch(Dispatchers.IO) {
                videosViewModel.delete(context, tagsViewModel, setOf(item.mediaId))
                videosState.previewerState.closeTransform()
            }
        },
        onTagsChanged = {
            scope.launch(Dispatchers.IO) {
                videosViewModel.refreshTabsAsync(context, tagsViewModel)
            }
        }
    )
}
