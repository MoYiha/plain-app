package com.ismartcoding.plain.ui.page.audio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ismartcoding.lib.channel.receiveEvent
import com.ismartcoding.lib.channel.sendEvent
import com.ismartcoding.lib.extensions.formatDuration
import com.ismartcoding.lib.helpers.CoroutinesHelper.withIO
import com.ismartcoding.plain.R
import com.ismartcoding.plain.data.DPlaylistAudio
import com.ismartcoding.plain.enums.AudioAction
import com.ismartcoding.plain.features.AudioActionEvent
import com.ismartcoding.plain.features.AudioPlayer
import com.ismartcoding.plain.features.ClearAudioPlaylistEvent
import com.ismartcoding.plain.features.Permissions
import com.ismartcoding.plain.features.locale.LocaleHelper
import com.ismartcoding.plain.preference.AudioPlayingPreference
import com.ismartcoding.plain.preference.AudioPlaylistPreference
import com.ismartcoding.plain.ui.base.PModalBottomSheet
import com.ismartcoding.plain.ui.components.PulsatingWave
import com.ismartcoding.plain.ui.theme.cardBackgroundActive
import com.ismartcoding.plain.ui.theme.cardBackgroundNormal
import com.ismartcoding.plain.ui.theme.circleBackground
import com.ismartcoding.plain.ui.theme.primaryTextColor
import com.ismartcoding.plain.ui.theme.red
import com.ismartcoding.plain.ui.theme.secondaryTextColor
import com.ismartcoding.plain.ui.theme.surfaceBackground
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import com.ismartcoding.plain.ui.base.PBottomSheetTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPlaylistPage(
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    
    var audioItems by remember { mutableStateOf<List<DPlaylistAudio>>(emptyList()) }
    var currentPlayingPath by remember { mutableStateOf("") }
    val isAudioPlaying by AudioPlayer.isPlayingFlow.collectAsState()
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    
    val listState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(
        onMove = { from, to ->
            scope.launch {
                val audioList = audioItems.toMutableList()
                audioList.apply {
                    add(to.index, removeAt(from.index))
                }
                audioItems = audioList
                withIO { AudioPlaylistPreference.putAsync(context, audioList) }
            }
        }
    )
    
    val loadData = {
        scope.launch {
            currentPlayingPath = withIO { AudioPlayingPreference.getValueAsync(context) }
            val audios = withIO {
                AudioPlaylistPreference.getValueAsync(context)
            }
            audioItems = audios
        }
    }
    
    // Initial load
    LaunchedEffect(Unit) {
        loadData()
    }
    
    // Event listeners
    LaunchedEffect(Unit) {
        lifecycleOwner.receiveEvent<AudioActionEvent> { event ->
            if (event.action == AudioAction.PLAYBACK_STATE_CHANGED ||
                event.action == AudioAction.MEDIA_ITEM_TRANSITION) {
                loadData()
            }
        }
        
        lifecycleOwner.receiveEvent<ClearAudioPlaylistEvent> {
            onDismissRequest()
        }
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            containerColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text(stringResource(R.string.clear_all)) },
            text = { Text(stringResource(R.string.clear_all_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            withIO {
                                AudioPlayingPreference.putAsync(context, "")
                                AudioPlaylistPreference.putAsync(context, arrayListOf())
                            }
                            AudioPlayer.clear()
                            sendEvent(ClearAudioPlaylistEvent())
                            showClearConfirmDialog = false
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
    
    PModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
    ) {
        Column {
            // Top Bar with title and actions
            PBottomSheetTopAppBar(
                titleContent = {
                    Text(
                        text = if (audioItems.isNotEmpty()) 
                            LocaleHelper.getStringF(R.string.playlist_title, "total", audioItems.size) 
                        else stringResource(R.string.playlist),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                actions = {
                    if (audioItems.isNotEmpty()) {
                        IconButton(onClick = { showClearConfirmDialog = true }) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = "Clear playlist",
                                tint = MaterialTheme.colorScheme.red
                            )
                        }
                    }
                }
            )
            
            if (audioItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.empty_playlist),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.secondaryTextColor
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .reorderable(reorderableState),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    itemsIndexed(audioItems, { _, item -> item.path }) { index, audio ->
                        val isPlaying = isAudioPlaying && currentPlayingPath == audio.path
                        val dragModifier = Modifier.detectReorderAfterLongPress(reorderableState)
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .then(dragModifier)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    Permissions.checkNotification(
                                        context, 
                                        R.string.audio_notification_prompt
                                    ) {
                                        AudioPlayer.justPlay(context, audio)
                                    }
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isPlaying) 
                                    MaterialTheme.colorScheme.cardBackgroundActive 
                                else MaterialTheme.colorScheme.cardBackgroundNormal
                            ),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Number or playing indicator
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isPlaying) 
                                                MaterialTheme.colorScheme.primary
                                            else 
                                                MaterialTheme.colorScheme.circleBackground
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isPlaying) {
                                        PulsatingWave(
                                            isPlaying = true,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    } else {
                                        Text(
                                            text = "${index + 1}",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.secondaryTextColor
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp)
                                ) {
                                    Text(
                                        text = audio.title,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 16.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.primaryTextColor
                                    )
                                    
                                    if (audio.artist.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        
                                        Text(
                                            text = "${audio.artist} · ${audio.duration.formatDuration()}",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 14.sp
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            color = MaterialTheme.colorScheme.secondaryTextColor
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        
                                        Text(
                                            text = audio.duration.formatDuration(),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 14.sp
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            color = MaterialTheme.colorScheme.secondaryTextColor
                                        )
                                    }
                                }
                                
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            val newList = audioItems.toMutableList()
                                            newList.removeAt(index)
                                            audioItems = newList
                                            withIO { 
                                                AudioPlaylistPreference.putAsync(context, newList) 
                                                if (audio.path == currentPlayingPath) {
                                                    // If removing currently playing item
                                                    if (newList.isNotEmpty()) {
                                                        // Play next item if available
                                                        val nextItem = if (index < newList.size) newList[index] else newList[0]
                                                        AudioPlayer.justPlay(context, nextItem)
                                                    } else {
                                                        // Stop playback if playlist empty
                                                        AudioPlayingPreference.putAsync(context, "")
                                                        AudioPlayer.clear()
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .size(32.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.x),
                                        contentDescription = "Remove",
                                        tint = MaterialTheme.colorScheme.secondaryTextColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
} 