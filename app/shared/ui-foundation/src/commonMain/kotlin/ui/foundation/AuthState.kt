/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.foundation

import me.him188.ani.app.domain.session.AuthState
import me.him188.ani.app.domain.session.SessionManager
import me.him188.ani.app.domain.session.launchAuthorize
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


fun <T> T.AuthState(): AuthState
        where T : KoinComponent, T : AbstractViewModel {
    val sessionManager: SessionManager by inject()
    return AuthState(
        state = sessionManager.state.produceState(null),
        launchAuthorize = { navigator ->
            launchAuthorize(navigator)
        },
        retry = { sessionManager.retry() },
        backgroundScope,
    )
}
