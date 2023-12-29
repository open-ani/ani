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

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import me.him188.animationgarden.api.AnimationGardenClient
import me.him188.animationgarden.api.model.SearchQuery
import me.him188.animationgarden.shared.models.*
import java.io.File

private val json = Json {
    encodeDefaults = true
}

suspend fun main() {
    val client = AnimationGardenClient.Factory.create {
    }

    val query = "葬送的芙莉莲"
    val session = client.startSearchSession(
        SearchQuery(
            keywords = query,
            category = TopicCategory.ANIME,
        )
    )

    @Serializable
    data class Output(
//        val alliance: String?,
        var tags: List<String> = listOf(),
        var titles: List<String>,
        var episode: Episode? = null,
        var resolution: Resolution? = null,
        var mediaOrigin: MediaOrigin? = null,
        var frameRate: FrameRate? = null,
        var subtitleLanguages: List<SubtitleLanguage> = listOf(),
    )

    @Serializable
    data class One(
        val input: String,
        val output: Output,
    )

    val list = buildList {
        session.results.collect { topic ->
            val details = topic.details ?: return@collect
            add(One(topic.rawTitle, details.run {
                Output(
//                    alliance = topic.alliance?.name,
                    tags = tags.toList(),
                    titles = buildList {
                        chineseTitle?.trim()?.let { add(it) }
                        addAll(otherTitles.mapNotNull { title -> title.trim().takeIf { it.isNotEmpty() } })
                    },
                    episode = episode,
                    resolution = resolution,
                    frameRate = frameRate,
                    mediaOrigin = mediaOrigin,
                    subtitleLanguages = subtitleLanguages.toList()
                )
            }))
        }
    }

    for (one in list) {
        if (one.output.tags.contains("Sousou no Frieren")) {
            one.output.titles += "Sousou no Frieren"
            one.output.tags = one.output.tags.filter { it != "Sousou no Frieren" }
        }
    }

    File("output/$query-${list.size}.json").apply { createNewFile() }.outputStream().use {
        json.encodeToStream(list, it)
    }

    println(list)
}