package com.example.localdictationpro.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.localdictationpro.data.entities.Settings

@Composable
fun LocalDictationProTheme(
    settings: Settings?,
    content: @Composable () -> Unit
) {
    // 根据设置中的主题决定是否使用深色模式
    val darkTheme = when (settings?.theme) {
        "Dark" -> true
        "Light" -> false
        else -> isSystemInDarkTheme()  // 未设置时跟随系统
    }

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = PrimaryDark,
            onPrimary = OnPrimaryDark,
            primaryContainer = PrimaryVariantDark,
            secondary = SecondaryDark,
            onSecondary = OnSecondaryDark,
            background = BackgroundDark,
            onBackground = OnBackgroundDark,
            surface = SurfaceDark,
            onSurface = OnSurfaceDark,
            surfaceVariant = SurfaceVariantDark,
            onSurfaceVariant = OnSurfaceVariantDark,
            error = ErrorDark,
            onError = OnErrorDark
        )
    } else {
        lightColorScheme(
            primary = PrimaryLight,
            onPrimary = OnPrimaryLight,
            primaryContainer = PrimaryVariantLight,
            secondary = SecondaryLight,
            onSecondary = OnSecondaryLight,
            background = BackgroundLight,
            onBackground = OnBackgroundLight,
            surface = SurfaceLight,
            onSurface = OnSurfaceLight,
            surfaceVariant = SurfaceVariantLight,
            onSurfaceVariant = OnSurfaceVariantLight,
            error = ErrorLight,
            onError = OnErrorLight
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as androidx.activity.ComponentActivity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}