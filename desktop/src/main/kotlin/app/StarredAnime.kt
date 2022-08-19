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

package me.him188.animationgarden.desktop.app

import androidx.compose.runtime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import me.him188.animationgarden.api.impl.model.KeyedMutableListFlowImpl
import me.him188.animationgarden.api.impl.model.MutableListFlow
import me.him188.animationgarden.api.model.Alliance
import me.him188.animationgarden.api.tags.Episode
import me.him188.animationgarden.api.tags.Resolution
import me.him188.animationgarden.api.tags.SubtitleLanguage
import net.mamoe.yamlkt.Yaml
import java.io.File

@Serializable
data class StarredAnime(
    val primaryName: String,
    val secondaryNames: List<String> = listOf(),
    val searchQuery: String, // keywords
    val episodes: List<Episode>,
    val watchedEpisodes: Set<Episode> = setOf(),
    val preferredAlliance: Alliance? = null,
    val preferredResolution: @Polymorphic Resolution? = null,
    val preferredSubtitleLanguage: @Polymorphic SubtitleLanguage? = null,
    val starTimeMillis: Long,
) {
    val id get() = searchQuery
}


@Stable
class AppData private constructor() {
    @Stable
    val starredAnime: MutableListFlow<StarredAnime> = KeyedMutableListFlowImpl { it.primaryName }

    @Serializable
    private class SerialData(
        val starredAnime: List<StarredAnime> = listOf(),
    )

    companion object {
        fun load(string: String): AppData {
            val decoded = Yaml.decodeFromString(SerialData.serializer(), string)
            return AppData().apply {
                starredAnime.value = decoded.starredAnime
            }
        }

        fun dump(data: AppData): String {
            return Yaml.encodeToString(
                SerialData.serializer(),
                data.run {
                    SerialData(starredAnime.value)
                }
            )
        }
    }
}

class AppDataSaver(
    private val file: File
) {
    lateinit var data: AppData

    fun reload() {
        data = if (!file.exists()) {
            AppData.load("")
        } else {
            AppData.load(file.readText())
        }
    }

    fun save() {
        if (::data.isInitialized) {
            file.writeText(AppData.dump(data))
        }
    }


    @Composable
    fun attachAutoSave() {
        val data by rememberUpdatedState(data)
        val starredAnime by data.starredAnime.asFlow().collectAsState()
        LaunchedEffect(starredAnime) {
            withContext(Dispatchers.IO) {
                save()
            }
        }
    }
}