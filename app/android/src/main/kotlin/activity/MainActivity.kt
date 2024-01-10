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

import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import me.him188.ani.app.session.AuthorizationCanceledException
import me.him188.ani.app.session.SessionManager
import me.him188.ani.app.ui.foundation.AniApp
import me.him188.ani.app.ui.home.MainScreen
import org.koin.android.ext.android.inject

class MainActivity : AniComponentActivity() {
    private val sessionManager: SessionManager by inject()

    private enum class AuthorizationState {
        PROCESSING,
        SUCCESS,
        CANCELLED
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
            // 当前授权状态
            var authorizationState by remember {
                mutableStateOf(
                    if (sessionManager.isSessionValid.value == true) {
                        AuthorizationState.SUCCESS // 已经登录
                    } else {
                        AuthorizationState.PROCESSING // 后台还在处理或者未登录
                    }
                )
            }

            AniApp(currentColorScheme) {
                when (authorizationState) {
                    AuthorizationState.PROCESSING -> {
                        LaunchedEffect(key1 = true) {
                            lifecycleScope.launch {
                                authorizationState = try {
                                    sessionManager.requireAuthorization(this@MainActivity, false)
                                    AuthorizationState.SUCCESS
                                } catch (e: AuthorizationCanceledException) {
                                    AuthorizationState.CANCELLED
                                }
                            }
                        }
                    }

                    AuthorizationState.SUCCESS -> {
                    }

                    AuthorizationState.CANCELLED -> {
//                        AlertDialog(
//                            onDismissRequest = {
//                                authorizationState = AuthorizationState.PROCESSING
//                            },
//                            confirmButton = {
//                                TextButton(onClick = {
//                                    authorizationState = AuthorizationState.PROCESSING
//                                }) {
//                                    Text(text = "继续")
//                                }
//                            },
//                            dismissButton = {
//                                TextButton(onClick = {
//                                    finish()
//                                }) {
//                                    Text(text = "退出")
//                                }
//                            },
//                            text = { Text(text = "您需要登录 Bangumi 账号才能使用 Ani") }
//                        )
                    }
                }

                MainScreen()
            }
        }
    }
}
