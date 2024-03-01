package ca.uwaterloo.tunein.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import ca.uwaterloo.tunein.ui.theme.Color as Color1

private val DarkColors = darkColorScheme(
    primary = Color1.DarkGreen,
    onPrimary = Color1.TextWhite,
    primaryContainer = Color1.DarkGreen,
    onPrimaryContainer = Color1.TextWhite,
    secondary = Color1.LightGreen,
    onSecondary = Color1.TextWhite,
    secondaryContainer = Color1.LightGreen,
    onSecondaryContainer = Color1.TextWhite,
    tertiary = Color1.DarkGray,
    onTertiary = Color1.TextWhite,
    tertiaryContainer = Color1.MediumGray,
    onTertiaryContainer = Color1.TextWhite,
    error = Color(0xFFCF6679),
    errorContainer = Color(0xFFFFEBEE),
    onError = Color1.TextWhite,
    onErrorContainer = Color1.TextWhite,
    background = Color1.DarkGray,
    onBackground = Color1.TextWhite,
    surface = Color1.MediumGray,
    onSurface = Color1.TextWhite,
    surfaceVariant = Color1.LightGray,
    onSurfaceVariant = Color1.TextWhite,
    outline = Color(0xFF424242),
    inverseOnSurface = Color1.TextWhite,
    inverseSurface = Color1.DarkGray,
    inversePrimary = Color1.LightGreen,
    surfaceTint = Color(0xFF80CBC4),
    outlineVariant = Color(0xFFBDBDBD),
    scrim = Color(0xFF323232),
)

@Composable
fun TuneInTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (!useDarkTheme) {
        DarkColors // Since we're only supporting dark mode
    } else {
        DarkColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = {
            ProvideCustomContentColor(
                color = Color.White,
                content = content,
            )
        }
    )
}