/*
 * Animation Garden App
 * Copyright (C) 2022  Him188
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

package me.him188.animationgarden.api.protocol

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import me.him188.animationgarden.api.model.Alliance
import me.him188.animationgarden.api.tags.Episode
import me.him188.animationgarden.api.tags.Resolution
import me.him188.animationgarden.api.tags.SubtitleLanguage

@Serializable
data class EStarredAnime(
    @ProtoNumber(1) val primaryName: String,
    @ProtoNumber(2) val secondaryNames: List<String> = listOf(),
    @ProtoNumber(3) val searchQuery: String, // keywords
    @ProtoNumber(4) val episodes: Set<Episode>,
    @ProtoNumber(5) val watchedEpisodes: Set<Episode> = setOf(),
    @ProtoNumber(6) val preferredAlliance: Alliance? = null,
    @ProtoNumber(7) val preferredResolution: @Polymorphic Resolution? = null,
    @ProtoNumber(8) val preferredSubtitleLanguage: @Polymorphic SubtitleLanguage? = null,
    @ProtoNumber(9) val starTimeMillis: Long,
) {
    inline val id get() = searchQuery
}