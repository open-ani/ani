package me.him188.animationgarden.desktop.ui

import androidx.compose.runtime.*
import me.him188.animationgarden.api.model.Alliance
import me.him188.animationgarden.api.model.Topic
import me.him188.animationgarden.api.tags.Episode
import me.him188.animationgarden.api.tags.Resolution
import me.him188.animationgarden.api.tags.SubtitleLanguage

/**
 * Represents a work, which may be Anime, Manga, Japanese Drama, etc.
 */
@Stable
class WorkState {
    @Stable
    val chineseName: MutableState<String> = mutableStateOf("")

    @Stable
    val otherNames: MutableState<List<String>> = mutableStateOf(listOf())

    @Stable
    val episodes: MutableState<List<Episode>> = mutableStateOf(listOf())

    @Stable
    val subtitleLanguages: MutableState<List<SubtitleLanguage>> = mutableStateOf(listOf())

    @Stable
    val resolutions: MutableState<List<Resolution>> = mutableStateOf(listOf())

    @Stable
    val alliances: MutableState<List<Alliance>> = mutableStateOf(listOf())


    @Stable
    val selectedEpisode: MutableState<Episode?> = mutableStateOf(null, structuralEqualityPolicy()) // inline class boxed

    @Stable
    val selectedSubtitleLanguage: MutableState<SubtitleLanguage?> = mutableStateOf(null, referentialEqualityPolicy())

    @Stable
    val selectedResolution: MutableState<Resolution?> = mutableStateOf(null, referentialEqualityPolicy())

    @Stable
    val selectedAlliance: MutableState<Alliance?> = mutableStateOf(null, referentialEqualityPolicy())


    // TODO: 2022/8/7 size filters

    fun setTopics(topics: List<Topic>) {
        this.episodes.value = topics.asSequence().mapNotNull { it.details?.episode }.distinctBy { it.raw }.toList()
        this.resolutions.value = topics.asSequence().mapNotNull { it.details?.resolution }.distinctBy { it.id }.toList()
        this.alliances.value = topics.asSequence().mapNotNull { it.alliance }.distinctBy { it.id }.toList()
        this.otherNames.value = topics.asSequence().flatMap { it.details?.otherTitles.orEmpty() }.distinct().toList()
        this.subtitleLanguages.value =
            topics.asSequence().flatMap { it.details?.subtitleLanguages.orEmpty() }.distinctBy { it.id }.toList()
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

    @Composable
    fun matchesQuery(topic: Topic): Boolean {
        this.selectedEpisode.value?.let { if (topic.details?.episode != it) return false }
        this.selectedResolution.value?.let { if (topic.details?.resolution != it) return false }
        this.selectedSubtitleLanguage.value?.let { if (topic.details?.subtitleLanguages?.contains(it) == false) return false } // allow null
        this.selectedAlliance.value?.let { if (topic.alliance != it) return false }
        return true
    }

    private fun updateNames(topics: List<Topic>) {
        // select the most frequent one
        val newChineseTitle = topics.mostFrequentByOrNull { it.details?.chineseTitle }
        this.chineseName.value = newChineseTitle.orEmpty()
//        this.otherNames.addAll(buildSet {
//            for (topic in this.< Error property >) {
//                topic.details?.otherTitles?.let { addAll(it) }
//            }
//        })
    }

    private fun <T, K> Iterable<T>.mostFrequentByOrNull(key: (T) -> K): K? =
        groupingBy(key)
            .eachCount()
            .asSequence()
            .sortedByDescending { it.value }
            .firstOrNull()
            ?.key

    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    private inline fun <T, K, V> Iterable<T>.associateByNotNull(
        keySelector: (T) -> K?,
        valueTransform: (T) -> V?
    ): Map<K, V> {
        val capacity = mapCapacity(collectionSizeOrDefault(10)).coerceAtLeast(16)
        val destination = LinkedHashMap<K, V>(capacity)
        for (element in this) {
            keySelector(element)?.let { key ->
                valueTransform.invoke(element)?.let { value ->
                    destination[key] = value
                }
            }
        }
        return destination
    }
}

fun <T : Any> MutableState<T?>.updateSelected(value: T) {
    if (this.value == value) this.value = null
    else this.value = value
}
