package me.him188.ani.app.platform

import androidx.compose.runtime.Stable
import me.him188.ani.app.foundation.BuildConfig


private object AniBuildConfigAndroid : AniBuildConfig {
    override val versionName: String
        get() = BuildConfig.VERSION_NAME
    override val isDebug: Boolean
        get() = BuildConfig.DEBUG
    override val aniAuthServerUrl: String
        get() = BuildConfig.ANI_AUTH_SERVER_URL
}

@Stable
@PublishedApi
internal actual val currentAniBuildConfigImpl: AniBuildConfig
    get() = AniBuildConfigAndroid
