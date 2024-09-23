/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.data.source.media.source.web.format

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import me.him188.ani.app.data.source.media.source.web.SelectorMediaSourceEngine
import me.him188.ani.app.data.source.media.source.web.WebSearchEpisodeInfo
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.utils.xml.Element
import me.him188.ani.utils.xml.QueryParser
import me.him188.ani.utils.xml.parseSelectorOrNull
import org.intellij.lang.annotations.Language

/**
 * 决定如何匹配线路和剧集
 * @see SelectorMediaSourceEngine
 */
sealed class SelectorChannelFormat<in Config : SelectorFormatConfig>(override val id: SelectorFormatId) :
    SelectorFormat {
    /**
     * @return `null` for invalid config
     * @see baseUrl must not end with `/`
     */
    abstract fun select(
        page: Element,
        baseUrl: String,
        config: Config,
    ): SelectedChannelEpisodes?

    companion object {
        val entries by lazy { // 必须 lazy, 否则可能获取到 null
            listOf(SelectorChannelFormatNoChannel, SelectorChannelFormatFlattened)
        }

        fun findById(id: SelectorFormatId): SelectorChannelFormat<*>? {
            return entries.find { it.id == id }
        }

        @Language("regexp")
        const val DEFAULT_MATCH_EPISODE_SORT_FROM_NAME = """第\s*(?<ep>.+)\s*[话集]"""

        fun isPossiblyMovie(title: String): Boolean {
            return ("简" in title || "繁" in title)
                    && ("2160P" in title || "1440P" in title || "2K" in title || "4K" in title || "1080P" in title || "720P" in title)
        }
    }
}

data class SelectedChannelEpisodes(
    /**
     * `null` 表示该 format 不支持 (不考虑) channels
     */
    val channels: List<String>?,
    val episodes: List<WebSearchEpisodeInfo>,
)


/**
 * 每个剧集标题内包含了线路名称. 例如 "主线第1集"
 *
 * 解析方式:
 */
data object SelectorChannelFormatFlattened :
    SelectorChannelFormat<SelectorChannelFormatFlattened.Config>(SelectorFormatId("flattened")) {
    @Immutable
    @Serializable
    data class Config(
        @Language("css")
        val selectChannels: String = "body > div.box-width.cor5 > div.anthology.wow.fadeInUp.animated > div.anthology-tab.nav-swiper.b-b.br div.swiper-wrapper a.swiper-slide",
        @Language("css")
        val selectLists: String = "body > div.box-width.cor5 > div.anthology.wow.fadeInUp.animated > a",
        @Language("css")
        val selectElements: String = "a",
        @Language("regexp")
        val matchEpisodeSortFromName: String = DEFAULT_MATCH_EPISODE_SORT_FROM_NAME,
    ) : SelectorFormatConfig {
        override fun isValid(): Boolean {
            return selectChannels.isNotBlank() && selectLists.isNotBlank() && selectElements.isNotBlank() && matchEpisodeSortFromName.isNotBlank()
        }
    }

    override fun select(
        page: Element,
        baseUrl: String,
        config: Config,
    ): SelectedChannelEpisodes? {
        val selectChannels = QueryParser.parseSelectorOrNull(config.selectChannels) ?: return null
        val selectElements = QueryParser.parseSelectorOrNull(config.selectElements) ?: return null
        val selectLists = QueryParser.parseSelectorOrNull(config.selectLists) ?: return null
        val matchEpisodeSortFromNameRegex = Regex.parseOrNull(config.matchEpisodeSortFromName) ?: return null

        val channels = page.select(selectChannels)
            .map { e -> e.text().trim() }

        fun parseEps(ep: Element, channel: String?): List<WebSearchEpisodeInfo> {
            return ep.select(selectElements).mapNotNull { a ->
                val text = a.text()
                if (text in channels) return@mapNotNull null

                val href = a.attr("title").takeIf { it.isNotBlank() } ?: a.attr("href")
                WebSearchEpisodeInfo(
                    channel = channel,
                    name = text,
                    episodeSort = matchEpisodeSortFromNameRegex.find(text)?.groups?.get("ep")?.value
                        ?.let { EpisodeSort(it) }
                        ?: EpisodeSort(text),
                    playUrl = SelectorHelpers.computeAbsoluteUrl(baseUrl, href),
                )
            }
        }

        return SelectedChannelEpisodes(
            channels,
            page.select(selectLists)
                .flatMapIndexed { i, e ->
                    val channel = channels.getOrNull(i)
                    parseEps(e, channel)
                },
        )
    }
}

/**
 * 没有线路, 或者相当于只有一个线路. 只有一个 list of episodes
 */
data object SelectorChannelFormatNoChannel :
    SelectorChannelFormat<SelectorChannelFormatNoChannel.Config>(SelectorFormatId("no-channel")) {
    @Immutable
    @Serializable
    data class Config(
        @Language("css")
        val selectEpisodes: String = "#glist-1 > div.module-blocklist.scroll-box.scroll-box-y > div > a",
        @Language("regexp")
        val matchEpisodeSortFromName: String = DEFAULT_MATCH_EPISODE_SORT_FROM_NAME,
    ) : SelectorFormatConfig {
        val matchEpisodeSortFromNameRegex by lazy(LazyThreadSafetyMode.PUBLICATION) {
            Regex.parseOrNull(matchEpisodeSortFromName)
        }

        override fun isValid(): Boolean {
            return selectEpisodes.isNotBlank() && matchEpisodeSortFromName.isNotBlank()
        }
    }

    override fun select(
        page: Element,
        baseUrl: String,
        config: Config,
    ): SelectedChannelEpisodes? {
        val regex = config.matchEpisodeSortFromNameRegex ?: return null
        val selectEpisodes = QueryParser.parseSelectorOrNull(config.selectEpisodes) ?: return null
        return SelectedChannelEpisodes(
            null,
            page.select(selectEpisodes).map { a ->
                val text = a.text()
                val href = a.attr("href")
                WebSearchEpisodeInfo(
                    channel = null,
                    name = text,
                    episodeSort = regex.find(text)?.groups?.get("ep")?.value
                        ?.let { EpisodeSort(it) }
                        ?: EpisodeSort(text),
                    playUrl = SelectorHelpers.computeAbsoluteUrl(baseUrl, href),
                )
            },
        )
    }
}
