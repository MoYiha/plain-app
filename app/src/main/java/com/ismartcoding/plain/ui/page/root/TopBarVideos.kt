package com.ismartcoding.plain.ui.page.root

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import com.ismartcoding.plain.ui.base.MediaTopBar
import com.ismartcoding.plain.ui.models.CastViewModel
import com.ismartcoding.plain.ui.models.TagsViewModel
import com.ismartcoding.plain.ui.models.VideosViewModel
import com.ismartcoding.plain.ui.page.videos.VideosPageState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TopBarVideos(
    navController: NavHostController,
    bucketId: String,
    videosState: VideosPageState,
    videosViewModel: VideosViewModel,
    tagsViewModel: TagsViewModel,
    castViewModel: CastViewModel,
) {
    val scope = rememberCoroutineScope()

    MediaTopBar(
        navController = navController,
        bucketId = bucketId,
        viewModel = videosViewModel,
        tagsViewModel = tagsViewModel,
        castViewModel = castViewModel,
        dragSelectState = videosState.dragSelectState,
        scrollBehavior = videosState.scrollBehavior,
        bucketsMap = videosState.bucketsMap,
        itemsState = videosState.itemsState,
        currentPage = videosState.pagerState.currentPage,
        scrollToTop = { page ->
            scope.launch {
                videosViewModel.scrollStateMap[page]?.scrollToItem(0)
            }
        },
        onCellsPerRowClick = { videosViewModel.showCellsPerRowDialog.value = true },
        onSearchAction = { context, tagsVM ->
            scope.launch(Dispatchers.IO) {
                videosViewModel.loadAsync(context, tagsVM)
            }
        }
    )
} 