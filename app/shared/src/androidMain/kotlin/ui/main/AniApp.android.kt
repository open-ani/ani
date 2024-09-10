package me.him188.ani.app.ui.main

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import me.him188.ani.app.data.models.preference.DarkMode
import me.him188.ani.app.platform.findActivity
import me.him188.ani.app.ui.foundation.theme.aniColorScheme

@Composable
internal actual fun currentPlatformColorTheme(darkMode: DarkMode, useDynamicTheme: Boolean): ColorScheme {
    val isDark = when (darkMode) {
        DarkMode.LIGHT -> false
        DarkMode.DARK -> true
        DarkMode.AUTO -> isSystemInDarkTheme()
    }

    val activity = LocalContext.current.findActivity() as? ComponentActivity
    if (activity != null) {
        if (isDark) {
            activity.enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
                navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            )
        } else {
            activity.enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.light(
                    android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT,
                ),
                navigationBarStyle = SystemBarStyle.light(
                    android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT,
                ),
            )
        }
    }

//    val activity = LocalContext.current.findActivity()
//    // 切换系统状态栏风格
//    if (activity != null && darkMode != DarkMode.AUTO) {
//        DisposableEffect(activity, isDark) {
//            val defaultNightMode = AppCompatDelegate.getDefaultNightMode()
//            AppCompatDelegate.setDefaultNightMode(
//                if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO,
//            )
//
//            onDispose {
//                AppCompatDelegate.setDefaultNightMode(defaultNightMode)
//            }
//
////            
////            val insetsController = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
////
////            val appearanceLightStatusBars = insetsController.isAppearanceLightStatusBars
////            insetsController.isAppearanceLightStatusBars = darkMode == DarkMode.LIGHT
////            val appearanceLightNavigationBars = insetsController.isAppearanceLightNavigationBars
////            insetsController.isAppearanceLightNavigationBars = darkMode == DarkMode.LIGHT
////
////            onDispose {
////                insetsController.isAppearanceLightStatusBars = appearanceLightStatusBars
////                insetsController.isAppearanceLightNavigationBars = appearanceLightNavigationBars
////            }
//        }
//    }

    if (useDynamicTheme) {
        if (Build.VERSION.SDK_INT >= 31) {
            return if (isDark) {
                dynamicDarkColorScheme(LocalContext.current)
            } else {
                dynamicLightColorScheme(LocalContext.current)
            }
        }
    }

    return aniColorScheme(isDark)
}
