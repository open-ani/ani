package me.him188.ani.app.session

import io.ktor.http.encodeURLParameter
import me.him188.ani.app.platform.currentAniBuildConfig

object BangumiAuthorizationConstants {
    /**
     * Note on Desktop platforms, this is not used.
     */
    const val CALLBACK_URL = "ani://bangumi-oauth-callback"

    fun makeOAuthUrl(
        clientId: String = currentAniBuildConfig.bangumiOauthClientId,
        callbackUrl: String = CALLBACK_URL,
    ): String {
        return "https://bgm.tv/oauth/authorize" +
                "?client_id=${clientId}" +
                "&response_type=code" +
                "&redirect_uri=" + callbackUrl.encodeURLParameter()
    }
}