package com.ismartcoding.plain.ui.base

import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ismartcoding.plain.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PIconButton(
    icon: Int,
    modifier: Modifier = Modifier,
    iconSize: Dp = 24.dp,
    tint: Color = LocalContentColor.current,
    contentDescription: String? = null,
    showBadge: Boolean = false,
    isHaptic: Boolean? = false,
    isSound: Boolean? = false,
    enabled: Boolean = true,
    click: () -> Unit = {},
) {
    val view = LocalView.current
    IconButton(
        modifier = modifier,
        enabled = enabled,
        colors = IconButtonDefaults.iconButtonColors().copy(
            contentColor = tint,
            disabledContentColor = tint.copy(alpha = 0.38f)
        ),
        onClick = {
            if (isHaptic == true) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            if (isSound == true) view.playSoundEffect(SoundEffectConstants.CLICK)
            click()
        },
    ) {
        if (showBadge) {
            BadgedBox(
                badge = {
                    Badge(
                        modifier = Modifier
                            .size(8.dp)
                            .offset(x = (1).dp, y = (-4).dp)
                            .clip(CircleShape),
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    )
                }
            ) {
                PIcon(
                    modifier = Modifier.size(iconSize),
                    icon = painterResource(icon),
                    contentDescription = contentDescription,
                )
            }
        } else {
            PIcon(
                modifier = Modifier.size(iconSize),
                icon = painterResource(icon),
                contentDescription = contentDescription,
            )
        }
    }
}

@Composable
fun IconShareButton(
    click: () -> Unit,
) {
    PIconButton(
        R.drawable.share_2,
        contentDescription = stringResource(R.string.share),
        click = click
    )
}

@Composable
fun IconLabelButton(
    click: () -> Unit,
) {
    PIconButton(
        R.drawable.label,
        contentDescription = stringResource(R.string.add_to_tags),
        click = click
    )
}


@Composable
fun IconLabelOffButton(
    click: () -> Unit,
) {
    PIconButton(
        R.drawable.label_off,
        contentDescription = stringResource(R.string.remove_from_tags),
        click = click
    )
}


@Composable
fun IconDeleteButton(
    click: () -> Unit,
) {
    PIconButton(
        R.drawable.delete_forever,
        contentDescription = stringResource(R.string.delete),
        click = click
    )
}

@Composable
fun IconRenameButton(
    click: () -> Unit,
) {
    PIconButton(
        R.drawable.square_pen,
        contentDescription = stringResource(R.string.rename),
        click = click
    )
}

@Composable
fun IconCutButton(
    click: () -> Unit,
) {
    PIconButton(
        R.drawable.scissors,
        contentDescription = stringResource(R.string.cut),
        click = click
    )
}

@Composable
fun IconCopyButton(
    click: () -> Unit,
) {
    PIconButton(
        R.drawable.copy,
        contentDescription = stringResource(R.string.copy),
        click = click
    )
}

@Composable
fun IconPlaylistAddButton(
    click: () -> Unit,
) {
    PIconButton(
        R.drawable.playlist_add,
        contentDescription = stringResource(R.string.add_to_playlist),
        click = click
    )
}

@Composable
fun IconRestoreButton(
    click: () -> Unit,
) {
    PIconButton(
        R.drawable.archive_restore,
        contentDescription = stringResource(R.string.restore),
        click = click
    )
}


@Composable
fun IconTrashButton(
    click: () -> Unit,
) {
    PIconButton(
        R.drawable.trash_2,
        contentDescription = stringResource(R.string.move_to_trash),
        click = click
    )
}

@Composable
fun IconZipButton(
    click: () -> Unit,
) {
    PIconButton(
        R.drawable.package2,
        contentDescription = stringResource(R.string.compress),
        click = click
    )
}


@Composable
fun IconUnzipButton(
    click: () -> Unit,
) {
    PIconButton(
        R.drawable.package_open,
        contentDescription = stringResource(R.string.decompress),
        click = click
    )
}





