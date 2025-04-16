package com.ismartcoding.plain.ui.page.root

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import com.ismartcoding.plain.ui.base.MediaTopBar
import com.ismartcoding.plain.ui.models.AudioViewModel
import com.ismartcoding.plain.ui.models.CastViewModel
import com.ismartcoding.plain.ui.models.TagsViewModel
import com.ismartcoding.plain.ui.page.audio.AudioPageState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TopBarAudio(
    navController: NavHostController,
    audioViewModel: AudioViewModel,
    tagsViewModel: TagsViewModel,
    castViewModel: CastViewModel,
    bucketId: String = "",
    audioState: AudioPageState? = null,
) {
    if (audioState == null) return
    
    val scope = rememberCoroutineScope()
    
    MediaTopBar(
        navController = navController,
        bucketId = bucketId,
        viewModel = audioViewModel,
        tagsViewModel = tagsViewModel,
        castViewModel = castViewModel,
        dragSelectState = audioState.dragSelectState,
        scrollBehavior = audioState.scrollBehavior,
        bucketsMap = audioState.bucketsMap,
        itemsState = audioState.itemsState,
        currentPage = audioState.pagerState.currentPage,
        scrollToTop = { page ->
            scope.launch {
                audioViewModel.scrollStateMap[page]?.scrollToItem(0)
            }
        },
        showCellsPerRowDialog = false,
        onSearchAction = { context, tagsVM ->
            scope.launch(Dispatchers.IO) {
                audioViewModel.loadAsync(context, tagsVM)
            }
        }
    )
} 