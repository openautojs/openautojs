package org.autojs.autojs.ui.compose.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat

@Immutable
data class ExtendedColors(
    val onBackgroundVariant: Color,
    val switchUncheckedThumbColor: Color,
    val divider: Color
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        onBackgroundVariant = Color.Unspecified,
        switchUncheckedThumbColor = Color.Unspecified,
        divider = Color.Unspecified
    )
}

object AutoXJsTheme {
    val colors: ExtendedColors
        @Composable
        get() = LocalExtendedColors.current
}


private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun AutoXJsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val context = view.context
            if (context is Activity){
                val window = context.window
                WindowCompat.setDecorFitsSystemWindows(window, false)
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()
//                ViewCompat.getWindowInsetsController(view)?.isAppearanceLightStatusBars = darkTheme
                WindowCompat.getInsetsController(context.window, context.window.decorView).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
        }
    }

    val darkExtendedColors = ExtendedColors(
        onBackgroundVariant = Color.Gray,
        switchUncheckedThumbColor = Color(0xFFA9A9A9) ,
        divider = Color(0xff262626)
    )
    val lightExtendedColors = ExtendedColors(
        onBackgroundVariant = Color(0xFF8D8D8D),
        switchUncheckedThumbColor = MaterialTheme.colorScheme.surface,
        divider = Color(0xFFF2F3F5)
    )
    CompositionLocalProvider(LocalExtendedColors provides if (darkTheme) darkExtendedColors else lightExtendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}

val ColorScheme.isLight get() = this.background.luminance() > 0.5