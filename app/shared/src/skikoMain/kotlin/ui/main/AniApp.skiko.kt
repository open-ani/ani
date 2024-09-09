package me.him188.ani.app.ui.main

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import me.him188.ani.app.data.models.preference.ThemeKind
import me.him188.ani.app.ui.foundation.theme.aniColorScheme
import me.him188.ani.app.ui.foundation.theme.aniDarkColorTheme
import me.him188.ani.app.ui.foundation.theme.aniLightColorTheme

@Composable
internal actual fun currentPlatformColorTheme(themeKind: ThemeKind?): ColorScheme {
    return when (themeKind) {
        ThemeKind.LIGHT -> aniLightColorTheme()
        ThemeKind.DARK -> aniDarkColorTheme()
        ThemeKind.DYNAMIC,
        ThemeKind.AUTO -> aniColorScheme(isSystemInDarkTheme())

        else -> aniColorScheme(isSystemInDarkTheme())
    }
}
