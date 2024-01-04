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
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import me.him188.animationgarden.datasources.api.DownloadSearchQuery
import me.him188.animationgarden.datasources.api.topic.Episode
import me.him188.animationgarden.datasources.api.topic.FrameRate
import me.him188.animationgarden.datasources.api.topic.MediaOrigin
import me.him188.animationgarden.datasources.api.topic.Resolution
import me.him188.animationgarden.datasources.api.topic.SubtitleLanguage
import me.him188.animationgarden.datasources.api.topic.TopicCategory
import me.him188.animationgarden.datasources.dmhy.DmhyClient
import java.io.File

private val json = Json {
    encodeDefaults = true
}


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

suspend fun main() {
    val client = DmhyClient.Factory.create {
    }

    val query = "葬送的芙莉莲"
    val session = client.startSearchSession(
        DownloadSearchQuery(
            keywords = query,
            category = TopicCategory.ANIME,
        )
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

class GenerateDataset {

    private sealed interface Kind {
        data object Zero : Kind {
            override fun toString(): String = "0"
        }

        data class Begin(val name: String) : Kind {
            override fun toString(): String = "B-$name"
        }

        data class Inside(val name: String) : Kind {
            override fun toString(): String = "I-$name"
        }
    }

    private sealed interface State {
        data class MergingWord(
            val name: String,
        ) : State

        data object MergingZero : State
        data object SkippingSpaceInZeroMode : State
        data class SpaceSeparated(
            val name: String,
        ) : State

        data object None : State
    }

    fun generate(one: One): String {
        val input = one.input
        val labels = mapOf(
            "TITLE" to one.output.titles.map {
                val index = input.indexOf(it)
                index..<(index + it.length)
            },
            "EPISODE" to one.output.episode?.let {
                val index = input.indexOf(it.toString())
                listOf(index..<(index + it.toString().length))
            },
        )

        data class Classification(
            val char: Char,
            val kind: Kind,
        )

        val charClassifications = buildList {
            var inWord = false
            input.forEachIndexed { index, c ->
                for ((name, ranges) in labels) {
                    if (ranges == null) continue
                    if (ranges.any { it.contains(index) }) {
                        if (inWord) {
                            add(Classification(c, Kind.Inside(name)))
                        } else {
                            inWord = true
                            add(Classification(c, Kind.Begin(name)))
                        }
                        return@forEachIndexed
                    }
                }
                inWord = false
                add(Classification(c, Kind.Zero))
            }
        }

        for (charClassification in charClassifications) {
            println(charClassification)
        }

        val collapsed = buildString {
            val buffer = StringBuilder()
            var lastKind: Kind = Kind.Zero
            fun flush() {
                if (buffer.isEmpty()) return
                append(buffer.toString()).append(' ').appendLine(lastKind)
                buffer.clear()
            }

            for (classification in charClassifications) {
                when (classification.kind) {
                    is Kind.Begin -> {
                        flush()
                        lastKind = classification.kind // begin
                        buffer.append(classification.char)
                    }

                    is Kind.Inside -> {
                        if (classification.char == ' ') {
                            flush()
                            lastKind = Kind.Inside(classification.kind.name)
                        } else {
                            buffer.append(classification.char)
                        }
                    }

                    Kind.Zero -> {
                        if (lastKind !is Kind.Zero) {
                            flush()
                            lastKind = Kind.Zero
                        }
                        if (classification.char == ' ') {
                            flush()
                            lastKind = Kind.Zero
                        } else {
                            buffer.append(classification.char)
                        }
                    }
                }
            }
            flush()
        }

        // merge B-kind followed by I-kinds as a single B-kind, separated by spaces.

//        val collapsed = buildString {
//            var state: State = State.None
//            for (classification in charClassifications) {
//                when (state) {
//                    is State.MergingWord -> {
//                        if (classification.char == ' ') {
//                            appendLine(" B-${state.name}")
//                            state = State.SpaceSeparated(state.name)
//                            continue
//                        }
//                        when (classification.kind) {
//                            is Kind.Inside -> {
//                                append(classification.char)
//                            }
//
//                            is Kind.Begin -> {
//                                appendLine(" B-${state.name}")
//                                append(classification.char)
//                                state = State.MergingWord(classification.kind.name)
//                            }
//
//                            Kind.Zero -> {
//                                appendLine(" B-${state.name}")
//                                append(classification.char)
//                                state = State.None
//                            }
//                        }
//                    }
//
//                    State.None -> {
//                        when (classification.kind) {
//                            is Kind.Begin -> {
//                                append(classification.char)
//                                state = State.MergingWord(classification.kind.name)
//                            }
//
//                            is Kind.Inside -> {
//                                error("Should not see Inside before Begin")
//                            }
//
//                            Kind.Zero -> {
//                                append(classification.char)
//                                state = State.MergingZero
//                            }
//                        }
//                    }
//
//                    is State.SpaceSeparated -> {
//                        if (classification.char == ' ') {
//                            appendLine(" I-${state.name}")
//                            state = State.SpaceSeparated(state.name)
//                            continue
//                        }
//                        when (classification.kind) {
//                            is Kind.Inside -> {
//                                append(classification.char)
//                            }
//
//                            is Kind.Begin -> {
//                                appendLine(" I-${state.name}")
//                                append(classification.char)
//                                state = State.MergingWord(classification.kind.name)
//                            }
//
//                            Kind.Zero -> {
//                                appendLine(" I-${state.name}")
//                                append(classification.char)
//                                state = State.None
//                            }
//                        }
//                    }
//
//                    State.SkippingSpaceInZeroMode -> {
//                        if (classification.char == ' ') {
//                            continue
//                        }
//                        when (classification.kind) {
//                            is Kind.Inside -> error("Should not see Inside before Begin")
//                            Kind.Zero -> {
//                                append(classification.char)
//                                state = State.MergingZero
//                            }
//
//                            is Kind.Begin -> {
//                                append(classification.char)
//                                state = State.MergingWord(classification.kind.name)
//                            }
//                        }
//                    }
//
//                    State.MergingZero -> {
//                        if (classification.char == ' ') {
//                            appendLine(" 0")
//                            state = State.SkippingSpaceInZeroMode
//                            continue
//                        }
//                        when (classification.kind) {
//                            is Kind.Inside -> error("Should not see Inside before Begin")
//                            Kind.Zero -> {
//                                append(classification.char)
//                            }
//
//                            is Kind.Begin -> {
//                                appendLine(" 0")
//                                append(classification.char)
//                                state = State.MergingWord(classification.kind.name)
//                            }
//                        }
//                    }
//                }
//            }
//
//            when (state) {
//                is State.MergingWord -> appendLine(" B-${state.name}")
//                State.MergingZero -> appendLine(" 0")
//                State.None -> {}
//                is State.SpaceSeparated -> appendLine(" I-${state.name}")
//                State.SkippingSpaceInZeroMode -> {}
//            }
//        }
        return collapsed
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {

//            println(
//                GenerateDataset().generate(
//                    One(
//                        input = "【喵萌奶茶屋】★4月新番★[葬送的芙莉莲/Sousou no Frieren][01][GB][1080P][AVC_AAC][MP4].mp4",
//                        output = Output(
//                            titles = listOf("葬送的芙莉莲", "Sousou no Frieren"),
//                            episode = Episode("01"),
//                            resolution = Resolution.R1080P,
//                            subtitleLanguages = listOf(SubtitleLanguage.ChineseSimplified),
//                        )
//                    )
//                )
//            )

            File("output").walk().toList().reversed().forEach { file ->
                if (file.name.endsWith(".json")) {
                    val out = buildString {
                        file.inputStream().use { stream ->
                            Json.decodeFromStream(ListSerializer(One.serializer()), stream).forEach { one ->
                                appendLine(GenerateDataset().generate(one).trim())
                                appendLine()
                            }
                        }
                    }

                    file.resolveSibling("${file.nameWithoutExtension}.iob").writeText(out)
                }
            }
        }
    }
}