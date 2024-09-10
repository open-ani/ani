package me.him188.ani.app.ui.main

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import me.him188.ani.app.data.models.preference.DarkMode
import me.him188.ani.app.ui.foundation.theme.aniColorScheme
import me.him188.ani.app.ui.foundation.theme.aniDarkColorTheme
import me.him188.ani.app.ui.foundation.theme.aniLightColorTheme

@Composable
internal actual fun currentPlatformColorTheme(darkMode: DarkMode, useDynamicTheme: Boolean): ColorScheme {
    // does not support useDynamicTheme

    return when (darkMode) {
        DarkMode.LIGHT -> aniLightColorTheme()
        DarkMode.DARK -> aniDarkColorTheme()
        DarkMode.AUTO -> aniColorScheme(isSystemInDarkTheme())

        else -> aniColorScheme(isSystemInDarkTheme())
    }
}
