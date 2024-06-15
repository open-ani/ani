package me.him188.ani.app.platform

import androidx.compose.runtime.Stable
import me.him188.ani.BuildConfig

private object AniBuildConfigAndroid : AniBuildConfig {
    override val versionName: String
        get() = BuildConfig.VERSION_NAME
    override val bangumiOauthClientAppId: String
        get() = BuildConfig.BANGUMI_OAUTH_CLIENT_APP_ID
    override val bangumiOauthClientSecret: String
        get() = BuildConfig.BANGUMI_OAUTH_CLIENT_SECRET
    override val isDebug: Boolean
        get() = BuildConfig.DEBUG
}

@Stable
actual val currentAniBuildConfigImpl: AniBuildConfig
    get() = AniBuildConfigAndroid
