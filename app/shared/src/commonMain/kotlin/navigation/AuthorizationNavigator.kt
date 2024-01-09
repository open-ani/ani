package me.him188.ani.app.navigation

interface AuthorizationNavigator {
    enum class AuthorizationResult {
        SUCCESS, CANCELLED
    }

    /**
     * 跳转到授权页面, 并等待授权结果.
     */
    suspend fun authorize(): AuthorizationResult
}
