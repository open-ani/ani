/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.him188.ani.app.data.source.session.AuthorizationCancelledException
import me.him188.ani.app.data.source.session.SessionManager
import me.him188.ani.app.navigation.AniNavigator
import kotlin.coroutines.cancellation.CancellationException

object AppStartupTasks {
    suspend fun verifySession(
        sessionManager: SessionManager,
        navigator: AniNavigator,
    ) {
        try {
            sessionManager.requireAuthorize(
                onLaunch = {
                    navigator.awaitNavController()
                    withContext(Dispatchers.Main) {
                        navigator.navigateWelcome()
                    }
                    // 打开 welcome page 一定代表账号验证失败或者没有账号，直接取消协程是可以的
                    throw CancellationException("Navigates to welcome page on first launch.")
                },
                skipOnGuest = true,
            )
        } catch (e: AuthorizationCancelledException) {
            // 如果验证失败的原因是 CancellationException，那可能是用户手动取消了验证或是上方首次启动的抛出
            if (e.cause is CancellationException) return
            throw IllegalStateException("Failed to automatically log in on startup, see cause", e)
        } catch (e: Throwable) {
            throw IllegalStateException("Failed to automatically log in on startup, see cause", e)
        }
    }
}