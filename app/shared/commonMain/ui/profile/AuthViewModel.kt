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
import androidx.compose.runtime.mutableIntStateOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import me.him188.ani.app.session.SessionManager
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.utils.logging.debug
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Stable
class AuthViewModel : AbstractViewModel(), KoinComponent {
    private val sessionManager: SessionManager by inject()

    /**
     * 当前授权是否正在进行中
     */
    val isProcessing = sessionManager.processingAuth

    /**
     * 需要进行授权
     */
    val needAuth: StateFlow<Boolean> = sessionManager.isSessionValid.map { it != true }.stateInBackground(false)

    /**
     * 当前是第几次尝试
     */
    val retryCount = mutableIntStateOf(0)

    private val _error: MutableStateFlow<String?> = MutableStateFlow(null)

    /**
     * 展示登录失败的错误
     */
    val authError: StateFlow<String?> = _error

    suspend fun setCode(code: String, callbackUrl: String) {
        runCatching {
            withContext(Dispatchers.IO) {
                sessionManager.refreshSessionByCode(
                    code,
                    callbackUrl
                )
            }
        }.onFailure {
            onAuthFailed(it)
        }
    }

    fun onAuthFailed(throwable: Throwable) {
        _error.value = "登录失败, 请重试\n\n错误信息: ${throwable.stackTraceToString()}"
    }

    @UiThread
    fun dismissError() {
        logger.debug { "dismissError" }
        _error.value = null
        retryCount.intValue++
    }

    fun refresh() {
        logger.debug { "refresh" }
        retryCount.intValue++
    }
}
