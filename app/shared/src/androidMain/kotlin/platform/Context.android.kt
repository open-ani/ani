/*
 * Ani
 * Copyright (C) 2022-2024 Him188
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.him188.ani.app.platform

import android.app.Activity
import android.os.Build
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File


actual typealias Context = android.content.Context

actual val LocalContext: ProvidableCompositionLocal<Context>
    get() = androidx.compose.ui.platform.LocalContext

@Composable
actual fun isInLandscapeMode(): Boolean =
    LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

@Suppress("USELESS_CAST") // compiler bug
actual fun Context.setRequestFullScreen(fullscreen: Boolean) {
    Log.i("setRequestFullScreen", "Requesting fullscreen: $fullscreen, context=$this")
    if (this is Activity) {
        if (fullscreen) {
            // go landscape
            requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

            // hide bars
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.hide(WindowInsets.Type.statusBars().or(WindowInsets.Type.navigationBars()))
                window.insetsController?.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                val decorView = window.decorView
                @Suppress("DEPRECATION")
                decorView.systemUiVisibility =
                    (View.SYSTEM_UI_FLAG_IMMERSIVE // Set the content to appear under the system bars so that the
                            // content doesn't resize when the system bars hide and show.
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // Hide the nav bar and status bar
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN)
            }

            // keep screen on
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            // cancel landscape
            requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

            // show bars
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.show(WindowInsets.Type.statusBars().or(WindowInsets.Type.navigationBars()))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    window.insetsController?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_DEFAULT
                }
            } else {
                val decorView = window.decorView
                @Suppress("DEPRECATION")
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE
                @Suppress("DEPRECATION")
                (this as Activity).window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            }

            // don't keep screen on
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    } else {
        val orientation = if (fullscreen) {
            android.content.res.Configuration.ORIENTATION_LANDSCAPE
        } else {
            android.content.res.Configuration.ORIENTATION_PORTRAIT
        }
        resources.configuration.orientation = orientation
    }
}


internal actual val Context.filesImpl: ContextFiles
    get() = object : ContextFiles {
        override val cacheDir: File get() = this@filesImpl.cacheDir ?: File("") // can be null when previewing
        override val dataDir: File get() = this@filesImpl.filesDir ?: File("") // can be null when previewing
    }

// TODO: isSystemInFullscreen is written by ChatGPT, not tested
@Composable
actual fun isSystemInFullscreenImpl(): Boolean {
    val context = LocalContext.current
    var isFullscreen by remember { mutableStateOf(isInFullscreenMode(context)) }

    DisposableEffect(context) {
        val window = (context as? Activity)?.window
        val decorView = window?.decorView

        val listener = View.OnApplyWindowInsetsListener { _, insets ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val isFullscreenNow = !insets.isVisible(WindowInsets.Type.systemBars())
                if (isFullscreenNow != isFullscreen) {
                    isFullscreen = isFullscreenNow
                }
            } else {
                val isFullscreenNow = (insets.systemWindowInsetTop == 0)
                if (isFullscreenNow != isFullscreen) {
                    isFullscreen = isFullscreenNow
                }
            }
            insets
        }

        if (decorView != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                decorView.setOnApplyWindowInsetsListener(listener)
            } else {
                ViewCompat.setOnApplyWindowInsetsListener(decorView) { v, insets ->
                    val toWindowInsets = insets.toWindowInsets()!!
                    listener.onApplyWindowInsets(v, toWindowInsets)
                    WindowInsetsCompat.toWindowInsetsCompat(toWindowInsets)
                }
            }
        }

        onDispose {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                decorView?.setOnApplyWindowInsetsListener(null)
            } else {
                if (decorView != null) {
                    ViewCompat.setOnApplyWindowInsetsListener(decorView, null)
                }
            }
        }
    }

    return isFullscreen
}

@Suppress("DEPRECATION")
private fun isInFullscreenMode(context: Context): Boolean {
    val window = (context as? Activity)?.window ?: return false
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val insetsController = window.insetsController
        insetsController != null && insetsController.systemBarsBehavior == BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    } else {
        val decorView = window.decorView
        (decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_FULLSCREEN) != 0
    }
}