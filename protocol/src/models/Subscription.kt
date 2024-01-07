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

package me.him188.ani.shared.models

import kotlinx.serialization.Serializable
import me.him188.ani.datasources.api.topic.Alliance
import me.him188.ani.datasources.api.topic.Episode
import me.him188.ani.datasources.api.topic.Resolution
import me.him188.ani.datasources.api.topic.SubtitleLanguage

/**
 * 订阅的条目.
 */
@Serializable
data class Subscription(
    val name: String, // official name
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