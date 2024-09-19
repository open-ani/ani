/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.settings.mediasource.selector.test

import androidx.compose.runtime.Immutable
import me.him188.ani.app.data.models.ApiFailure
import me.him188.ani.app.data.source.media.source.web.SelectorSearchConfig
import me.him188.ani.app.data.source.media.source.web.SelectorSearchQuery
import me.him188.ani.app.data.source.media.source.web.WebSearchEpisodeInfo
import me.him188.ani.app.ui.settings.mediasource.RefreshResult
import me.him188.ani.app.ui.settings.mediasource.rss.test.MatchTag
import me.him188.ani.app.ui.settings.mediasource.rss.test.buildMatchTags
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.utils.xml.Element

@Immutable
sealed class SelectorTestEpisodeListResult : RefreshResult {
    @Immutable
    data class Success(
        val channels: List<String>?,
        val episodes: List<SelectorTestEpisodePresentation>,
    ) : SelectorTestEpisodeListResult(), RefreshResult.Success

    @Immutable
    data class ApiError(
        override val reason: ApiFailure
    ) : SelectorTestEpisodeListResult(), RefreshResult.ApiError

    @Immutable
    data object InvalidConfig : SelectorTestEpisodeListResult(), RefreshResult.InvalidConfig

    @Immutable
    data class UnknownError(
        override val exception: Throwable
    ) : SelectorTestEpisodeListResult(), RefreshResult.UnknownError
}

@Immutable
class SelectorTestEpisodePresentation(
    val name: String,
    val episodeSort: EpisodeSort?,
    val playUrl: String,
    val tags: List<MatchTag>,
    val origin: Element?,
) {
    companion object {
        fun compute(
            info: WebSearchEpisodeInfo,
            searchQuery: SelectorSearchQuery,
            origin: Element?,
            config: SelectorSearchConfig,
        ): SelectorTestEpisodePresentation {
            return SelectorTestEpisodePresentation(
                name = info.name,
                episodeSort = info.episodeSort,
                playUrl = info.playUrl,
                tags = buildMatchTags {
                    if (config.filterByEpisodeSort) {
                        if (info.episodeSort == null) {
                            emit("缺失 EP", isMissing = true)
                        } else {
                            emit("EP: ${info.episodeSort}", isMatch = info.episodeSort == searchQuery.episodeSort)
                        }
                    }

                    when {
                        info.playUrl.isEmpty() -> {
                            emit("缺失播放地址", isMissing = true)
                        }

                        !info.playUrl.startsWith("http") -> {
                            emit("播放地址: ${info.playUrl}", isMatch = false)
                        }

                        else -> {
                            emit(info.playUrl, isMatch = true)
                        }
                    }
                },
                origin = origin,
            )
        }
    }
}
