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

package me.him188.ani.datasources.api.source

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.paging.SizedSource
import me.him188.ani.datasources.api.topic.ResourceLocation
import kotlin.contracts.contract

/**
 * A place where [MediaMatch] can be fetched using [MediaFetchRequest].
 * For example, a website, or a local directory.
 *
 * A [MediaSource] can be either online or local. See [location] for more details.
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
        get() = MediaSourceLocation.Online

    val kind: MediaSourceKind

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
    suspend fun fetch(query: MediaFetchRequest): SizedSource<MediaMatch>
}

@Serializable
enum class MediaSourceKind {
    /**
     * 在线视频网站. 资源为 [ResourceLocation.WebVideo] 或 [ResourceLocation.HttpStreamingFile]
     */
    WEB,

    /**
     * P2P BitTorrent 网络. 资源为 [ResourceLocation.HttpTorrentFile] 或 [ResourceLocation.MagnetLink]
     */
    BitTorrent,

    /**
     * 本地视频缓存. 只表示那些通过 `MediaCacheManager` 缓存的视频.
     */
    LocalCache
}

/**
 * A media matched from the source.
 */
data class MediaMatch(
    val media: Media,
    val kind: MatchKind,
)

enum class MatchKind {
    /**
     * The request has an exact match with the cache.
     * Usually because episode id is the same.
     */
    EXACT,

    /**
     * The request does not have a [EXACT] match but a [FUZZY] one.
     *
     * This is done on a best-effort basis where they can be false positives.
     */
    FUZZY,

    /**
     * The request does not match the cache.
     *
     * This is done on a best-effort basis where they can be false negatives.
     */
    NONE
}

@Serializable
class MediaFetchRequest(
    /**
     * Platform-specific index id to help improve accuracy,
     * especially when fetching from cache storages where the original [MediaFetchRequest] was saved when caching the media.
     *
     * This is designed to support multiple index providers like Bangumi.
     */
    val subjectId: String? = null,
    val episodeId: String? = null,
    val subjectNameCN: String? = null,
    /**
     * Translated and original names of the subject.
     *
     * E.g. "关于我转生变成史莱姆这档事 第三季"
     */
    val subjectNames: Set<String>,
    /**
     * E.g. "49", "01"
     */
    val episodeSort: EpisodeSort,
    /**
     * E.g. "恶魔与阴谋"
     */
    val episodeName: String,
    val episodeEp: EpisodeSort? = episodeSort,
)


/**
 * Location of a [MediaSource].
 */
@Serializable(MediaSourceLocation.AsStringSerializer::class)
sealed class MediaSourceLocation {
    /**
     * The media source is very far a way from us, e.g. on the Internet or on another planet.
     */
    data object Online : MediaSourceLocation()

    /**
     * The media source is fairly close to us, e.g. on a file server on the LAN.
     */
    data object Lan : MediaSourceLocation()

    /**
     * The media source is on our local file system.
     */
    data object Local : MediaSourceLocation()

    companion object {
        val entries = listOf(Online, Lan, Local)
    }

    internal object AsStringSerializer : KSerializer<MediaSourceLocation> {
        override val descriptor = String.serializer().descriptor

        override fun serialize(encoder: Encoder, value: MediaSourceLocation) {
            encoder.encodeString(
                getText(value)
            )
        }

        private fun getText(value: MediaSourceLocation) = when (value) {
            Online -> "ONLINE"
            Lan -> "LAN"
            Local -> "LOCAL"
        }

        override fun deserialize(decoder: Decoder): MediaSourceLocation {
            val string = decoder.decodeString()
            for (entry in entries) { // type safe
                if (getText(entry) == string) {
                    return entry
                }
            }
            throw SerializationException("Unknown MediaSourceLocation: $string")
        }
    }
}

fun MediaSourceLocation.isLowEffort(): Boolean {
    contract {
        returns(false) implies (this@isLowEffort is MediaSourceLocation.Online)
        returns(true) implies (this@isLowEffort is MediaSourceLocation.Lan || this@isLowEffort is MediaSourceLocation.Local)
    }
    return this is MediaSourceLocation.Lan || this is MediaSourceLocation.Local
}

enum class ConnectionStatus {
    SUCCESS,
    FAILED,
}

fun Boolean.toConnectionStatus() = if (this) ConnectionStatus.SUCCESS else ConnectionStatus.FAILED

interface SearchOrdering {
    val id: String
    val name: String
}
