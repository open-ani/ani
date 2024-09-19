/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.data.source.media.resolver

import me.him188.ani.app.data.models.preference.ProxyConfig
import me.him188.ani.app.data.models.preference.VideoResolverSettings
import me.him188.ani.app.platform.Context
import me.him188.ani.utils.platform.annotations.TestOnly

interface WebViewVideoExtractor {
    suspend fun <R : Any> getVideoResourceUrl(
        context: Context,
        pageUrl: String,
        resourceMatcher: (String) -> R?,
    ): R
}

expect fun WebViewVideoExtractor(
    proxyConfig: ProxyConfig?,
    videoResolverSettings: VideoResolverSettings,
): WebViewVideoExtractor

@TestOnly
class TestWebViewVideoExtractor(
    private val urls: (pageUrl: String) -> List<String>,
) : WebViewVideoExtractor {
    override suspend fun <R : Any> getVideoResourceUrl(
        context: Context,
        pageUrl: String,
        resourceMatcher: (String) -> R?
    ): R {
        urls(pageUrl).forEach {
            resourceMatcher(it)?.let { return it }
        }
        throw IllegalStateException("No match found")
    }
}
