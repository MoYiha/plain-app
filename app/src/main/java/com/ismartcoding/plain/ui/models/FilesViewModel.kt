package com.ismartcoding.plain.ui.models

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.ismartcoding.lib.extensions.getFilenameFromPath
import com.ismartcoding.lib.extensions.getParentPath
import com.ismartcoding.lib.extensions.scanFileByConnection
import com.ismartcoding.lib.helpers.CoroutinesHelper.withIO
import com.ismartcoding.plain.MainApp
import com.ismartcoding.plain.features.file.DFile
import com.ismartcoding.plain.features.file.FileSortBy
import com.ismartcoding.plain.features.file.FileSystemHelper
import com.ismartcoding.plain.features.media.FileMediaStoreHelper
import com.ismartcoding.plain.preference.ShowHiddenFilesPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class BreadcrumbItem(var name: String, var path: String)

class FilesViewModel : BaseItemsModel(), ISearchableViewModel<DFile>, ISelectableViewModel<DFile> {
    var root = FileSystemHelper.getInternalStoragePath()
    var path = root
    val breadcrumbs = mutableStateListOf(BreadcrumbItem(FileSystemHelper.getInternalStorageName(MainApp.instance), root))
    val selectedBreadcrumbIndex = mutableStateOf(0)
    var cutFiles = mutableListOf<DFile>()
    var copyFiles = mutableListOf<DFile>()
    var type: FilesType = FilesType.INTERNAL_STORAGE
    
    // 添加文件信息查看相关属性
    val selectedFile = mutableStateOf<DFile?>(null)
    val showRenameDialog = mutableStateOf(false)
    
    override val showSearchBar = mutableStateOf(false)
    override val searchActive = mutableStateOf(false)
    override val queryText = mutableStateOf("")
    
    override val selectMode = mutableStateOf(false)
    override val selectedIds = mutableStateListOf<String>()
    private val _itemsFlow = MutableStateFlow<List<DFile>>(emptyList())
    override val itemsFlow: StateFlow<List<DFile>> = _itemsFlow.asStateFlow()
    
    val sortBy = mutableStateOf(FileSortBy.NAME_ASC)
    val showSortDialog = mutableStateOf(false)
    
    val isLoading = mutableStateOf(true)
    val showMoreMenu = mutableStateOf(false)
    val showPasteBar = mutableStateOf(false)
    val showCreateFolderDialog = mutableStateOf(false)
    val showCreateFileDialog = mutableStateOf(false)
    val showFolderKanbanDialog = mutableStateOf(false)
    
    fun updateItems(items: List<DFile>) {
        _itemsFlow.value = items
    }

    fun getAndUpdateSelectedIndex(): Int {
        var index = breadcrumbs.indexOfFirst { it.path == path }
        if (index == -1) {
            val parent = path.getParentPath()
            breadcrumbs.reversed().forEach { b ->
                if (b.path != parent && !("$parent/").startsWith(b.path + "/")) {
                    breadcrumbs.remove(b)
                }
            }
            breadcrumbs.add(BreadcrumbItem(path.getFilenameFromPath(), path))
            index = breadcrumbs.size - 1
        }
        
        selectedBreadcrumbIndex.value = index
        return index
    }
    
    fun getQuery(): String {
        return queryText.value.trim()
    }

    suspend fun loadAsync(context: android.content.Context) {
        isLoading.value = true
        val showHiddenFiles = withContext(Dispatchers.IO) {
            ShowHiddenFilesPreference.getAsync(context)
        }
        
        val files = withContext(Dispatchers.IO) {
            val query = getQuery()
            if (showSearchBar.value && query.isNotEmpty()) {
                // Search mode
                FileSystemHelper.search(query, path, showHiddenFiles)
            } else if (type == FilesType.RECENTS) {
                FileMediaStoreHelper.getRecentFilesAsync(context)
            } else {
                FileSystemHelper.getFilesList(
                    path,
                    showHiddenFiles,
                    sortBy.value
                )
            }
        }
        
        _itemsFlow.value = files
        isLoading.value = false
    }
    
    fun deleteFiles(paths: Set<String>) {
        viewModelScope.launch {
            withIO {
                paths.forEach {
                    File(it).deleteRecursively()
                }
                
                MainApp.instance.scanFileByConnection(paths.toTypedArray())
            }
        }
    }
}

enum class FilesType {
    INTERNAL_STORAGE,
    RECENTS,
    SDCARD,
    USB_STORAGE,
    APP,
}
