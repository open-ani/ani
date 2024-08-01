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

package me.him188.ani.app.ui.profile

import androidx.annotation.UiThread
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import io.ktor.http.encodeURLParameter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import me.him188.ani.app.data.source.AniAuthClient
import me.him188.ani.app.data.source.session.ExternalOAuthRequest
import me.him188.ani.app.data.source.session.OAuthResult
import me.him188.ani.app.data.source.session.SessionManager
import me.him188.ani.app.data.source.session.isSessionVerified
import me.him188.ani.app.platform.currentAniBuildConfig
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.feedback.ErrorMessage
import me.him188.ani.utils.logging.debug
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.platform.Uuid
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Stable
class BangumiOAuthViewModel : AbstractViewModel(), KoinComponent {
    private val sessionManager: SessionManager by inject()
    private val client by lazy { AniAuthClient().also { addCloseable(it) } }

    /**
     * 当前授权是否正在进行中
     */
    val processingRequest = sessionManager.processingRequest

    /**
     * 需要进行授权
     */
    val needAuth by sessionManager.isSessionVerified.map { !it }.produceState(true)

    var requestIdFlow = MutableStateFlow(Uuid.randomString())

    /**
     * 当前是第几次尝试
     */
    val requestId by requestIdFlow.produceState()

    fun makeOAuthUrl(
        requestId: String,
    ): String {
        val base = currentAniBuildConfig.aniAuthServerUrl.removeSuffix("/")
        return "${base}/v1/login/bangumi/oauth?requestId=${requestId.encodeURLParameter()}"
    }

    val oauthUrl by derivedStateOf {
        makeOAuthUrl(requestId)
    }

    /**
     * 展示登录失败的错误
     */
    val authError: MutableStateFlow<ErrorMessage?> = sessionManager.processingRequest.map { request ->
        (request?.state?.value as? ExternalOAuthRequest.State.Failed)?.throwable?.let {
            ErrorMessage.simple("登录失败, 请重试", it)
        }
    }.localCachedStateFlow(null)

    suspend fun doCheckResult() {
        withContext(Dispatchers.Default) {
            while (true) {
                val resp = client.getResultOrNull(requestIdFlow.value)
                logger.info { "Check OAuth result: $resp" }
                if (resp != null) {
                    sessionManager.processingRequest.value?.onCallback(
                        Result.success(
                            OAuthResult(
                                accessToken = resp.accessToken,
                                refreshToken = resp.refreshToken,
                                expiresInSeconds = resp.expiresIn,
                            ),
                        ),
                    )
                }
                delay(1000)
            }
        }
    }

    /**
     * Set callback code. Only used by Desktop platform. For Android, see `MainActivity.onNewIntent`
     */
    private fun setCode(oAuthResult: OAuthResult) {

    }

    @UiThread
    fun dismissError() {
        logger.debug { "dismissError" }
        authError.value = null
        refresh()
    }

    @UiThread
    fun refresh() {
        logger.debug { "refresh" }
        requestIdFlow.value = Uuid.randomString()
    }

    fun onCancel() {
        sessionManager.processingRequest.value?.cancel()
    }
}
