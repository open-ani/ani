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

package me.him188.animationgarden.api.model

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import me.him188.animationgarden.api.protocol.EStarredAnime
import me.him188.animationgarden.api.tags.Episode
import me.him188.animationgarden.api.tags.Resolution
import me.him188.animationgarden.api.tags.SubtitleLanguage

@Serializable
sealed interface Commit

object StarredAnimeCommits {
    @Serializable
    data class Add(private val anime: EStarredAnime) : Commit

    @Serializable
    data class Remove(private val id: String) : Commit

    @Serializable
    data class Update(
        val id: String,

        val primaryName: String,
        val secondaryNames: List<String> = listOf(),
        val searchQuery: String, // keywords
        val episodes: Set<Episode>,
        val preferredAlliance: Alliance? = null,
        val preferredResolution: @Polymorphic Resolution? = null,
        val preferredSubtitleLanguage: @Polymorphic SubtitleLanguage? = null,
    ) : Commit

    @Serializable
    data class EpisodeWatched(
        val id: String,
        val episode: Episode,
    ) : Commit

    @Serializable
    data class UpdateEpisodes(
        val id: String,
        val episode: Set<Episode>,
    ) : Commit
}

@Serializable
data class UnsupportedCommit(
    private val kind: String?
) : Commit {
    companion object {
        @Serializable
        private object SerialData

        fun deserializer(kind: String?): DeserializationStrategy<UnsupportedCommit> =
            object : DeserializationStrategy<UnsupportedCommit> {
                override val descriptor: SerialDescriptor = SerialData.serializer().descriptor

                override fun deserialize(decoder: Decoder): UnsupportedCommit =
                    decoder.decodeStructure(descriptor) {
                        UnsupportedCommit(kind)
                    }
            }
    }
}

val CommitsModule = SerializersModule {
    polymorphic(Commit::class) {
        subclass(StarredAnimeCommits.Add::class, StarredAnimeCommits.Add.serializer())
        subclass(StarredAnimeCommits.Remove::class, StarredAnimeCommits.Remove.serializer())
        subclass(StarredAnimeCommits.Update::class, StarredAnimeCommits.Update.serializer())
        subclass(StarredAnimeCommits.EpisodeWatched::class, StarredAnimeCommits.EpisodeWatched.serializer())
        subclass(StarredAnimeCommits.UpdateEpisodes::class, StarredAnimeCommits.UpdateEpisodes.serializer())
        default { UnsupportedCommit.deserializer(it) }
    }
}
