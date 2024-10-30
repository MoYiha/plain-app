package com.ismartcoding.plain.ui.page.root

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ismartcoding.plain.enums.DataType
import com.ismartcoding.plain.ui.base.AnimatedBottomAction
import com.ismartcoding.plain.ui.base.PScaffold
import com.ismartcoding.plain.ui.components.mediaviewer.previewer.MediaPreviewer
import com.ismartcoding.plain.ui.models.AudioViewModel
import com.ismartcoding.plain.ui.models.CastViewModel
import com.ismartcoding.plain.ui.models.ImagesViewModel
import com.ismartcoding.plain.ui.models.MainViewModel
import com.ismartcoding.plain.ui.models.MediaFoldersViewModel
import com.ismartcoding.plain.ui.models.TagsViewModel
import com.ismartcoding.plain.ui.models.VideosViewModel
import com.ismartcoding.plain.ui.models.exitSearchMode
import com.ismartcoding.plain.ui.page.audio.AudioFilesSelectModeBottomActions
import com.ismartcoding.plain.ui.page.audio.AudioPageState
import com.ismartcoding.plain.ui.page.images.ImageFilesSelectModeBottomActions
import com.ismartcoding.plain.ui.page.images.ImagesPageState
import com.ismartcoding.plain.ui.page.videos.VideoFilesSelectModeBottomActions
import com.ismartcoding.plain.ui.page.videos.VideosPageState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RootPage(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    imagesViewModel: ImagesViewModel = viewModel(),
    imageTagsViewModel: TagsViewModel = viewModel(key = "imageTagsViewModel"),
    imageBucketViewModel: MediaFoldersViewModel = viewModel(key = "imageBucketViewModel"),
    imageCastViewModel: CastViewModel = viewModel(key = "imageCastViewModel"),
    videosViewModel: VideosViewModel = viewModel(),
    videoTagsViewModel: TagsViewModel = viewModel(key = "videoTagsViewModel"),
    videoBucketViewModel: MediaFoldersViewModel = viewModel(key = "videoBucketViewModel"),
    videoCastViewModel: CastViewModel = viewModel(key = "videoCastViewModel"),
    audioViewModel: AudioViewModel = viewModel(),
    audioTagsViewModel: TagsViewModel = viewModel(key = "audioTagsViewModel"),
    audioBucketViewModel: MediaFoldersViewModel = viewModel(key = "audioBucketViewModel"),
    audioCastViewModel: CastViewModel = viewModel(key = "audioCastViewModel"),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = RootTabType.HOME.value,
        pageCount = { 4 }
    )

    val imagesState = ImagesPageState.create(
        imagesViewModel = imagesViewModel,
        tagsViewModel = imageTagsViewModel,
        bucketViewModel = imageBucketViewModel,
    )

    val videosState = VideosPageState.create(
        videosViewModel = videosViewModel,
        tagsViewModel = videoTagsViewModel,
        bucketViewModel = videoBucketViewModel,
    )

    val audioState = AudioPageState.create(
        audioViewModel = audioViewModel,
        tagsViewModel = audioTagsViewModel,
        bucketViewModel = audioBucketViewModel,
    )

    BackHandler {
        if (pagerState.currentPage == RootTabType.IMAGES.value) {
            if (imagesState.previewerState.visible) {
                scope.launch {
                    imagesState.previewerState.closeTransform()
                }
            } else if (imagesState.dragSelectState.selectMode) {
                imagesState.dragSelectState.exitSelectMode()
            } else if (imageCastViewModel.castMode.value) {
                imageCastViewModel.exitCastMode()
            } else if (imagesViewModel.showSearchBar.value) {
                if (!imagesViewModel.searchActive.value || imagesViewModel.queryText.value.isEmpty()) {
                    imagesViewModel.exitSearchMode()
                    imagesViewModel.showLoading.value = true
                    scope.launch(Dispatchers.IO) {
                        imagesViewModel.loadAsync(context, imageTagsViewModel)
                    }
                }
            } else {
                navController.popBackStack()
            }
        } else if (pagerState.currentPage == RootTabType.VIDEOS.value) {
            if (videosState.previewerState.visible) {
                scope.launch {
                    videosState.previewerState.closeTransform()
                }
            } else if (videosState.dragSelectState.selectMode) {
                videosState.dragSelectState.exitSelectMode()
            } else if (videoCastViewModel.castMode.value) {
                videoCastViewModel.exitCastMode()
            } else if (videosViewModel.showSearchBar.value) {
                if (!videosViewModel.searchActive.value || videosViewModel.queryText.value.isEmpty()) {
                    videosViewModel.exitSearchMode()
                    videosViewModel.showLoading.value = true
                    scope.launch(Dispatchers.IO) {
                        videosViewModel.loadAsync(context, videoTagsViewModel)
                    }
                }
            } else {
                navController.popBackStack()
            }
        } else if (pagerState.currentPage == RootTabType.AUDIO.value) {
            if (audioState.dragSelectState.selectMode) {
                audioState.dragSelectState.exitSelectMode()
            } else if (audioCastViewModel.castMode.value) {
                audioCastViewModel.exitCastMode()
            } else if (audioViewModel.showSearchBar.value) {
                if (!audioViewModel.searchActive.value || audioViewModel.queryText.value.isEmpty()) {
                    audioViewModel.exitSearchMode()
                    audioViewModel.showLoading.value = true
                    scope.launch(Dispatchers.IO) {
                        audioViewModel.loadAsync(context, audioTagsViewModel)
                    }
                }
            } else {
                navController.popBackStack()
            }
        }
    }

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
                        imagesState = imagesState,
                        imagesViewModel = imagesViewModel,
                        tagsViewModel = imageTagsViewModel,
                        castViewModel = imageCastViewModel,
                    )
                }

                RootTabType.AUDIO.value -> {
                    TopBarAudio(
                        navController = navController,
                        audioState = audioState,
                        audioViewModel = audioViewModel,
                        tagsViewModel = audioTagsViewModel,
                        castViewModel = audioCastViewModel,
                    )
                }

                RootTabType.VIDEOS.value -> {
                    TopBarVideos(
                        navController = navController,
                        videosState = videosState,
                        videosViewModel = videosViewModel,
                        tagsViewModel = videoTagsViewModel,
                        castViewModel = videoCastViewModel,
                    )
                }
            }
        },
        bottomBar = {
            AnimatedBottomAction(visible = imagesState.dragSelectState.showBottomActions()) {
                ImageFilesSelectModeBottomActions(
                    imagesViewModel,
                    imageTagsViewModel,
                    imagesState.tagsState,
                    imagesState.dragSelectState
                )
            }
            AnimatedBottomAction(visible = videosState.dragSelectState.showBottomActions()) {
                VideoFilesSelectModeBottomActions(
                    videosViewModel,
                    videoTagsViewModel,
                    videosState.tagsState,
                    videosState.dragSelectState
                )
            }
            AnimatedBottomAction(visible = audioState.dragSelectState.showBottomActions()) {
                AudioFilesSelectModeBottomActions(
                    audioViewModel,
                    audioTagsViewModel,
                    audioState.tagsState,
                    audioState.dragSelectState
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                beyondViewportPageCount = 3,
                userScrollEnabled = false,
            ) { page ->
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
                            imagesState, imagesViewModel, imageTagsViewModel, imageBucketViewModel, imageCastViewModel,
                            paddingValues
                        )
                    }

                    RootTabType.AUDIO.value -> {
                        TabContentAudio(
                            navController = navController,
                            bucketId = "",
                            audioState = audioState,
                            audioViewModel = audioViewModel,
                            tagsViewModel = audioTagsViewModel,
                            bucketViewModel = audioBucketViewModel,
                            castViewModel = audioCastViewModel,
                            paddingValues = paddingValues
                        )
                    }

                    RootTabType.VIDEOS.value -> {
                        TabContentVideos(
                            navController = navController,
                            bucketId = "",
                            videosState, videosViewModel, videoTagsViewModel, videoBucketViewModel, videoCastViewModel,
                            paddingValues
                        )
                    }
                }
            }

            RootNavigationBar(
                selectedTab = pagerState.currentPage,
                onTabSelected = {
                    scope.launch {
                        pagerState.animateScrollToPage(it)
                    }
                }
            )
        }
    }

    MediaPreviewer(
        state = imagesState.previewerState,
        tagsViewModel = imageTagsViewModel,
        tagsMap = imagesState.tagsMapState,
        tagsState = imagesState.tagsState,
        onRenamed = {
            scope.launch(Dispatchers.IO) {
                imagesViewModel.loadAsync(context, imageTagsViewModel)
            }
        },
        deleteAction = { item ->
            scope.launch(Dispatchers.IO) {
                imagesViewModel.delete(context, imageTagsViewModel, setOf(item.mediaId))
                imagesState.previewerState.closeTransform()
            }
        },
        onTagsChanged = {
            scope.launch(Dispatchers.IO) {
                imagesViewModel.refreshTabsAsync(context, imageTagsViewModel)
            }
        }
    )

    MediaPreviewer(
        state = videosState.previewerState,
        tagsViewModel = videoTagsViewModel,
        tagsMap = videosState.tagsMapState,
        tagsState = videosState.tagsState,
        onRenamed = {
            scope.launch(Dispatchers.IO) {
                videosViewModel.loadAsync(context, videoTagsViewModel)
            }
        },
        deleteAction = { item ->
            scope.launch(Dispatchers.IO) {
                videosViewModel.delete(context, videoTagsViewModel, setOf(item.mediaId))
                videosState.previewerState.closeTransform()
            }
        },
        onTagsChanged = {
            scope.launch(Dispatchers.IO) {
                videosViewModel.refreshTabsAsync(context, videoTagsViewModel)
            }
        }
    )
}

