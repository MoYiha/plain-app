package com.ismartcoding.plain.ui.page.file.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import com.ismartcoding.lib.extensions.isAudioFast
import com.ismartcoding.lib.extensions.isImageFast
import com.ismartcoding.lib.extensions.isPdfFile
import com.ismartcoding.lib.extensions.isTextFile
import com.ismartcoding.lib.extensions.isVideoFast
import com.ismartcoding.lib.helpers.CoroutinesHelper.coMain
import com.ismartcoding.lib.helpers.CoroutinesHelper.withIO
import com.ismartcoding.plain.R
import com.ismartcoding.plain.Constants
import com.ismartcoding.plain.data.DPlaylistAudio
import com.ismartcoding.plain.features.AudioPlayer
import com.ismartcoding.plain.features.Permissions
import com.ismartcoding.plain.features.file.DFile
import com.ismartcoding.plain.helpers.ShareHelper
import com.ismartcoding.plain.ui.base.BottomSpace
import com.ismartcoding.plain.ui.components.mediaviewer.previewer.MediaPreviewerState
import com.ismartcoding.plain.ui.helpers.DialogHelper
import com.ismartcoding.plain.ui.models.FilesViewModel
import com.ismartcoding.plain.ui.models.MediaPreviewData
import com.ismartcoding.plain.ui.models.enterSelectMode
import com.ismartcoding.plain.ui.models.select
import com.ismartcoding.plain.ui.nav.navigatePdf
import com.ismartcoding.plain.ui.nav.navigateTextFile
import com.ismartcoding.plain.ui.page.file.navigateTo
import com.ismartcoding.plain.ui.preview.PreviewItem
import java.io.File

@Composable
fun FileListContent(
    navController: NavHostController,
    viewModel: FilesViewModel,
    files: List<DFile>,
    loadFiles: (List<DFile>, Boolean) -> Unit,
    previewerState: MediaPreviewerState
) {
    val context = LocalContext.current
    
    if (viewModel.isLoading.value) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (files.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.no_data),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    } else {
        val lazyListState = rememberLazyListState()
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = lazyListState,
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(files) { file ->
                FileListItem(
                    file = file,
                    isSelected = viewModel.selectedIds.contains(file.path),
                    isSelectMode = viewModel.selectMode.value,
                    onClick = {
                        if (viewModel.selectMode.value) {
                            viewModel.select(file.path)
                        } else {
                            if (file.isDir) {
                                navigateTo(viewModel, file.path) {
                                    viewModel.isLoading.value = true
                                    viewModel.updateItems(emptyList())
                                    loadFiles(emptyList(), true)
                                }
                            } else {
                                openFile(context, file, navController, previewerState)
                            }
                        }
                    },
                    onLongClick = {
                        if (!viewModel.selectMode.value) {
                            // 显示文件信息
                            viewModel.selectedFile.value = file
                        } else {
                            viewModel.select(file.path)
                        }
                    },
                    previewerState = previewerState
                )
            }

            item {
                BottomSpace()
            }
        }
    }
}

fun openFile(
    context: android.content.Context,
    file: DFile,
    navController: NavHostController,
    previewerState: MediaPreviewerState? = null
) {
    val path = file.path

    when {
        path.isImageFast() || path.isVideoFast() -> {
            if (previewerState != null) {
                coMain {
                    val previewItem = PreviewItem(
                        id = file.path,
                        path = file.path,
                        size = file.size,
                        mediaId = file.path
                    )

                    withIO {
                        val transformState = previewerState.transformState ?: return@withIO
                        val itemState = transformState.itemState ?: return@withIO

                        // 确保items在调用open之前已经设置好
                        MediaPreviewData.items = listOf(previewItem)

                        if (path.isImageFast()) {
                            previewItem.initImageAsync()
                        }

                        // 设置数据以便预览使用
                        MediaPreviewData.setDataAsync(context, itemState, listOf(previewItem), previewItem)
                    }

                    // 现在可以安全地打开预览了
                    if (MediaPreviewData.items.isNotEmpty()) {
                        previewerState.open(index = 0)
                    }
                }
            }
        }
        path.isAudioFast() -> {
            try {
                Permissions.checkNotification(context, R.string.audio_notification_prompt) {
                    AudioPlayer.play(context, DPlaylistAudio.fromPath(context, path))
                }
            } catch (ex: Exception) {
                // 处理音频播放错误
                DialogHelper.showMessage(R.string.audio_play_error)
            }
        }
        path.isTextFile() -> {
            // 添加文件大小限制检查
            if (file.size <= Constants.MAX_READABLE_TEXT_FILE_SIZE) {
                navController.navigateTextFile(path)
            } else {
                DialogHelper.showMessage(R.string.text_file_size_limit)
            }
        }
        path.isPdfFile() -> {
            try {
                navController.navigatePdf(File(path).toUri())
            } catch (ex: Exception) {
                // 处理PDF打开错误
                DialogHelper.showMessage(R.string.pdf_open_error)
            }
        }
        else -> {
            ShareHelper.openPathWith(context, path)
        }
    }
}