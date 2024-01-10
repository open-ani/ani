package me.him188.ani.app.navigation

import me.him188.ani.app.platform.Context

interface AuthorizationNavigator {
    enum class AuthorizationResult {
        SUCCESS, CANCELLED
    }

    /**
     * OAuth 回调 URL.
     */
    val authorizationCallbackUrl: String

    /**
     * 跳转到授权页面, 并等待授权结果.
     */
    suspend fun navigateToAuthorization(context: Context, optional: Boolean): AuthorizationResult
}
