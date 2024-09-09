package me.him188.ani.app.ui.main

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import me.him188.ani.app.data.models.preference.ThemeKind
import me.him188.ani.app.ui.foundation.theme.aniColorScheme

@Composable
internal actual fun currentPlatformColorTheme(themeKind: ThemeKind?): ColorScheme {
    val isDark = isSystemInDarkTheme()
    return when (themeKind) {
        ThemeKind.DYNAMIC -> {
            if (Build.VERSION.SDK_INT >= 31) {
                if (isDark) {
                    dynamicDarkColorScheme(LocalContext.current)
                } else {
                    dynamicLightColorScheme(LocalContext.current)
                }
            } else {
                aniColorScheme(isDark)
            }
        }

        else -> aniColorScheme(isDark)
    }
}
