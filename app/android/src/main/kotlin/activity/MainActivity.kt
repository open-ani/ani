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
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import me.him188.ani.app.navigation.AniNavigator
import me.him188.ani.app.session.BangumiAuthorizationConstants
import me.him188.ani.app.session.OAuthResult
import me.him188.ani.app.session.SessionManager
import me.him188.ani.app.ui.foundation.AniApp
import me.him188.ani.app.ui.main.AniAppContent
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import org.koin.android.ext.android.inject


class MainActivity : AniComponentActivity() {
    private val sessionManager: SessionManager by inject()
    private val logger = logger(MainActivity::class)

    private val aniNavigator = AniNavigator()

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val data = intent.data ?: return

        val hasCallbackCode = data.queryParameterNames?.contains("code") == true
        if (hasCallbackCode) {
            val code = data.getQueryParameter("code")!!
            logger.info { "onNewIntent Receive code '$code', current processingRequest: ${sessionManager.processingRequest.value}" }
            sessionManager.processingRequest.value?.onCallback(
                Result.success(
                    OAuthResult(code, BangumiAuthorizationConstants.CALLBACK_URL)
                )
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            // 透明状态栏
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            // 透明导航栏
            navigationBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            )
        )

        // 允许画到 system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            AniApp(currentColorScheme) {
                AniAppContent(aniNavigator)
            }
        }

        lifecycleScope.launch {
            runCatching {
                sessionManager.requireOnline(aniNavigator)
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
