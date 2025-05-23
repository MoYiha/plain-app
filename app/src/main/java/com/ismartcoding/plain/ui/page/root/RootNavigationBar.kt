package com.ismartcoding.plain.ui.page.root

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ismartcoding.plain.R
import com.ismartcoding.plain.ui.theme.navBarBackground
import com.ismartcoding.plain.ui.theme.navBarUnselectedColor

@Composable
fun RootNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
) {
    val navBarColor = MaterialTheme.colorScheme.navBarBackground
    val unselectedColor = MaterialTheme.colorScheme.navBarUnselectedColor
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(navBarColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { }
            .padding(top = 8.dp) // Add some top padding
            .navigationBarsPadding(), // This will automatically avoid the system navigation gesture area
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { onTabSelected(RootTabType.HOME.value) },
            modifier = Modifier.size(48.dp) // Slightly larger for better tap targets
        ) {
            Icon(
                painterResource(R.drawable.house),
                contentDescription = stringResource(R.string.home),
                modifier = Modifier.size(26.dp), // Larger icons for better visibility
                tint = if (selectedTab == RootTabType.HOME.value) 
                    MaterialTheme.colorScheme.primary 
                else 
                    unselectedColor
            )
        }
        
        IconButton(
            onClick = { onTabSelected(RootTabType.IMAGES.value) },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                painterResource(R.drawable.image),
                contentDescription = stringResource(R.string.images),
                modifier = Modifier.size(26.dp),
                tint = if (selectedTab == RootTabType.IMAGES.value) 
                    MaterialTheme.colorScheme.primary 
                else 
                    unselectedColor
            )
        }
        
        IconButton(
            onClick = { onTabSelected(RootTabType.AUDIO.value) },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                painterResource(R.drawable.music),
                contentDescription = stringResource(R.string.audios),
                modifier = Modifier.size(26.dp),
                tint = if (selectedTab == RootTabType.AUDIO.value) 
                    MaterialTheme.colorScheme.primary 
                else 
                    unselectedColor
            )
        }
        
        IconButton(
            onClick = { onTabSelected(RootTabType.VIDEOS.value) },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                painterResource(R.drawable.video),
                contentDescription = stringResource(R.string.videos),
                modifier = Modifier.size(26.dp),
                tint = if (selectedTab == RootTabType.VIDEOS.value) 
                    MaterialTheme.colorScheme.primary 
                else 
                    unselectedColor
            )
        }
    }
}