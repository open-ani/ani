package me.him188.ani.app.platform

import me.him188.ani.BuildConfig
import me.him188.ani.datasources.bangumi.BangumiClient

actual fun createBangumiClient(): BangumiClient {
    return BangumiClient.create(BuildConfig.BANGUMI_OAUTH_CLIENT_ID, BuildConfig.BANGUMI_OAUTH_CLIENT_SECRET)
}