package com.ismartcoding.plain.ui.page.videos

import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import com.ismartcoding.plain.data.DMediaBucket
import com.ismartcoding.plain.data.DVideo
import com.ismartcoding.plain.db.DTag
import com.ismartcoding.plain.db.DTagRelation
import com.ismartcoding.plain.preference.VideoGridCellsPerRowPreference
import com.ismartcoding.plain.ui.base.dragselect.DragSelectState
import com.ismartcoding.plain.ui.base.dragselect.rememberDragSelectState
import com.ismartcoding.plain.ui.components.mediaviewer.previewer.MediaPreviewerState
import com.ismartcoding.plain.ui.components.mediaviewer.previewer.rememberPreviewerState
import com.ismartcoding.plain.ui.models.MediaFoldersViewModel
import com.ismartcoding.plain.ui.models.TagsViewModel
import com.ismartcoding.plain.ui.models.VideosViewModel

@OptIn(ExperimentalMaterial3Api::class)
data class VideosPageState(
    val pagerState: PagerState,
    val itemsState: List<DVideo>,
    val dragSelectState: DragSelectState,
    val scrollBehavior: TopAppBarScrollBehavior,
    val previewerState: MediaPreviewerState,
    val tagsState: List<DTag>,
    val tagsMapState: Map<String, List<DTagRelation>>,
    val bucketsMap: Map<String, DMediaBucket>,
    val cellsPerRow: MutableState<Int>
) {

    companion object {
        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun create(
            videosViewModel: VideosViewModel,
            tagsViewModel: TagsViewModel,
            bucketViewModel: MediaFoldersViewModel,
        ): VideosPageState {
            val pagerState = rememberPagerState(pageCount = { videosViewModel.tabs.value.size })
            val itemsState by videosViewModel.itemsFlow.collectAsState()
            val dragSelectState = rememberDragSelectState({ videosViewModel.scrollStateMap[pagerState.currentPage] })
            val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(canScroll = {
                (videosViewModel.scrollStateMap[pagerState.currentPage]?.firstVisibleItemIndex ?: 0) > 0 && !dragSelectState.selectMode
            })
            val previewerState = rememberPreviewerState()
            val tagsState by tagsViewModel.itemsFlow.collectAsState()
            val tagsMapState by tagsViewModel.tagsMapFlow.collectAsState()
            val bucketsMap by bucketViewModel.bucketsMapFlow.collectAsState()
            val cellsPerRow = remember { mutableIntStateOf(VideoGridCellsPerRowPreference.default) }

            return VideosPageState(
                pagerState = pagerState,
                itemsState = itemsState,
                dragSelectState = dragSelectState,
                scrollBehavior = scrollBehavior,
                previewerState = previewerState,
                tagsState = tagsState,
                tagsMapState = tagsMapState,
                bucketsMap = bucketsMap,
                cellsPerRow = cellsPerRow
            )
        }
    }
} 