/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.domain.session

import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.him188.ani.app.navigation.AniNavigator
import me.him188.ani.app.tools.MonoTasker
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Stable
class AuthState(
    state: State<SessionStatus?>,
    val launchAuthorize: (navigator: AniNavigator) -> Unit,
    private val retry: suspend () -> Unit,
    backgroundScope: CoroutineScope,
) {
    val status by state

    val isKnownLoggedIn: Boolean by derivedStateOf { this.status is SessionStatus.Verified }
    val isKnownGuest: Boolean by derivedStateOf { this.status is SessionStatus.Guest }
    val isLoading: Boolean by derivedStateOf { this.status == null || this.status is SessionStatus.Loading }

    /**
     * 任何未登录成功的情况, 如网络错误
     */
    val isKnownLoggedOut: Boolean by derivedStateOf { this.status is SessionStatus.VerificationFailed }

    val isKnownExpired: Boolean by derivedStateOf { this.status is SessionStatus.NoToken || this.status is SessionStatus.Expired }

    private val retryTasker = MonoTasker(backgroundScope)
    fun retry() {
        retryTasker.launch {
            retry.invoke()
        }
    }
}

fun <T> T.launchAuthorize(navigator: AniNavigator)
        where T : KoinComponent {
    val sessionManager: SessionManager by inject()
    sessionManager.requireAuthorizeAsync(
        onLaunch = {
            withContext(Dispatchers.Main) { navigator.navigateBangumiOAuthOrTokenAuth() }
        },
        skipOnGuest = false,
    )  //  use SessionManager's lifecycle
}
