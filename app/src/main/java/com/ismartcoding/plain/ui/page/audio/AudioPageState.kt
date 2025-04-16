package com.ismartcoding.plain.ui.page.audio

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.ismartcoding.plain.data.DAudio
import com.ismartcoding.plain.data.DMediaBucket
import com.ismartcoding.plain.db.DTag
import com.ismartcoding.plain.db.DTagRelation
import com.ismartcoding.plain.ui.base.dragselect.DragSelectState
import com.ismartcoding.plain.ui.base.dragselect.rememberDragSelectState
import com.ismartcoding.plain.ui.base.dragselect.rememberListDragSelectState
import com.ismartcoding.plain.ui.components.mediaviewer.previewer.MediaPreviewerState
import com.ismartcoding.plain.ui.components.mediaviewer.previewer.rememberPreviewerState
import com.ismartcoding.plain.ui.models.AudioViewModel
import com.ismartcoding.plain.ui.models.MediaFoldersViewModel
import com.ismartcoding.plain.ui.models.TagsViewModel

@OptIn(ExperimentalMaterial3Api::class)
data class AudioPageState(
    val pagerState: PagerState,
    val itemsState: List<DAudio>,
    val dragSelectState: DragSelectState,
    val scrollBehavior: TopAppBarScrollBehavior,
    val previewerState: MediaPreviewerState,
    val tagsState: List<DTag>,
    val tagsMapState: Map<String, List<DTagRelation>>,
    val scrollState: LazyListState,
    val bucketsMap: Map<String, DMediaBucket>
) {

    companion object {
        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun create(
            audioViewModel: AudioViewModel,
            tagsViewModel: TagsViewModel,
            bucketViewModel: MediaFoldersViewModel,
        ): AudioPageState {
            val pagerState = rememberPagerState(pageCount = { audioViewModel.tabs.value.size })
            val itemsState by audioViewModel.itemsFlow.collectAsState()
            val scrollState = rememberLazyListState()
            
            val dragSelectState = rememberListDragSelectState({ audioViewModel.scrollStateMap[pagerState.currentPage] })
            
            val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(canScroll = {
                (audioViewModel.scrollStateMap[pagerState.currentPage]?.firstVisibleItemIndex ?: 0) > 0 && !dragSelectState.selectMode
            })
            
            val previewerState = rememberPreviewerState()
            val tagsState by tagsViewModel.itemsFlow.collectAsState()
            val tagsMapState by tagsViewModel.tagsMapFlow.collectAsState()
            val bucketsMap by bucketViewModel.bucketsMapFlow.collectAsState()

            return AudioPageState(
                pagerState = pagerState,
                itemsState = itemsState,
                dragSelectState = dragSelectState,
                scrollBehavior = scrollBehavior,
                previewerState = previewerState,
                tagsState = tagsState,
                tagsMapState = tagsMapState,
                scrollState = scrollState,
                bucketsMap = bucketsMap
            )
        }
    }
} 