package me.him188.ani.app.platform

import androidx.compose.runtime.Stable
import me.him188.ani.datasources.bangumi.BangumiClient

actual fun createBangumiClient(): BangumiClient {
    TODO("Not yet implemented")
}

@Stable
actual val currentAniBuildConfig: AniBuildConfig
    get() = TODO("Not yet implemented")