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

package me.him188.ani.android.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import me.him188.ani.app.data.source.session.SessionManager
import me.him188.ani.app.navigation.AniNavigator
import me.him188.ani.app.platform.notification.AndroidNotifManager
import me.him188.ani.app.platform.notification.AndroidNotifManager.Companion.EXTRA_REQUEST_CODE
import me.him188.ani.app.platform.notification.NotifManager
import me.him188.ani.app.platform.window.LocalPlatformWindow
import me.him188.ani.app.platform.window.PlatformWindow
import me.him188.ani.app.ui.foundation.widgets.LocalToaster
import me.him188.ani.app.ui.foundation.widgets.Toaster
import me.him188.ani.app.ui.main.AniApp
import me.him188.ani.app.ui.main.AniAppContent
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import org.koin.android.ext.android.inject
import org.koin.mp.KoinPlatformTools


class MainActivity : AniComponentActivity() {
    private val sessionManager: SessionManager by inject()
    private val logger = logger(MainActivity::class)

    private val aniNavigator = AniNavigator()

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        KoinPlatformTools.defaultContext().getOrNull()?.get<NotifManager>()?.let {
            val code = intent.getIntExtra(EXTRA_REQUEST_CODE, -1)
            if (code != -1) {
                logger.info { "onNewIntent requestCode: $code" }
                AndroidNotifManager.handleIntent(code)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            // 透明状态栏
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
            // 透明导航栏
            navigationBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
        )

        // 允许画到 system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val toaster = object : Toaster {
            override fun toast(text: String) {
                Toast.makeText(this@MainActivity, text, Toast.LENGTH_LONG).show()
            }
        }
        setContent {
            AniApp {
//                val viewModel = rememberViewModel { AniAppViewModel() }
//                when (viewModel.themeKind) {
//                    null -> {}
//                    ThemeKind.AUTO -> {
//                        SideEffect {
//                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
//                        }
//                    }
//
//                    ThemeKind.LIGHT -> {
//                        SideEffect {
//                            window.navigationBarColor = aniLightColorTheme().surfaceColorAtElevation(2.dp).toArgb()
//                            window.statusBarColor = aniLightColorTheme().onSurface.toArgb()
////                            window.decorView.systemUiVisibility =
//                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//                        }
//                    }
//
//                    ThemeKind.DARK -> {
//                        SideEffect {
//                            window.navigationBarColor = aniDarkColorTheme().surfaceColorAtElevation(2.dp).toArgb()
//                            window.statusBarColor = aniDarkColorTheme().onSurface.toArgb()
//                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//                        }
//                    }
//                }
                CompositionLocalProvider(
                    LocalToaster provides toaster,
                    LocalPlatformWindow provides remember {
                        PlatformWindow()
                    },
                ) {
                    AniAppContent(aniNavigator)
                }
            }
        }

        lifecycleScope.launch {
            runCatching {
                sessionManager.requireAuthorize(aniNavigator, navigateToWelcome = true)
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = window.insetsController ?: return
            if (hasFocus && requestedOrientation == android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                controller.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_DEFAULT
                } else {
                    @Suppress("DEPRECATION")
                    @SuppressLint("WrongConstant")
                    controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_BARS_BY_SWIPE
                }
            }
        }
    }
}
