package com.github.heroslender.lgtvcontroller.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Identidade visual única do app: escuro e minimalista.
 * O app ignora o tema claro e as cores dinâmicas do Android de propósito,
 * para que todas as telas fiquem iguais à tela do controle.
 */
object AppColors {
    val Background = Color(0xFF000000)
    val Surface = Color(0xFF1C1C1E)
    val SurfaceHigh = Color(0xFF2C2C2E)
    val SurfaceHighest = Color(0xFF3A3A3C)
    val Separator = Color(0xFF38383A)
    val Content = Color(0xFFFFFFFF)
    val ContentSecondary = Color(0xFF8E8E93)
    val ContentTertiary = Color(0xFF48484A)
    val Accent = Color(0xFF0A84FF)
    val Green = Color(0xFF30D158)
    val Yellow = Color(0xFFFFD60A)
    val Red = Color(0xFFFF453A)
}

private val appScheme = darkColorScheme(
    primary = AppColors.Accent,
    onPrimary = Color.White,
    primaryContainer = AppColors.SurfaceHigh,
    onPrimaryContainer = AppColors.Content,
    secondary = AppColors.ContentSecondary,
    onSecondary = Color.Black,
    secondaryContainer = AppColors.SurfaceHigh,
    onSecondaryContainer = AppColors.Content,
    tertiary = AppColors.Accent,
    onTertiary = Color.White,
    tertiaryContainer = AppColors.SurfaceHigh,
    onTertiaryContainer = AppColors.Content,
    error = AppColors.Red,
    onError = Color.White,
    errorContainer = Color(0xFF3A1A1A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = AppColors.Background,
    onBackground = AppColors.Content,
    surface = AppColors.Background,
    onSurface = AppColors.Content,
    surfaceVariant = AppColors.SurfaceHigh,
    onSurfaceVariant = AppColors.ContentSecondary,
    outline = AppColors.SurfaceHighest,
    outlineVariant = AppColors.Separator,
    scrim = Color.Black,
    inverseSurface = AppColors.Content,
    inverseOnSurface = AppColors.Surface,
    inversePrimary = AppColors.Accent,
    surfaceDim = AppColors.Background,
    surfaceBright = AppColors.SurfaceHigh,
    surfaceContainerLowest = AppColors.Background,
    surfaceContainerLow = AppColors.Surface,
    surfaceContainer = AppColors.Surface,
    surfaceContainerHigh = AppColors.SurfaceHigh,
    surfaceContainerHighest = AppColors.SurfaceHigh,
)

@Composable
fun LGTVControllerTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = true,
    @Suppress("UNUSED_PARAMETER") dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = appScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content,
    )
}
