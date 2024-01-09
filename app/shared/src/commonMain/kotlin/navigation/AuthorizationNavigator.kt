package me.him188.ani.app.navigation

import me.him188.ani.app.platform.Context

interface AuthorizationNavigator {
    enum class AuthorizationResult {
        SUCCESS, CANCELLED
    }

    /**
     * 跳转到授权页面, 并等待授权结果.
     */
    suspend fun authorize(context: Context, optional: Boolean): AuthorizationResult
}
