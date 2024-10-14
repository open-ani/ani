/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.domain.mediasource.web

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import io.ktor.http.URLBuilder
import kotlinx.serialization.Serializable
import me.him188.ani.app.domain.mediasource.web.format.SelectorChannelFormat
import me.him188.ani.app.domain.mediasource.web.format.SelectorChannelFormatIndexGrouped
import me.him188.ani.app.domain.mediasource.web.format.SelectorChannelFormatNoChannel
import me.him188.ani.app.domain.mediasource.web.format.SelectorFormatConfig
import me.him188.ani.app.domain.mediasource.web.format.SelectorFormatId
import me.him188.ani.app.domain.mediasource.web.format.SelectorSubjectFormat
import me.him188.ani.app.domain.mediasource.web.format.SelectorSubjectFormatA
import me.him188.ani.app.domain.mediasource.web.format.SelectorSubjectFormatIndexed
import me.him188.ani.app.domain.mediasource.web.format.SelectorSubjectFormatJsonPathIndexed
import me.him188.ani.app.domain.mediasource.web.format.parseOrNull
import org.intellij.lang.annotations.Language

@Immutable
@Serializable
data class SelectorSearchConfig(
    // Phase 1, search
    val searchUrl: String = "", // required
    val searchUseOnlyFirstWord: Boolean = true,
    val rawBaseUrl: String = "", // if empty, guess
    // Phase 2, for search result, select subjects
    val subjectFormatId: SelectorFormatId = SelectorSubjectFormatA.id,
    val selectorSubjectFormatA: SelectorSubjectFormatA.Config = SelectorSubjectFormatA.Config(),
    val selectorSubjectFormatIndexed: SelectorSubjectFormatIndexed.Config = SelectorSubjectFormatIndexed.Config(),
    val selectorSubjectFormatJsonPathIndexed: SelectorSubjectFormatJsonPathIndexed.Config = SelectorSubjectFormatJsonPathIndexed.Config(),
    // Phase 3, for each subject, select channels
    val channelFormatId: SelectorFormatId = SelectorChannelFormatNoChannel.id,
    val selectorChannelFormatFlattened: SelectorChannelFormatIndexGrouped.Config = SelectorChannelFormatIndexGrouped.Config(),
    val selectorChannelFormatNoChannel: SelectorChannelFormatNoChannel.Config = SelectorChannelFormatNoChannel.Config(),
//    /**
//     * Regex. Group names:
//     * - `<ch>`: channel name
//     * - `<ep>`: episode name
//     *
//     * E.g. 用于匹配 "线路1 第1集":
//     * ```regex
//     * (?<ch>.+)\s*第(?<ep>\d+)集
//     * ```
//     *
//     * 匹配方式为 find 而不是 matchEntire.
//     * @see SelectorChannelFormat.FLATTENED
//     */
//    val matchChannelFromEpisodeText: String = "",
//    val selectNameFromEpisode: String = "",
//    val selectPlayUrlFromEpisode: String = "",


    // Search done. Now we should have Medias.
    val filterByEpisodeSort: Boolean = true,
    val filterBySubjectName: Boolean = true,

    // When playing a media:
    val selectMedia: SelectMediaConfig = SelectMediaConfig(),
    val matchVideo: MatchVideoConfig = MatchVideoConfig(),
) { // TODO: add Engine version capabilities
    val finalBaseUrl by lazy(LazyThreadSafetyMode.PUBLICATION) {
        rawBaseUrl.ifBlank { guessBaseUrl(searchUrl) }
    }

    @Serializable
    @Suppress("RegExpRedundantEscape")
    data class MatchVideoConfig(
        val enableNestedUrl: Boolean = true,
        @Language("regexp")
        val matchNestedUrl: String = """^.+(m3u8|vip|xigua\.php).+\?""",
        @Language("regexp")
        val matchVideoUrl: String = """(^http(s)?:\/\/(?!.*http(s)?:\/\/).+((\.mp4)|(\.mkv)|(m3u8)).*(\?.+)?)|(akamaized)|(bilivideo.com)""",
        val cookies: String = """quality=1080""",
        val addHeadersToVideo: VideoHeaders = VideoHeaders(),
    ) {
        val matchNestedUrlRegex by lazy {
            Regex.parseOrNull(matchNestedUrl)
        }
        val matchVideoUrlRegex by lazy {
            Regex.parseOrNull(matchVideoUrl)
        }
    }

    @Serializable
    data class VideoHeaders(
        val referer: String = "",
        val userAgent: String = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3",
    )

    @Serializable
    data class SelectMediaConfig(
        val distinguishSubjectName: Boolean = true,
        val distinguishChannelName: Boolean = true,
    )


//    val matchChannelFromEpisodeRegex by lazy(LazyThreadSafetyMode.PUBLICATION) {
//        matchChannelFromEpisodeText.toRegex()
//    }

    // These classes are nested to limit namespace

    @Stable
    companion object {
        @Stable
        val Empty = SelectorSearchConfig()

        fun guessBaseUrl(searchUrl: String): String {
            return kotlin.runCatching {
                URLBuilder(searchUrl).apply {
                    pathSegments = emptyList()
                    parameters.clear()
                }.toString()
            }.getOrElse {
                val schemaIndex = searchUrl.indexOf("//")
                if (schemaIndex == -1) {
                    searchUrl.removeSuffix("/")
                } else {
                    val slashIndex = searchUrl.indexOf('/', startIndex = schemaIndex + 2)
                    if (slashIndex == -1) {
                        searchUrl.removeSuffix("/")
                    } else {
                        searchUrl.substring(0, slashIndex)
                    }
                }
            }
        }
    }
}

/**
 * 获取该 [SelectorSubjectFormat] 的配置 [C].
 */
fun <C : SelectorFormatConfig> SelectorSearchConfig.getFormatConfig(format: SelectorSubjectFormat<C>): C {
    @Suppress("UNCHECKED_CAST")
    return when (format) {
        SelectorSubjectFormatA -> selectorSubjectFormatA as C
        SelectorSubjectFormatIndexed -> selectorSubjectFormatIndexed as C
        SelectorSubjectFormatJsonPathIndexed -> selectorSubjectFormatJsonPathIndexed as C
    }
}

/**
 * 获取该 [SelectorChannelFormat] 的配置 [C].
 */
fun <C : SelectorFormatConfig> SelectorSearchConfig.getFormatConfig(format: SelectorChannelFormat<C>): C {
    @Suppress("UNCHECKED_CAST")
    return when (format) {
        SelectorChannelFormatIndexGrouped -> selectorChannelFormatFlattened as C
        SelectorChannelFormatNoChannel -> selectorChannelFormatNoChannel as C
    }
}
