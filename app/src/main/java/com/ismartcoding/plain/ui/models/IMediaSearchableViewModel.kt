package com.ismartcoding.plain.ui.models

import android.content.Context
import androidx.compose.runtime.MutableState
import com.ismartcoding.plain.data.IData
import com.ismartcoding.plain.enums.DataType

interface IMediaSearchableViewModel<T : IData> : ISearchableViewModel<T> {
    val tag: MutableState<com.ismartcoding.plain.db.DTag?>
    val trash: MutableState<Boolean>
    val bucketId: MutableState<String>
    val dataType: DataType
    val showLoading: MutableState<Boolean>
    val hasPermission: MutableState<Boolean>
    val showSortDialog: MutableState<Boolean>

    suspend fun loadAsync(context: Context, tagsViewModel: TagsViewModel)
} 