package me.him188.ani.app.platform

import io.ktor.client.plugins.UserAgent
import me.him188.ani.BuildConfig
import me.him188.ani.datasources.bangumi.BangumiClient

actual fun createBangumiClient(): BangumiClient {
    return BangumiClient.create(
        currentAniBuildConfig.bangumiOauthClientId,
        currentAniBuildConfig.bangumiOauthClientSecret
    ) {
        install(UserAgent) {
            agent = getAniUserAgent(currentAniBuildConfig.versionName)
        }
    }
}

private object AniBuildConfigAndroid : AniBuildConfig {
    override val versionName: String
        get() = BuildConfig.VERSION_NAME
    override val bangumiOauthClientId: String
        get() = BuildConfig.BANGUMI_OAUTH_CLIENT_ID
    override val bangumiOauthClientSecret: String
        get() = BuildConfig.BANGUMI_OAUTH_CLIENT_SECRET
}

actual val currentAniBuildConfig: AniBuildConfig
    get() = AniBuildConfigAndroid
