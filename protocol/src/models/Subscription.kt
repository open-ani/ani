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

package me.him188.animationgarden.shared.models

import me.him188.animationgarden.datasources.api.topic.Alliance
import me.him188.animationgarden.datasources.api.topic.Episode
import me.him188.animationgarden.datasources.api.topic.Resolution
import me.him188.animationgarden.datasources.api.topic.SubtitleLanguage

data class Subscription(
    val primaryName: String,
    val secondaryNames: List<String> = listOf(),
    /**
     * 搜索时的关键词.
     */
    val searchQuery: String, // keywords
    val episodes: Set<Episode>,
    val watchedEpisodes: Set<Episode> = setOf(),
    val preferredAlliance: Alliance? = null,
    val preferredResolution: Resolution? = null,
    val preferredSubtitleLanguage: SubtitleLanguage? = null,
    val starTimeMillis: Long,
)

@JvmInline
value class MagnetLink(
    val value: String,
)