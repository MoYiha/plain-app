package com.ismartcoding.plain.ui.models

import android.content.Context
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ismartcoding.plain.R
import com.ismartcoding.plain.data.DAudio
import com.ismartcoding.plain.data.DMediaBucket
import com.ismartcoding.plain.db.DTag
import com.ismartcoding.plain.enums.DataType
import com.ismartcoding.plain.features.TagHelper
import com.ismartcoding.plain.features.file.FileSortBy
import com.ismartcoding.plain.features.locale.LocaleHelper
import com.ismartcoding.plain.features.media.AudioMediaStoreHelper
import com.ismartcoding.plain.preference.AudioPlaylistPreference
import com.ismartcoding.plain.ui.helpers.DialogHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi::class)
class AudioViewModel(private val savedStateHandle: SavedStateHandle) :
    IMediaSearchableViewModel<DAudio>,
    ViewModel() {
    private val _itemsFlow = MutableStateFlow(mutableStateListOf<DAudio>())
    val itemsFlow: StateFlow<List<DAudio>> get() = _itemsFlow
    override var showLoading = mutableStateOf(true)
    var offset = mutableIntStateOf(0)
    var limit = mutableIntStateOf(50)
    var noMore = mutableStateOf(false)
    override var trash = mutableStateOf(false)
    var total = mutableIntStateOf(0)
    override var tag = mutableStateOf<DTag?>(null)
    override val dataType = DataType.AUDIO
    var selectedItem = mutableStateOf<DAudio?>(null)
    val sortBy = mutableStateOf(FileSortBy.DATE_DESC)
    override val showSortDialog = mutableStateOf(false)
    val showRenameDialog = mutableStateOf(false)
    val tabs = mutableStateOf(listOf<VTabData>())
    override var hasPermission = mutableStateOf(false)
    override var bucketId = mutableStateOf("")

    val scrollStateMap = mutableStateMapOf<Int, LazyListState>()

    override val showSearchBar = mutableStateOf(false)
    override val searchActive = mutableStateOf(false)
    override val queryText = mutableStateOf("")

    suspend fun moreAsync(context: Context, tagsViewModel: TagsViewModel) {
        offset.value += limit.intValue
        val items = AudioMediaStoreHelper.searchAsync(context, getQuery(), limit.intValue, offset.intValue, sortBy.value)
        _itemsFlow.update {
            val mutableList = it.toMutableStateList()
            mutableList.addAll(items)
            mutableList
        }
        tagsViewModel.loadMoreAsync(items.map { it.id }.toSet())
        showLoading.value = false
        noMore.value = items.size < limit.intValue
    }

    override suspend fun loadAsync(context: Context, tagsViewModel: TagsViewModel) {
        offset.intValue = 0
        _itemsFlow.value = AudioMediaStoreHelper.searchAsync(context, getQuery(), limit.intValue, offset.intValue, sortBy.value).toMutableStateList()
        refreshTabsAsync(context, tagsViewModel)
        noMore.value = _itemsFlow.value.size < limit.intValue
        showLoading.value = false
    }

    suspend fun refreshTabsAsync(context: Context, tagsViewModel: TagsViewModel) {
        tagsViewModel.loadAsync(_itemsFlow.value.map { it.id }.toSet())
        total.intValue = AudioMediaStoreHelper.countAsync(context, getTotalQuery())
        tabs.value = listOf(
            VTabData(LocaleHelper.getString(R.string.all), "all", total.intValue),
            * tagsViewModel.itemsFlow.value.map { VTabData(it.name, it.id, it.count) }.toTypedArray()
        )
    }

    fun trash(ids: Set<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            DialogHelper.showLoading()
            // 标记为回收站
            // 将来可以实现实际的回收站功能
            DialogHelper.hideLoading()
        }
    }

    fun restore(ids: Set<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            DialogHelper.showLoading()
            DialogHelper.hideLoading()
        }
    }

    fun delete(context: Context, tagsViewModel: TagsViewModel, ids: Set<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            DialogHelper.showLoading()
            TagHelper.deleteTagRelationByKeys(ids, dataType)
            AudioMediaStoreHelper.deleteRecordsAndFilesByIdsAsync(context, ids)
            val pathes = itemsFlow.value.filter { ids.contains(it.id) }.map { it.path }.toSet()
            AudioPlaylistPreference.deleteAsync(context, pathes)
            loadAsync(context, tagsViewModel)
            DialogHelper.hideLoading()
            _itemsFlow.update {
                it.toMutableStateList().apply {
                    removeIf { i -> ids.contains(i.id) }
                }
            }
        }
    }

    private fun getTotalQuery(): String {
        var query = "${queryText.value} trash:false"
        if (bucketId.value.isNotEmpty()) {
            query += " bucket_id:${bucketId.value}"
        }
        return query
    }

    private fun getTrashQuery(): String {
        var query = "${queryText.value} trash:true"
        if (bucketId.value.isNotEmpty()) {
            query += " bucket_id:${bucketId.value}"
        }
        return query
    }

    private fun getQuery(): String {
        var query = "${queryText.value} trash:${trash.value}"
        if (tag.value != null) {
            val tagId = tag.value!!.id
            val ids = TagHelper.getKeysByTagId(tagId)
            query += " ids:${ids.joinToString(",")}"
        }

        if (bucketId.value.isNotEmpty()) {
            query += " bucket_id:${bucketId.value}"
        }
        return query
    }

}
