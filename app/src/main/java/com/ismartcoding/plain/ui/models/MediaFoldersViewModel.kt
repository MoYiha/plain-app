package com.ismartcoding.plain.ui.models

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ismartcoding.plain.data.DMediaBucket
import com.ismartcoding.plain.db.DFeed
import com.ismartcoding.plain.enums.DataType
import com.ismartcoding.plain.features.media.AudioMediaStoreHelper
import com.ismartcoding.plain.features.media.ImageMediaStoreHelper
import com.ismartcoding.plain.features.media.VideoMediaStoreHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@OptIn(androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi::class)
class MediaFoldersViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    private val _itemsFlow = MutableStateFlow<List<DMediaBucket>>(emptyList())
    val itemsFlow: StateFlow<List<DMediaBucket>> get() = _itemsFlow

    val bucketsMapFlow: StateFlow<Map<String, DMediaBucket>> =
        _itemsFlow
            .map { list -> list.associateBy { it.id } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    var showLoading = mutableStateOf(true)
    var selectedItem = mutableStateOf<DFeed?>(null)
    var dataType = mutableStateOf(DataType.DEFAULT)

    fun loadAsync(context: Context) {
        _itemsFlow.value = (when (dataType.value) {
            DataType.IMAGE -> {
                ImageMediaStoreHelper.getBucketsAsync(context)
            }

            DataType.VIDEO -> {
                VideoMediaStoreHelper.getBucketsAsync(context)
            }
            
            DataType.AUDIO -> {
                AudioMediaStoreHelper.getBucketsAsync(context)
            }

            else -> {
                emptyList()
            }
        }).toMutableStateList()
        showLoading.value = false
    }

}
