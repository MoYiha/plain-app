package com.ismartcoding.plain.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.ismartcoding.lib.extensions.formatDuration
import com.ismartcoding.lib.helpers.CoroutinesHelper.coIO
import com.ismartcoding.plain.R
import com.ismartcoding.plain.data.DAudio
import com.ismartcoding.plain.db.DTag
import com.ismartcoding.plain.features.AudioPlayer
import com.ismartcoding.plain.features.Permissions
import com.ismartcoding.plain.ui.base.HorizontalSpace
import com.ismartcoding.plain.ui.base.VerticalSpace
import com.ismartcoding.plain.ui.base.dragselect.DragSelectState
import com.ismartcoding.plain.ui.models.AudioViewModel
import com.ismartcoding.plain.ui.models.TagsViewModel
import com.ismartcoding.plain.ui.theme.PlainTheme
import com.ismartcoding.plain.ui.theme.listItemSubtitle
import com.ismartcoding.plain.ui.theme.listItemTag
import com.ismartcoding.plain.ui.theme.listItemTitle

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AudioListItem(
    item: DAudio,
    viewModel: AudioViewModel,
    tags: List<DTag>,
    dragSelectState: DragSelectState,
    isCurrentlyPlaying: Boolean = false,
    tagsViewModel: TagsViewModel? = null,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val selected = remember(item.id, dragSelectState.selectedIds, viewModel.selectedItem.value) {
        dragSelectState.isSelected(item.id) || viewModel.selectedItem.value?.id == item.id
    }

    Row(modifier = modifier) {
        if (dragSelectState.selectMode) {
            HorizontalSpace(dp = 16.dp)
            Checkbox(
                checked = dragSelectState.isSelected(item.id),
                onCheckedChange = {
                    dragSelectState.select(item.id)
                }
            )
        }

        Surface(
            modifier = PlainTheme
                .getCardModifier(selected = selected)
                .combinedClickable(
                    onClick = {
                        if (dragSelectState.selectMode) {
                            dragSelectState.select(item.id)
                        } else {
                            Permissions.checkNotification(context, R.string.audio_notification_prompt) {
                                AudioPlayer.play(context, item.toPlaylistAudio())
                            }
                        }
                    },
                    onLongClick = {
                        if (dragSelectState.selectMode) {
                            return@combinedClickable
                        }
                        viewModel.selectedItem.value = item
                    },
                )
                .weight(1f),
            color = Color.Unspecified,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 8.dp, 8.dp, 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.size(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (!isCurrentlyPlaying) {
                        Icon(
                            painter = painterResource(R.drawable.music2),
                            contentDescription = item.title,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        PulsatingWave(
                            isPlaying = true,
                            modifier = Modifier
                                .align(Alignment.Center)
                        )
                    }
                }

                HorizontalSpace(dp = 16.dp)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.listItemTitle(),
                    )
                    VerticalSpace(dp = 8.dp)
                    Text(
                        text = listOf(item.artist, item.duration.formatDuration()).filter { it.isNotEmpty() }.joinToString(" • "),
                        style = MaterialTheme.typography.listItemSubtitle(),
                    )
                    if (tags.isNotEmpty()) {
                        VerticalSpace(dp = 8.dp)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            tags.forEach { tag ->
                                ClickableText(
                                    text = AnnotatedString("#" + tag.name),
                                    modifier = Modifier
                                        .wrapContentHeight()
                                        .padding(end = 8.dp),
                                    style = MaterialTheme.typography.listItemTag(),
                                    onClick = {
                                        if (dragSelectState.selectMode) {
                                            return@ClickableText
                                        }
                                        viewModel.trash.value = false
                                        viewModel.tag.value = tag
                                        if (tagsViewModel != null) {
                                            coIO {
                                                viewModel.loadAsync(context, tagsViewModel)
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
