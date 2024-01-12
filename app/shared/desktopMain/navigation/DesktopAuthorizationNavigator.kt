package me.him188.ani.app.navigation

import me.him188.ani.app.platform.Context

class DesktopAuthorizationNavigator : AuthorizationNavigator {
    override val authorizationCallbackUrl: String
        get() = "ani://bangumi-oauth-callback"

    override suspend fun navigateToAuthorization(
        context: Context,
        optional: Boolean
    ): AuthorizationNavigator.AuthorizationResult {
//        Desktop.getDesktop().browse(URI.create(url))
        TODO()
    }
}