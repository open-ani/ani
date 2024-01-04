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

package me.him188.animationgarden.app.ui.search

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.structuralEqualityPolicy
import me.him188.animationgarden.datasources.api.topic.Alliance
import me.him188.animationgarden.datasources.api.topic.Episode
import me.him188.animationgarden.datasources.api.topic.Resolution
import me.him188.animationgarden.datasources.api.topic.SubtitleLanguage
import me.him188.animationgarden.datasources.api.topic.Topic

/**
 * Represents a work, which may be Anime, Manga, Japanese Drama, etc.
 */
@Stable
class OrganizedViewState {
    @Stable
    val chineseName: MutableState<String?> = mutableStateOf(null)

    @Stable
    val otherNames: MutableState<List<String>> = mutableStateOf(listOf())

    @Stable
    val episodes: MutableState<Set<Episode>> = mutableStateOf(setOf())

    @Stable
    val subtitleLanguages: MutableState<List<SubtitleLanguage>> =
        mutableStateOf(listOf()) // null element means Other

    @Stable
    val resolutions: MutableState<List<Resolution>> = mutableStateOf(listOf())

    @Stable
    val alliances: MutableState<List<Alliance>> = mutableStateOf(listOf())


    @Stable
    val selectedEpisode: MutableState<Episode?> =
        mutableStateOf(null, structuralEqualityPolicy()) // inline class boxed

    @Stable
    val selectedSubtitleLanguage: MutableState<SubtitleLanguage?> =
        mutableStateOf(null, referentialEqualityPolicy())

    @Stable
    val selectedResolution: MutableState<Resolution?> =
        mutableStateOf(null, referentialEqualityPolicy())

    @Stable
    val selectedAlliance: MutableState<Alliance?> =
        mutableStateOf(null, referentialEqualityPolicy())

    @Stable
    val hasFilter: State<Boolean> = derivedStateOf {
        selectedAlliance.value != null
                || selectedSubtitleLanguage.value != null
                || selectedResolution.value != null
                || selectedAlliance.value != null
    }


    // TODO: 2022/8/7 size filters

    @Stable
    private val filter: MutableState<String?> = mutableStateOf(null)
    private fun checkFilterUpdated(filter: String?) {
        if (this.filter.value != filter) {
            this.filter.value = filter
            selectedEpisode.value = null
            selectedAlliance.value = null
            selectedResolution.value = null
            selectedSubtitleLanguage.value = null
        }
    }

    fun setTopics(topics: List<Topic>, filter: String?) {
        checkFilterUpdated(filter)

        // Sort first by length, then alphabetically.
        // Example result: 01, 02, 03, SP 01, SP 02, SP 03, 01+小剧场, 02+小剧场
        this.episodes.value = topics.asSequence()
            .mapNotNull { it.details?.episode }
            .distinctBy { it.raw }
            .sortedBy { it.raw.length }
            .groupBy { it.raw.length }
            .flatMap { (_, value) ->
                value.sortedBy { it.raw }
            }
            .toSet()

        // Sort descending by resolution size.
        // Example resulst: 4K, 2K, 1080P, 720P
        this.resolutions.value = topics.asSequence()
            .mapNotNull { it.details?.resolution }
            .distinctBy { it.id }
            .sortedByDescending { it.size }
            .toList()

        // Simply sort by name.
        this.alliances.value = topics.asSequence()
            .mapNotNull { it.alliance }
            .distinctBy { it.id }
            .sortedBy { it.name }
            .toList()

        // Simply sort by name.
        this.otherNames.value = topics.asSequence()
            .flatMap { it.details?.otherTitles.orEmpty() }
            .distinctBy { it.lowercase() }
            .sorted()
            .toList()

        // Sort by id, then add Other to the end
        this.subtitleLanguages.value =
            topics.asSequence()
                .flatMap { it.details?.subtitleLanguages.orEmpty() }
                .distinctBy { it.id }
                .sortedBy { it.id }
                .toList()

        updateNames(topics)

//        this.episodes.mutate { old ->
//            buildSet {
//                for (topic in topics) {
//                    topic.details?.episode?.let { add(it) }
//                }
//            }
//        }
//
//        this.subtitleLanguages.mutate { old ->
//            buildSet {
//                addAll(old)
//                for (topic in topics) {
//                    topic.details?.subtitleLanguages?.let { addAll(it) }
//                }
//            }
//        }
//        this.resolutions.mutate { old ->
//            buildSet {
//                addAll(old)
//                for (topic in topics) {
//                    topic.details?.resolution?.let { add(it) }
//                }
//            }
//        }
//        this.alliances.mutate { old ->
//            buildSet {
//                addAll(old)
//                for (topic in topics) {
//                    topic.alliance?.let { add(it) }
//                }
//            }
//        }
    }

    fun matchesQuery(topic: Topic): Boolean {
        return matchesQueryImpl(topic)
    }

    private fun matchesQueryImpl(topic: Topic): Boolean {
        this.selectedEpisode.value?.let { if (topic.details?.episode != it) return false }
        this.selectedResolution.value?.let { if (topic.details?.resolution != it) return false }
        this.selectedSubtitleLanguage.value?.let {
            val languages = topic.details?.subtitleLanguages
            if (it == SubtitleLanguage.Other) {
                if (!languages.isNullOrEmpty()) return false // languages are parsed
            } else if (languages?.contains(it) == false) {
                // language not included
                return false
            }
        } // allow null
        this.selectedAlliance.value?.let { if (topic.alliance != it) return false }
        return true
    }

    private fun updateNames(topics: List<Topic>) {
        // select the most frequent one
        val newChineseTitle = topics.mostFrequentByOrNull { it.details?.chineseTitle }
        this.chineseName.value = newChineseTitle
    }

    private fun <T, K> Iterable<T>.mostFrequentByOrNull(key: (T) -> K): K? =
        groupingBy(key)
            .eachCount()
            .asSequence()
            .sortedByDescending { it.value }
            .firstOrNull()
            ?.key
}

fun <T : Any> MutableState<T?>.invertSelected(value: T) {
    if (this.value == value) this.value = null
    else this.value = value
}
