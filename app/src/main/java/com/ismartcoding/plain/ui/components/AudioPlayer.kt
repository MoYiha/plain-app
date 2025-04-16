package com.ismartcoding.plain.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ismartcoding.lib.channel.receiveEventHandler
import com.ismartcoding.lib.extensions.isGestureInteractionMode
import com.ismartcoding.lib.helpers.CoroutinesHelper.withIO
import com.ismartcoding.plain.R
import com.ismartcoding.plain.data.DPlaylistAudio
import com.ismartcoding.plain.enums.AudioAction
import com.ismartcoding.plain.features.AudioActionEvent
import com.ismartcoding.plain.features.AudioPlayer
import com.ismartcoding.plain.preference.AudioPlayingPreference
import com.ismartcoding.plain.ui.page.audio.AudioPlayerPage
import com.ismartcoding.plain.ui.page.audio.AudioPlaylistPage
import com.ismartcoding.plain.ui.page.audio.SleepTimerPage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun AudioPlayerBar(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var visible by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("") }
    var progress by remember { mutableFloatStateOf(0f) }
    var duration by remember { mutableFloatStateOf(1f) }
    val isPlaying by AudioPlayer.isPlayingFlow.collectAsState()
    var showPlayer by remember { mutableStateOf(false) }
    var showSleepTimer by remember { mutableStateOf(false) }
    var showPlaylist by remember { mutableStateOf(false) }

    // Function to update the UI
    val updateUI = {
        scope.launch {
            val path = withIO { AudioPlayingPreference.getValueAsync(context) }
            visible = path.isNotEmpty()

            if (visible) {
                val audio = DPlaylistAudio.fromPath(context, path)
                title = audio.title
                artist = audio.artist
                duration = audio.duration.toFloat()
                progress = AudioPlayer.playerProgress / 1000f
            }
        }
    }

    // Store the event handler job to be cancelled on dispose
    var eventJob by remember { mutableStateOf<Job?>(null) }

    // Set up event listener for audio player events,仅监听MEDIA_ITEM_TRANSITION
    LaunchedEffect(Unit) {
        eventJob = receiveEventHandler<AudioActionEvent> { event ->
            when (event.action) {
                AudioAction.MEDIA_ITEM_TRANSITION -> {
                    updateUI()
                }
                else -> {}
            }
        }

        // Initial UI update
        updateUI()
    }

    // Dispose the event handler when component is disposed
    DisposableEffect(Unit) {
        onDispose {
            eventJob?.cancel()
        }
    }

    // Progress update job
    var progressUpdateJob: Job? = null

    LaunchedEffect(isPlaying) {
        progressUpdateJob?.cancel()

        if (isPlaying) {
            progressUpdateJob = scope.launch {
                while (isActive) {
                    progress = AudioPlayer.playerProgress / 1000f
                    delay(1000)
                }
            }
        }
    }

    // Dispose the progress update job when the component is disposed
    DisposableEffect(Unit) {
        onDispose {
            progressUpdateJob?.cancel()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically { it },
        exit = slideOutVertically { it },
        modifier = modifier
    ) {
        // Check if device is using gesture navigation
        val isGestureMode = context.isGestureInteractionMode()
        
        ElevatedCard(
            modifier = Modifier
                .padding(
                    start = 12.dp, 
                    end = 12.dp, 
                    // Adjust bottom padding based on navigation mode
                    bottom = if (isGestureMode) 16.dp else 8.dp
                )
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 6.dp
            ),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = { if (duration == 0f) 0f else  progress / duration },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .weight(1f)
                            .clickable { showPlayer = true }
                            .padding(8.dp)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = artist,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (isPlaying) {
                                AudioPlayer.pause()
                            } else {
                                AudioPlayer.play()
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .shadow(2.dp, CircleShape)
                            .clip(CircleShape)
                            .background(
                                if (isPlaying)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.primary
                            )
                    ) {
                        Icon(
                            painter = painterResource(id = if (isPlaying) R.drawable.pause else R.drawable.play_arrow),
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = if (isPlaying)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { showPlaylist = true },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.list_music),
                            contentDescription = "Queue",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }

    if (showPlayer) {
        AudioPlayerPage(
            onDismissRequest = { showPlayer = false }
        )
    }

    if (showSleepTimer) {
        SleepTimerPage(
            onDismissRequest = { showSleepTimer = false }
        )
    }

    if (showPlaylist) {
        AudioPlaylistPage(
            onDismissRequest = { showPlaylist = false }
        )
    }
} 