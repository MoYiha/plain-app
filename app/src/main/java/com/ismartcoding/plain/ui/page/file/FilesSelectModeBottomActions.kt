package com.ismartcoding.plain.ui.page.file

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.ismartcoding.lib.channel.sendEvent
import com.ismartcoding.lib.extensions.scanFileByConnection
import com.ismartcoding.lib.helpers.CoroutinesHelper.withIO
import com.ismartcoding.lib.helpers.ZipHelper
import com.ismartcoding.plain.MainApp
import com.ismartcoding.plain.R
import com.ismartcoding.plain.enums.ActionSourceType
import com.ismartcoding.plain.enums.ActionType
import com.ismartcoding.plain.extensions.newPath
import com.ismartcoding.plain.features.ActionEvent
import com.ismartcoding.plain.helpers.FileHelper
import com.ismartcoding.plain.helpers.ShareHelper
import com.ismartcoding.plain.ui.base.ActionButtons
import com.ismartcoding.plain.ui.base.IconCopyButton
import com.ismartcoding.plain.ui.base.IconCutButton
import com.ismartcoding.plain.ui.base.IconDeleteButton
import com.ismartcoding.plain.ui.base.IconRenameButton
import com.ismartcoding.plain.ui.base.IconShareButton
import com.ismartcoding.plain.ui.base.IconUnzipButton
import com.ismartcoding.plain.ui.base.IconZipButton
import com.ismartcoding.plain.ui.base.PBottomAppBar
import com.ismartcoding.plain.ui.base.TextFieldDialog
import com.ismartcoding.plain.ui.models.FilesViewModel
import com.ismartcoding.plain.ui.helpers.DialogHelper
import com.ismartcoding.plain.ui.models.exitSelectMode
import kotlinx.coroutines.launch
import org.zeroturnaround.zip.ZipUtil
import java.io.File

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilesSelectModeBottomActions(
    viewModel: FilesViewModel,
    onShowPasteBar: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val selectedFiles = viewModel.itemsFlow.value.filter { file -> viewModel.selectedIds.contains(file.path) }
    
    val showRenameDialog = remember { mutableStateOf(false) }
    
    if (showRenameDialog.value && viewModel.selectedIds.size == 1) {
        val file = selectedFiles[0]
        val name = remember { mutableStateOf(file.name) }
        
        TextFieldDialog(
            title = stringResource(id = R.string.rename),
            value = name.value,
            placeholder = file.name,
            onValueChange = { name.value = it },
            onDismissRequest = { showRenameDialog.value = false },
            confirmText = stringResource(id = R.string.save),
            onConfirm = { newName ->
                scope.launch {
                    DialogHelper.showLoading()
                    val oldName = file.name
                    val oldPath = file.path
                    val dstFile = withIO { FileHelper.rename(file.path, newName) }
                    if (dstFile != null) {
                        withIO {
                            MainApp.instance.scanFileByConnection(file.path)
                            MainApp.instance.scanFileByConnection(dstFile)
                        }
                    }
                    
                    file.name = newName
                    file.path = file.path.replace("/$oldName", "/$newName")
                    if (file.isDir) {
                        viewModel.breadcrumbs.find { b -> b.path == oldPath }?.let { b ->
                            b.path = file.path
                            b.name = newName
                        }
                    }
                    
                    DialogHelper.hideLoading()
                    viewModel.exitSelectMode()
                    showRenameDialog.value = false
                }
            }
        )
    }
    
    PBottomAppBar {
        ActionButtons {
            IconCutButton {
                viewModel.cutFiles.clear()
                viewModel.cutFiles.addAll(selectedFiles.map { file ->
                    file.copy()
                })
                viewModel.copyFiles.clear()
                onShowPasteBar(true)
                viewModel.exitSelectMode()
            }
            IconCopyButton {
                viewModel.copyFiles.clear()
                viewModel.copyFiles.addAll(selectedFiles.map { file ->
                    file.copy()
                })
                viewModel.cutFiles.clear()
                onShowPasteBar(true)
                viewModel.exitSelectMode()
            }
            IconShareButton {
                ShareHelper.sharePaths(context, viewModel.selectedIds.toSet())
            }
            IconDeleteButton {
                DialogHelper.confirmToDelete {
                    scope.launch {
                        val paths = viewModel.selectedIds.toSet()
                        DialogHelper.showLoading()
                        withIO {
                            paths.forEach {
                                File(it).deleteRecursively()
                            }
                            MainApp.instance.scanFileByConnection(paths.toTypedArray())
                        }
                        DialogHelper.hideLoading()
                        sendEvent(ActionEvent(ActionSourceType.FILE, ActionType.DELETED, paths))
                        viewModel.exitSelectMode()
                    }
                }
            }

            IconZipButton {
                if (selectedFiles.isNotEmpty()) {
                    scope.launch {
                        DialogHelper.showLoading()
                        val file = selectedFiles[0]
                        val destFile = File(file.path + ".zip")
                        var destPath = destFile.path
                        if (destFile.exists()) {
                            destPath = destFile.newPath()
                        }
                        withIO {
                            ZipHelper.zip(selectedFiles.map { it.path }, destPath)
                        }
                        DialogHelper.hideLoading()
                        viewModel.exitSelectMode()
                    }
                }
            }
            
            if (selectedFiles.size == 1 && selectedFiles[0].path.endsWith(".zip")) {
                IconUnzipButton {
                    scope.launch {
                        DialogHelper.showLoading()
                        val file = selectedFiles[0]
                        val destFile = File(file.path.removeSuffix(".zip"))
                        var destPath = destFile.path
                        if (destFile.exists()) {
                            destPath = destFile.newPath()
                        }
                        withIO {
                            ZipUtil.unpack(File(file.path), File(destPath))
                            MainApp.instance.scanFileByConnection(destPath)
                        }
                        DialogHelper.hideLoading()
                        viewModel.exitSelectMode()
                    }
                }
            }
            
            if (viewModel.selectedIds.size == 1) {
                IconRenameButton {
                    showRenameDialog.value = true
                }
            }
        }
    }
} 