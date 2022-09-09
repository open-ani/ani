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

package me.him188.animationgarden.app.app.data

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import me.him188.animationgarden.api.impl.model.ListFlow
import me.him188.animationgarden.api.impl.model.ListFlowImpl
import me.him188.animationgarden.api.impl.model.MutableListFlow
import me.him188.animationgarden.api.impl.model.mutate
import me.him188.animationgarden.api.model.Alliance
import me.him188.animationgarden.api.model.Commit
import me.him188.animationgarden.api.model.StarredAnimeCommits
import me.him188.animationgarden.api.model.UnsupportedCommit
import me.him188.animationgarden.api.protocol.EAppData
import me.him188.animationgarden.api.tags.Episode
import me.him188.animationgarden.api.tags.Resolution
import me.him188.animationgarden.api.tags.SubtitleLanguage
import me.him188.animationgarden.app.app.RefreshState
import me.him188.animationgarden.app.app.StarredAnime
import me.him188.animationgarden.app.app.toEStarredAnime
import me.him188.animationgarden.app.app.toStarredAnime
import me.him188.animationgarden.app.ui.OrganizedViewState
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

inline fun <reified T, R> context(c: T, block: context(T) () -> R): R {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return block(c)
}

@Stable
interface AppData {
    val starredAnime: ListFlow<StarredAnime>

    companion object {
        @Stable
        val initial: AppData = object : AppData {
            override val starredAnime: ListFlow<StarredAnime> = ListFlowImpl(listOf())
        }
    }
}

fun AppData.toEAppData(): EAppData {
    return EAppData(starredAnime = starredAnime.value.map { it.toEStarredAnime() })
}

fun Commit.toMutation(): DataMutation = when (this) {
    is StarredAnimeCommits.Add -> StarredAnimeMutations.Add(anime.toStarredAnime())
    is StarredAnimeCommits.EpisodeWatched -> StarredAnimeMutations.EpisodeWatched(id, episode)
    is StarredAnimeCommits.Remove -> StarredAnimeMutations.Remove(id)
    is UnsupportedCommit -> UnsupportedDataMutation(kind)
    is StarredAnimeCommits.UpdateRefreshed -> StarredAnimeMutations.UpdateRefreshed(
        id = id,
        primaryName = primaryName,
        secondaryNames = secondaryNames,
        searchQuery = searchQuery,
        episodes = episodes,
        preferredAlliance = preferredAlliance,
        preferredResolution = preferredResolution,
        preferredSubtitleLanguage = preferredSubtitleLanguage
    )
    is StarredAnimeCommits.AddEpisodes -> StarredAnimeMutations.AddEpisodes(id, episode)
}

fun EAppData.toAppData(): AppData {
    TODO()
}

@Stable
val LocalData: ProvidableCompositionLocal<AppData> = compositionLocalOf { error("AppData is not provided") }

@Stable
interface DataMutationContext : AppData {
    @Stable
    override val starredAnime: MutableListFlow<StarredAnime>
}

@Stable
val LocalDataMutation: ProvidableCompositionLocal<DataMutationContext> =
    compositionLocalOf { error("DataMutationContext is not provided") }

sealed interface DataMutation {
    context(DataMutationContext) suspend fun invoke()

    context(DataMutationContext) suspend fun revoke()
}

sealed interface SingleDataMutation : DataMutation {
    /**
     * @return `null` means no commit needed for this mutation.
     */
    fun toCommit(): Commit?
}

infix fun DataMutation.then(mutation: DataMutation): DataMutation = CombinedDataMutation(this, mutation)

/**
 * Multiple [DataMutation]s invoked in one commit.
 */
class CombinedDataMutation(
    val first: DataMutation,
    val then: DataMutation
) : DataMutation {
    context(DataMutationContext) override suspend fun invoke() {
        first.invoke()
        then.invoke()
    }

    context(DataMutationContext) override suspend fun revoke() {
        first.revoke()
        then.revoke()
    }
}

object StarredAnimeMutations {
    class Add(private val anime: StarredAnime) : SingleDataMutation {
        constructor(searchQuery: String, organized: OrganizedViewState) : this(
            StarredAnime(
                primaryName = organized.chineseName.value
                    ?: organized.otherNames.value.firstOrNull() ?: "",
                secondaryNames = organized.otherNames.value,
                episodes = organized.episodes.value,
                preferredAlliance = organized.selectedAlliance.value,
                preferredResolution = organized.selectedResolution.value,
                preferredSubtitleLanguage = organized.selectedSubtitleLanguage.value,
                searchQuery = searchQuery,
                starTimeMillis = System.currentTimeMillis()
            )
        )


        override fun toCommit(): Commit = StarredAnimeCommits.Add(anime.toEStarredAnime())

        context(DataMutationContext) override suspend fun invoke() {
            starredAnime.add(anime)
        }

        context(DataMutationContext) override suspend fun revoke() {
            starredAnime.remove(anime)
        }
    }

    class Remove(private val id: String) : SingleDataMutation {
        override fun toCommit(): Commit {
            return StarredAnimeCommits.Remove(id)
        }

        private var removed: StarredAnime? = null

        context(DataMutationContext) override suspend fun invoke() {
            removed = starredAnime.removeById(id)
        }

        context(DataMutationContext) override suspend fun revoke() {
            removed?.let { starredAnime.add(it) }
        }
    }

    class UpdateRefreshed(
        private val id: String,

        private val primaryName: String,
        private val secondaryNames: List<String> = listOf(),
        private val searchQuery: String, // keywords
        private val episodes: Set<Episode>,
        private val preferredAlliance: Alliance? = null,
        private val preferredResolution: @Polymorphic Resolution? = null,
        private val preferredSubtitleLanguage: @Polymorphic SubtitleLanguage? = null,
    ) : SingleDataMutation {
        constructor(id: String, searchQuery: String, organized: OrganizedViewState) : this(
            id = id,
            primaryName = organized.chineseName.value
                ?: organized.otherNames.value.firstOrNull() ?: "",
            secondaryNames = organized.otherNames.value,
            episodes = organized.episodes.value,
            preferredAlliance = organized.selectedAlliance.value,
            preferredResolution = organized.selectedResolution.value,
            preferredSubtitleLanguage = organized.selectedSubtitleLanguage.value,
            searchQuery = searchQuery,
        )

        override fun toCommit(): Commit {
            return StarredAnimeCommits.UpdateRefreshed(
                id = id,
                primaryName = primaryName,
                secondaryNames = secondaryNames,
                searchQuery = searchQuery,
                episodes = episodes,
                preferredAlliance = preferredAlliance,
                preferredResolution = preferredResolution,
                preferredSubtitleLanguage = preferredSubtitleLanguage
            )
        }

        private lateinit var original: StarredAnime

        context(DataMutationContext) override suspend fun invoke() {
            starredAnime.update(id) {
                original = this
                copy(
                    primaryName = this@UpdateRefreshed.primaryName,
                    secondaryNames = this@UpdateRefreshed.secondaryNames,
                    episodes = this@UpdateRefreshed.episodes,
                    preferredAlliance = this@UpdateRefreshed.preferredAlliance,
                    preferredResolution = this@UpdateRefreshed.preferredResolution,
                    preferredSubtitleLanguage = this@UpdateRefreshed.preferredSubtitleLanguage,
                    searchQuery = this@UpdateRefreshed.searchQuery,
                )
            }
        }

        context(DataMutationContext) override suspend fun revoke() {
            starredAnime.update(id) { this@UpdateRefreshed.original }
        }
    }

    class EpisodeWatched(
        private val id: String,
        private val episode: Episode,
    ) : SingleDataMutation {
        override fun toCommit(): Commit = StarredAnimeCommits.EpisodeWatched(id, episode)

        private lateinit var original: Set<Episode>

        context(DataMutationContext) override suspend fun invoke() {
            starredAnime.update(id) {
                original = watchedEpisodes
                copy(
                    watchedEpisodes = watchedEpisodes + episode
                )
            }
        }

        context(DataMutationContext) override suspend fun revoke() {
            starredAnime.update(id) {
                copy(
                    watchedEpisodes = watchedEpisodes
                )
            }
        }
    }

    class AddEpisodes(
        private val id: String,
        private val extraEpisodes: Set<Episode>,
    ) : SingleDataMutation {
        override fun toCommit(): Commit = StarredAnimeCommits.AddEpisodes(id, extraEpisodes)

        private lateinit var original: Set<Episode>

        context(DataMutationContext) override suspend fun invoke() {
            starredAnime.update(id) {
                original = episodes
                copy(
                    episodes = episodes + extraEpisodes
                )
            }
        }

        context(DataMutationContext) override suspend fun revoke() {
            starredAnime.update(id) {
                copy(
                    episodes = original
                )
            }
        }
    }

    class ChangeRefreshState(
        private val id: String,
        private val newState: RefreshState
    ) : SingleDataMutation {
        override fun toCommit(): Commit? = null

        context(DataMutationContext) override suspend fun invoke() {
            starredAnime.update(id) {
                copy(refreshState = newState)
            }
        }

        context(DataMutationContext) override suspend fun revoke(): Nothing {
            throw UnsupportedOperationException()
        }
    }

    private fun MutableListFlow<StarredAnime>.add(
        anime: StarredAnime
    ) {
        return mutate { list ->
            (list + anime).distinctBy { it.id }
        }
    }

    private fun MutableListFlow<StarredAnime>.remove(
        anime: StarredAnime
    ) {
        return mutate { list ->
            list - anime
        }
    }

    private fun MutableListFlow<StarredAnime>.removeById(
        id: String
    ): StarredAnime? {
        return removeFirst { it.id == id }
    }

    private inline fun <T : Any> MutableListFlow<T>.removeFirst(
        predicate: (T) -> Boolean
    ): T? {
        var removed: T? = null
        mutate { list ->
            val result = ArrayList<T>(list.size)
            list.filterTo(result) {
                if (removed == null && predicate(it)) {
                    removed = it
                    false
                } else true
            }
        }
        return removed
    }

    private fun MutableListFlow<StarredAnime>.update(
        id: String,
        update: StarredAnime.() -> StarredAnime,
    ) {
        return mutate { list ->
            list.map { anime ->
                if (anime.id == id) update(anime) else anime
            }
        }
    }
}

@Serializable
class UnsupportedDataMutation(
    private val kind: String?
) : SingleDataMutation {
    override fun toCommit(): Commit {
        return UnsupportedCommit(kind)
    }

    context(DataMutationContext) override suspend fun invoke() {
        // nop
    }

    context(DataMutationContext) override suspend fun revoke() {
        // nop
    }
}
