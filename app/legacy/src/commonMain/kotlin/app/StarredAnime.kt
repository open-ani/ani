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

package me.him188.ani.app.app

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import me.him188.ani.api.protocol.EStarredAnime
import me.him188.ani.shared.models.Alliance
import me.him188.ani.shared.models.Episode
import me.him188.ani.shared.models.Resolution
import me.him188.ani.shared.models.SubtitleLanguage

/**
 * 表示一个收藏的番剧信息.
 */
@Serializable
@Immutable
data class StarredAnime(
    @ProtoNumber(1) val primaryName: String,
    @ProtoNumber(2) val secondaryNames: List<String> = listOf(),
    /**
     * 搜索时的关键词.
     */
    @ProtoNumber(3) val searchQuery: String, // keywords
    @ProtoNumber(4) val episodes: Set<Episode>,
    @ProtoNumber(5) val watchedEpisodes: Set<Episode> = setOf(),
    @ProtoNumber(6) val preferredAlliance: Alliance? = null,
    @ProtoNumber(7) val preferredResolution: Resolution? = null,
    @ProtoNumber(8) val preferredSubtitleLanguage: SubtitleLanguage? = null,
    @ProtoNumber(9) val starTimeMillis: Long,
) {
    val id
        @Stable
        get() = searchQuery
}

fun EStarredAnime.toStarredAnime(): StarredAnime {
    return StarredAnime(
        primaryName = primaryName,
        secondaryNames = secondaryNames,
        searchQuery = searchQuery,
        episodes = episodes,
        watchedEpisodes = watchedEpisodes,
        preferredAlliance = preferredAlliance,
        preferredResolution = preferredResolution,
        preferredSubtitleLanguage = preferredSubtitleLanguage,
        starTimeMillis = starTimeMillis,
    )
}

fun StarredAnime.toEStarredAnime(): EStarredAnime {
    return EStarredAnime(
        primaryName = primaryName,
        secondaryNames = secondaryNames,
        searchQuery = searchQuery,
        episodes = episodes,
        watchedEpisodes = watchedEpisodes,
        preferredAlliance = preferredAlliance,
        preferredResolution = preferredResolution,
        preferredSubtitleLanguage = preferredSubtitleLanguage,
        starTimeMillis = starTimeMillis,
    )
}
