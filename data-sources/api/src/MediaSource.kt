/*
 * Ani
 * Copyright (C) 2022-2024 Him188
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.him188.ani.datasources.api

import kotlinx.serialization.Serializable
import me.him188.ani.datasources.api.fetcher.MediaFetchRequest
import me.him188.ani.datasources.api.fetcher.MediaFetcher
import me.him188.ani.datasources.api.paging.PagedSource
import me.him188.ani.datasources.api.paging.SizedSource
import me.him188.ani.datasources.api.paging.map
import me.him188.ani.datasources.api.paging.merge
import me.him188.ani.datasources.api.topic.Alliance
import me.him188.ani.datasources.api.topic.Resolution
import me.him188.ani.datasources.api.topic.Topic
import me.him188.ani.datasources.api.topic.TopicCategory

/**
 * 提供视频媒体的接口
 *
 * @see MediaFetcher
 */
interface MediaSource {
    /**
     * Unique ID.
     */
    val mediaSourceId: String

    /**
     * Location of this [MediaSource].
     *
     * See [MediaSourceLocation] for meanings of the values.
     *
     * This can be used determine the cost of using this media source.
     *
     * **Implementation Notes**:
     * This properly must return the same value everytime it is called,
     * otherwise it may break its downstream usages, i.e. the media fetcher or the caching system.
     */
    val location: MediaSourceLocation
        get() = MediaSourceLocation.ONLINE

    /**
     * Checks whether this source is reachable.
     */
    suspend fun checkConnection(): ConnectionStatus

    /**
     * Starts a fetch.
     *
     * A media source is encouraged to use all names from [MediaFetchRequest.subjectNames]
     * to give as many results as possible.
     */
    suspend fun fetch(query: MediaFetchRequest): SizedSource<Media>
}

/**
 * A [remote][MediaSourceLocation.ONLINE] [MediaSource] that uses [Topic] internally.
 */
abstract class TopicMediaSource : MediaSource {
    override val location: MediaSourceLocation get() = MediaSourceLocation.ONLINE

    private fun Topic.toOnlineMedia(): OnlineMedia {
        val details = details
        return OnlineMedia(
            mediaId = "$mediaSourceId.${topicId}",
            mediaSourceId = mediaSourceId,
            originalUrl = originalLink,
            download = downloadLink,
            originalTitle = rawTitle,
            size = size,
            publishedTime = publishedTimeMillis ?: 0,
            properties = MediaProperties(
                subtitleLanguages = details?.subtitleLanguages?.map { it.toString() } ?: emptyList(),
                resolution = details?.resolution?.toString() ?: Resolution.R1080P.toString(),
                alliance = alliance,
            ),
        )
    }

    // For backward compatibility
    protected abstract suspend fun startSearch(query: DownloadSearchQuery): PagedSource<Topic>

    final override suspend fun fetch(query: MediaFetchRequest): SizedSource<OnlineMedia> {
        return query.subjectNames
            .map { name ->
                startSearch(
                    DownloadSearchQuery(
                        keywords = name,
                        category = TopicCategory.ANIME,
                        episodeSort = query.episodeSort,
                        episodeName = query.episodeName,
                    )
                ).map { it.toOnlineMedia() }
            }.merge()
    }
}

/**
 * Location of a [MediaSource].
 */
@Serializable
enum class MediaSourceLocation {
    /**
     * The media source is very far a way from us, e.g. on the Internet or on other planet.
     */
    ONLINE,

    /**
     * The media source is very close to us, e.g. on the local storage, or on a file server on the LAN.
     */
    LOCAL,
}

enum class ConnectionStatus {
    SUCCESS,
    FAILED,
}

data class DownloadSearchQuery(
    val keywords: String? = null,
    val category: TopicCategory? = null,
    val alliance: Alliance? = null,
    val ordering: SearchOrdering? = null,
    val episodeSort: String? = null,
    val episodeName: String? = null,
)

interface SearchOrdering {
    val id: String
    val name: String
}
