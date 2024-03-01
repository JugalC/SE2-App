package ca.uwaterloo.tunein.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Custom composition local for overriding the default content color.
 */
private val LocalCustomContentColor = compositionLocalOf<Color> {
    error("No CustomContentColor provided")
}

/**
 * Composable function to provide a custom content color.
 */
@Composable
fun ProvideCustomContentColor(
    color: Color,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalCustomContentColor provides color) {
        content()
    }
}

/**
 * Get the current custom content color.
 */
@Composable
fun getCustomContentColor(): Color {
    return LocalCustomContentColor.current
}
