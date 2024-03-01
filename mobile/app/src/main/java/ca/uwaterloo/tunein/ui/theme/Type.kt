package ca.uwaterloo.tunein.ui.theme

import androidx.compose.material3.Typography

private val defaultTypography = Typography()
val Typography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(color = Color.TextWhite),
    displayMedium = defaultTypography.displayMedium.copy(color = Color.TextWhite),
    displaySmall = defaultTypography.displaySmall.copy(color = Color.TextWhite),

    headlineLarge = defaultTypography.headlineLarge.copy(color = Color.TextWhite),
    headlineMedium = defaultTypography.headlineMedium.copy(color = Color.TextWhite),
    headlineSmall = defaultTypography.headlineSmall.copy(color = Color.TextWhite),

    titleLarge = defaultTypography.titleLarge.copy(color = Color.TextWhite),
    titleMedium = defaultTypography.titleMedium.copy(color = Color.TextWhite),
    titleSmall = defaultTypography.titleSmall.copy(color = Color.TextWhite),

    bodyLarge = defaultTypography.bodyLarge.copy(color = Color.TextWhite),
    bodyMedium = defaultTypography.bodyMedium.copy(color = Color.TextWhite),
    bodySmall = defaultTypography.bodySmall.copy(color = Color.TextWhite),

    labelLarge = defaultTypography.labelLarge.copy(color = Color.TextWhite),
    labelMedium = defaultTypography.labelMedium.copy(color = Color.TextWhite),
    labelSmall = defaultTypography.labelSmall.copy(color = Color.TextWhite)
)