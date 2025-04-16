package com.ismartcoding.plain.ui.components.home

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.FilePresent
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.RssFeed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavHostController
import com.ismartcoding.plain.R
import com.ismartcoding.plain.enums.AppFeatureType
import com.ismartcoding.plain.ui.base.PCard
import com.ismartcoding.plain.ui.base.PIconTextButton
import com.ismartcoding.plain.ui.file.FilesDialog
import com.ismartcoding.plain.ui.nav.Routing

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeFeatures(
    navController: NavHostController,
    itemWidth: Dp,
) {
    PCard {
        HomeItemFlow {
            PIconTextButton(
                icon = Icons.AutoMirrored.Outlined.Article,
                stringResource(id = R.string.docs),
                modifier = Modifier.width(itemWidth),
            ) {
                navController.navigate(Routing.Docs)
            }
            PIconTextButton(
                icon = Icons.Outlined.FilePresent,
                stringResource(id = R.string.files),
                modifier = Modifier.width(itemWidth),
            ) {
                FilesDialog().show()
            }
            if (AppFeatureType.APPS.has()) {
                PIconTextButton(
                    icon = Icons.Outlined.Apps,
                    stringResource(id = R.string.apps),
                    modifier = Modifier.width(itemWidth),
                ) {
                    navController.navigate(Routing.Apps)
                }
            }
            PIconTextButton(
                icon = Icons.AutoMirrored.Outlined.Notes,
                stringResource(id = R.string.notes),
                modifier = Modifier.width(itemWidth),
            ) {
                navController.navigate(Routing.Notes)
            }
            PIconTextButton(
                icon = Icons.Outlined.RssFeed,
                stringResource(id = R.string.feeds),
                modifier = Modifier.width(itemWidth),
            ) {
                navController.navigate(Routing.FeedEntries(""))
            }
            PIconTextButton(
                icon = Icons.Outlined.GraphicEq,
                stringResource(id = R.string.sound_meter),
                modifier = Modifier.width(itemWidth),
            ) {
                navController.navigate(Routing.SoundMeter)
            }
        }
    }
}
