/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.subject.components.comment

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.runBlocking
import me.him188.ani.app.platform.AniCefApp
import org.cef.CefClient
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.browser.CefRendering
import org.cef.browser.CefRequestContext
import org.cef.handler.CefResourceRequestHandlerAdapter
import org.cef.network.CefRequest
import java.awt.Component

@Stable
class DesktopTurnstileState(
    override val url: String,
) : TurnstileState {
    private var client: CefClient? = null
    private var browser: CefBrowser? = null

    var isDarkTheme: Boolean = false

    override val tokenFlow: MutableSharedFlow<String> = MutableSharedFlow(extraBufferCapacity = 1)

    private fun concatUrl(): String {
        return "${url}&theme=${if (isDarkTheme) "dark" else "light"}"
    }

    fun initializeBrowser(): Component = runBlocking {
        AniCefApp.suspendCoroutineOnCefContext {
            val newClient = AniCefApp.createClient()
                ?: throw IllegalStateException("AniCefApp should be initialized.")

            client = newClient

            val newBrowser = newClient.createBrowser(
                concatUrl(),
                CefRendering.DEFAULT,
                true,
                CefRequestContext.createContext { _, _, _, _, _, _, _ ->
                    object : CefResourceRequestHandlerAdapter() {
                        override fun onBeforeResourceLoad(
                            browser: CefBrowser?,
                            frame: CefFrame?,
                            request: CefRequest?,
                        ): Boolean {
                            val requestUrl = request?.url
                            println("Request URL: $requestUrl")
                            if (requestUrl != null &&
                                requestUrl.startsWith(TurnstileState.CALLBACK_INTERCEPTION_PREFIX)
                            ) {
                                tokenFlow.tryEmit(
                                    requestUrl.substringAfter(
                                        TurnstileState.CALLBACK_INTERCEPTION_PREFIX
                                    )
                                )
                                return true
                            }
                            return super.onBeforeResourceLoad(browser, frame, request)
                        }
                    }
                },
            )

            browser = newBrowser
            newBrowser.setCloseAllowed()
            newBrowser.uiComponent.apply { }
        }
    }

    override fun reload() {
        AniCefApp.runOnCefContext {
            browser?.loadURL(concatUrl())
        }
    }

    fun dispose() {
        AniCefApp.blockOnCefContext {
            browser?.close(true)
            client?.dispose()
        }
    }
}

actual fun createTurnstileState(url: String): TurnstileState {
    return DesktopTurnstileState(url)
}

@Composable
actual fun ActualTurnstile(
    state: TurnstileState,
    constraints: Constraints,
    modifier: Modifier,
) {
    check(state is DesktopTurnstileState)
    val isDark = isSystemInDarkTheme()

    SwingPanel(
        background = Color.Transparent,
        factory = {
            state.isDarkTheme = isDark
            state.initializeBrowser()
        },
        update = { component ->

        },
        modifier = modifier.fillMaxWidth().height(100.dp)
    )

    DisposableEffect(Unit) {
        onDispose {
            state.dispose()
        }
    }
}