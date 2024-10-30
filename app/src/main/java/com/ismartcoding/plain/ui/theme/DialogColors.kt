package com.ismartcoding.plain.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.ismartcoding.plain.enums.DarkTheme
import com.ismartcoding.plain.preference.LocalDarkTheme

/**
 * Centralized color definitions for dialogs
 */
object DialogColors {
    // Dialog background color that adapts to theme
    val containerColor: Color
        @Composable
        @ReadOnlyComposable
        get() = if (DarkTheme.isDarkTheme(LocalDarkTheme.current)) {
            // Slightly darker light color for dark mode (to avoid being too bright)
            Color(0xFFE8E8E8)
        } else {
            // Light white for light mode
            Color(0xFFF5F5F5)
        }
    
    // Text color for dialog title that adapts to theme
    val titleContentColor: Color
        @Composable
        @ReadOnlyComposable
        get() = if (DarkTheme.isDarkTheme(LocalDarkTheme.current)) {
            // Dark text on light background in dark mode
            Color(0xFF202124)
        } else {
            // Dark text in light mode
            Color(0xFF202124)
        }
    
    // Text color for dialog content that adapts to theme
    val textContentColor: Color
        @Composable
        @ReadOnlyComposable
        get() = if (DarkTheme.isDarkTheme(LocalDarkTheme.current)) {
            // Dark text on light background in dark mode
            Color(0xFF333333)
        } else {
            // Dark text in light mode
            Color(0xFF202124)
        }
} 