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

package me.him188.ani.datasources.dmhy.impl.titles

import me.him188.ani.datasources.api.topic.Episode
import me.him188.ani.datasources.api.topic.FrameRate
import me.him188.ani.datasources.api.topic.MediaOrigin
import me.him188.ani.datasources.api.topic.Resolution
import me.him188.ani.datasources.api.topic.SubtitleLanguage

/**
 * Machine Learning based
 */
internal class MLBasedRawTitleParser : RawTitleParser() {
    override fun parse(
        text: String,
        allianceName: String?,
        collectTag: (title: String) -> Unit,
        collectChineseTitle: (String) -> Unit,
        collectOtherTitle: (String) -> Unit,
        collectEpisode: (Episode) -> Unit,
        collectResolution: (Resolution) -> Unit,
        collectFrameRate: (FrameRate) -> Unit,
        collectMediaOrigin: (MediaOrigin) -> Unit,
        collectSubtitleLanguage: (SubtitleLanguage) -> Unit,
    ) {
        TODO("Not yet implemented")
    }
}