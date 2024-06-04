package me.him188.ani.datasources.api.matcher

import me.him188.ani.datasources.api.Media

/**
 * 匹配 WebView 拦截到的资源.
 */
interface WebVideoMatcher { // SPI service load
    fun match(
        url: String,
        context: WebVideoMatcherContext
    ): WebVideo?
}

class WebVideoMatcherContext(
    val media: Media,
)

data class WebVideo(
    /**
     * 视频数据地址
     */
    val m3u8Url: String,
    /**
     * 请求视频数据时需要的 headers
     *
     * 建议提供:
     *
     * - `User-Agent`
     * - `Referer`
     */
    val headers: Map<String, String>
)