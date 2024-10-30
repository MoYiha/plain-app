package com.ismartcoding.plain.ui.theme

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.ismartcoding.plain.ui.theme.palette.core.ProvideZcamViewingConditions
import com.ismartcoding.plain.ui.theme.palette.dynamicDarkColorScheme
import com.ismartcoding.plain.ui.theme.palette.dynamicLightColorScheme

@Composable
fun AppTheme(
    useDarkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    ProvideZcamViewingConditions {
        CompositionLocalProvider(
            LocalTextStyle provides LocalTextStyle.current.applyTextDirection()
        ) {
            MaterialTheme(
                colorScheme =
                if (useDarkTheme) dynamicDarkColorScheme()
                else dynamicLightColorScheme(),
                typography = SystemTypography.applyTextDirection(),
                shapes = Shapes,
                content = content,
            )
        }
    }
}
