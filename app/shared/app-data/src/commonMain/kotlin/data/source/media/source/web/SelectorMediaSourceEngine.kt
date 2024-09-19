/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.data.source.media.source.web

import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import me.him188.ani.app.data.models.ApiResponse
import me.him188.ani.app.data.models.runApiRequest
import me.him188.ani.app.data.source.media.source.MediaListFilter
import me.him188.ani.app.data.source.media.source.MediaListFilterContext
import me.him188.ani.app.data.source.media.source.MediaListFilters
import me.him188.ani.app.data.source.media.source.MediaSourceEngineHelpers
import me.him188.ani.app.data.source.media.source.asCandidate
import me.him188.ani.app.data.source.media.source.web.format.SelectedChannelEpisodes
import me.him188.ani.app.data.source.media.source.web.format.SelectorChannelFormat
import me.him188.ani.app.data.source.media.source.web.format.SelectorChannelFormat.Companion.isPossiblyMovie
import me.him188.ani.app.data.source.media.source.web.format.SelectorFormatConfig
import me.him188.ani.app.data.source.media.source.web.format.SelectorSubjectFormat
import me.him188.ani.datasources.api.DefaultMedia
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.MediaProperties
import me.him188.ani.datasources.api.SubtitleKind
import me.him188.ani.datasources.api.matcher.WebVideo
import me.him188.ani.datasources.api.source.MediaSourceKind
import me.him188.ani.datasources.api.source.MediaSourceLocation
import me.him188.ani.datasources.api.topic.EpisodeRange
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.api.topic.ResourceLocation
import me.him188.ani.datasources.api.topic.SubtitleLanguage
import me.him188.ani.utils.ktor.toSource
import me.him188.ani.utils.xml.Document
import me.him188.ani.utils.xml.Element
import me.him188.ani.utils.xml.Xml

data class SelectorSearchQuery(
    val subjectName: String,
    val episodeSort: EpisodeSort,
)

fun SelectorSearchQuery.toFilterContext() = MediaListFilterContext(
    subjectNames = setOf(subjectName),
    episodeSort = episodeSort,
)

/**
 * 解析流程:
 *
 * [SelectorMediaSourceEngine.searchSubjects]
 * -> [SelectorMediaSourceEngine.selectSubjects]
 * -> [SelectorMediaSourceEngine.searchEpisodes]
 * -> [SelectorMediaSourceEngine.selectEpisodes]
 */
abstract class SelectorMediaSourceEngine {
    companion object {
        const val CURRENT_VERSION: UInt = 1u
    }

    data class SearchSubjectResult(
        val url: Url,
        /**
         * `null` means 404
         */
        val document: Document?,
    )

    suspend fun searchSubjects(
        searchUrl: String,
        subjectName: String,
    ): ApiResponse<SearchSubjectResult> {
        val encodedUrl = MediaSourceEngineHelpers.encodeUrlSegment(subjectName)

        val finalUrl = Url(
            searchUrl.replace("{keyword}", encodedUrl),
        )

        return searchImpl(finalUrl)
    }

    protected abstract suspend fun searchImpl(
        finalUrl: Url,
    ): ApiResponse<SearchSubjectResult>

    /**
     * @return `null` if config is invalid
     */
    open fun selectSubjects(
        document: Element,
        config: SelectorSearchConfig,
    ): List<WebSearchSubjectInfo>? {
        val subjectFormat = SelectorSubjectFormat.findById(config.subjectFormatId)
            ?: throw UnsupportedOperationException("Unsupported subject format: ${config.subjectFormatId}")

        @Suppress("UNCHECKED_CAST")
        subjectFormat as SelectorSubjectFormat<SelectorFormatConfig>

        val formatConfig = config.getFormatConfig(subjectFormat)
        if (!formatConfig.isValid()) {
            return null
        }
        val originalList = subjectFormat.select(document, config.baseUrl, formatConfig)

        return originalList
    }

    suspend fun searchEpisodes(
        subjectDetailsPageUrl: String,
    ): ApiResponse<Document> = doHttpGet(subjectDetailsPageUrl)

    /**
     * @return `null` if config is invalid
     */
    fun selectEpisodes(
        subjectDetailsPage: Element,
        config: SelectorSearchConfig,
    ): SelectedChannelEpisodes? {
        val channelFormat = SelectorChannelFormat.findById(config.channelFormatId)
            ?: throw UnsupportedOperationException("Unsupported channel format: ${config.channelFormatId}")

        @Suppress("UNCHECKED_CAST")
        channelFormat as SelectorChannelFormat<SelectorFormatConfig>
        val formatConfig = config.getFormatConfig(channelFormat)
        if (!formatConfig.isValid()) {
            return null
        }
        return channelFormat.select(
            subjectDetailsPage,
            config.baseUrl,
            formatConfig,
        )
    }

    data class SelectMediaResult(
        val originalList: List<DefaultMedia>,
        val filteredList: List<DefaultMedia>,
    )

    fun selectMedia(
        episodes: Sequence<WebSearchEpisodeInfo>,
        config: SelectorSearchConfig,
        query: SelectorSearchQuery,
        mediaSourceId: String,
    ): SelectMediaResult {
        val originalMediaList = episodes.mapNotNull { info ->
            info.episodeSort ?: return@mapNotNull null
            DefaultMedia(
                mediaId = "$mediaSourceId.${info.name}-${info.episodeSort}",
                mediaSourceId = mediaSourceId,
                originalUrl = info.playUrl,
                download = ResourceLocation.WebVideo(info.playUrl),
                originalTitle = info.name,
                publishedTime = 0L,
                properties = MediaProperties(
                    subtitleLanguageIds = listOf(SubtitleLanguage.ChineseSimplified.id),
                    resolution = "1080P",
                    alliance = info.channel ?: mediaSourceId,
                    size = FileSize.Unspecified,
                    subtitleKind = SubtitleKind.EMBEDDED,
                ),
                episodeRange = EpisodeRange.single(
                    if (isPossiblyMovie(info.name) && info.episodeSort is EpisodeSort.Special) {
                        EpisodeSort(1) // 电影总是 01
                    } else {
                        info.episodeSort
                    },
                ),
                location = MediaSourceLocation.Online,
                kind = MediaSourceKind.WEB,
            )
        }.toList()

        return with(query.toFilterContext()) {
            val filters = config.createFiltersForEpisode()
            val filteredList = originalMediaList.filter {
                filters.applyOn(it.asCandidate())
            }
            SelectMediaResult(originalMediaList, filteredList)
        }
    }

    fun matchWebVideo(url: String, searchConfig: SelectorSearchConfig.MatchVideoConfig): WebVideo? {
        val result = searchConfig.matchVideoUrlRegex?.find(url) ?: return null
        val videoUrl = result.groups["v"]?.value ?: result.value
        return WebVideo(
            videoUrl,
            mapOf(
                "User-Agent" to searchConfig.addHeadersToVideo.userAgent,
                "Referer" to searchConfig.addHeadersToVideo.referer,
                "Sec-Ch-Ua-Mobile" to "?0",
                "Sec-Ch-Ua-Platform" to "macOS",
                "Sec-Fetch-Dest" to "video",
                "Sec-Fetch-Mode" to "no-cors",
                "Sec-Fetch-Site" to "cross-site",
            ),
        )
    }

    protected abstract suspend fun doHttpGet(uri: String): ApiResponse<Document>
}

// TODO: require MediaListFilterContext when context parameters
fun WebSearchSubjectInfo.asCandidate(): MediaListFilter.Candidate {
    val info = this
    return object : MediaListFilter.Candidate {
        override val originalTitle: String get() = info.name
        override val episodeRange: EpisodeRange? get() = null
    }
}

/**
 * If you change, you also need to change
 */
internal fun SelectorSearchConfig.createFiltersForSubject() = buildList {
    if (filterBySubjectName) add(MediaListFilters.ContainsSubjectName)
}

internal fun SelectorSearchConfig.createFiltersForEpisode() = buildList {
    addAll(createFiltersForSubject())
    if (filterByEpisodeSort) add(MediaListFilters.ContainsEpisodeSort)
}

class DefaultSelectorMediaSourceEngine(
    /**
     * Engine 自己不会 cache 实例, 每次都调用 `.first()`.
     */
    private val client: Flow<HttpClient>,
) : SelectorMediaSourceEngine() {
    override suspend fun searchImpl(
        finalUrl: Url,
    ): ApiResponse<SearchSubjectResult> = runApiRequest {
        val document = try {
            client.first().get(finalUrl).let { resp ->
                Xml.parse(resp.bodyAsChannel().toSource())
            }
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.NotFound) {
                // 404 Not Found
                return@runApiRequest SearchSubjectResult(
                    finalUrl,
                    document = null,
                )
            }
            throw e
        }


        SearchSubjectResult(
            finalUrl,
            document,
        )
    }


    public override suspend fun doHttpGet(uri: String): ApiResponse<Document> =
        runApiRequest {
            client.first().get(uri) {
            }.let { resp ->
                Xml.parse(resp.bodyAsChannel().toSource())
            }
        }
}
