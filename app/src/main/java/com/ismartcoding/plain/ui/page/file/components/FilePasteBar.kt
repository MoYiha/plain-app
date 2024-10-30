package com.ismartcoding.plain.ui.page.file.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ismartcoding.lib.extensions.getFilenameFromPath
import com.ismartcoding.lib.helpers.CoroutinesHelper.withIO
import com.ismartcoding.plain.R
import com.ismartcoding.plain.extensions.newPath
import com.ismartcoding.plain.features.locale.LocaleHelper
import com.ismartcoding.plain.ui.helpers.DialogHelper
import com.ismartcoding.plain.ui.models.FilesViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.moveTo

@Composable
fun FilePasteBar(
    viewModel: FilesViewModel,
    coroutineScope: CoroutineScope,
    onPasteComplete: () -> Unit
) {
    val context = LocalContext.current
    
    BottomAppBar {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                viewModel.cutFiles.clear()
                viewModel.copyFiles.clear()
                viewModel.showPasteBar.value = false
            }) {
                Icon(painter = painterResource(R.drawable.x), contentDescription = "Cancel")
            }

            Text(
                text = if (viewModel.cutFiles.isNotEmpty())
                    LocaleHelper.getQuantityString(R.plurals.moving_items, viewModel.cutFiles.size)
                else
                    LocaleHelper.getQuantityString(R.plurals.copying_items, viewModel.copyFiles.size),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Button(onClick = {
                coroutineScope.launch {
                    if (viewModel.cutFiles.isNotEmpty()) {
                        DialogHelper.showLoading()
                        withContext(Dispatchers.IO) {
                            viewModel.cutFiles.forEach {
                                val dstFile = File(viewModel.path + "/" + it.id.getFilenameFromPath())
                                if (!dstFile.exists()) {
                                    Path(it.id).moveTo(dstFile.toPath(), true)
                                } else {
                                    Path(it.id).moveTo(Path(dstFile.newPath()), true)
                                }
                            }
                            viewModel.cutFiles.clear()
                        }
                        DialogHelper.hideLoading()
                        onPasteComplete()
                        viewModel.showPasteBar.value = false
                    } else if (viewModel.copyFiles.isNotEmpty()) {
                        DialogHelper.showLoading()
                        withContext(Dispatchers.IO) {
                            viewModel.copyFiles.forEach {
                                val dstFile = File(viewModel.path + "/" + it.id.getFilenameFromPath())
                                if (!dstFile.exists()) {
                                    File(it.id).copyRecursively(dstFile, true)
                                } else {
                                    File(it.id).copyRecursively(File(dstFile.newPath()), true)
                                }
                            }
                            viewModel.copyFiles.clear()
                        }
                        DialogHelper.hideLoading()
                        onPasteComplete()
                        viewModel.showPasteBar.value = false
                    }
                }
            }) {
                Text(stringResource(R.string.paste))
            }
        }
    }
} 