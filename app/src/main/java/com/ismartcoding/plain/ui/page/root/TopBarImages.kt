package com.ismartcoding.plain.ui.page.root

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import com.ismartcoding.plain.ui.base.MediaTopBar
import com.ismartcoding.plain.ui.models.CastViewModel
import com.ismartcoding.plain.ui.models.ImagesViewModel
import com.ismartcoding.plain.ui.models.TagsViewModel
import com.ismartcoding.plain.ui.page.images.ImagesPageState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TopBarImages(
    navController: NavHostController,
    bucketId: String,
    imagesState: ImagesPageState,
    imagesViewModel: ImagesViewModel,
    tagsViewModel: TagsViewModel,
    castViewModel: CastViewModel,
) {
    val scope = rememberCoroutineScope()

    MediaTopBar(
        navController = navController,
        bucketId = bucketId,
        viewModel = imagesViewModel,
        tagsViewModel = tagsViewModel,
        castViewModel = castViewModel,
        dragSelectState = imagesState.dragSelectState,
        scrollBehavior = imagesState.scrollBehavior,
        bucketsMap = imagesState.bucketsMap,
        itemsState = imagesState.itemsState,
        currentPage = imagesState.pagerState.currentPage,
        scrollToTop = { page ->
            scope.launch {
                imagesViewModel.scrollStateMap[page]?.scrollToItem(0)
            }
        },
        onCellsPerRowClick = { imagesViewModel.showCellsPerRowDialog.value = true },
        onSearchAction = { context, tagsVM ->
            scope.launch(Dispatchers.IO) {
                imagesViewModel.loadAsync(context, tagsVM)
            }
        }
    )
}