package me.him188.ani.app.ui.subject.episode.mediaFetch

import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.util.fastDistinctBy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import me.him188.ani.app.data.media.Media

/**
 * Creates a [MediaSelectorState].
 *
 * [defaultPreferenceProvider] is a provider for the default [MediaPreference].
 * [MediaSelectorState] uses a [derivedStateOf] to get the default value so,
 * if [defaultPreferenceProvider] accesses states, they will be observed.
 *
 * Similar for [mediaListProvider].
 */
fun MediaSelectorState(
    mediaListProvider: () -> List<Media>,
    defaultPreferenceProvider: () -> MediaPreference,
): MediaSelectorState = MediaSelectorStateImpl(mediaListProvider, defaultPreferenceProvider)

@Stable
interface MediaSelectorState {
    /**
     * The list of media available for selection.
     */
    val mediaList: List<Media>

    /**
     * Preferences explicit set by the user in this session.
     *
     * Use [selectedAlliance], [selectedResolution], [selectedSubtitleLanguage]
     * for selections made from both [preference] and [default].
     */
    val preference: MediaPreference

    /**
     * Updates the user's preference.
     */
    fun preferAlliance(alliance: String)
    fun preferResolution(resolution: String)
    fun preferSubtitleLanguage(subtitleLanguage: String)
    fun preferMediaSource(mediaSourceId: String)

    /**
     * Default preferences to use if [preference] does not specify a preference for a property.
     *
     * This can typically be loaded from the data stores.
     */
    val default: MediaPreference

    /**
     * Available options for alliances, summarized from [mediaList].
     *
     * It is distinct and sorted.
     */
    val alliances: List<String>
    val resolutions: List<String>
    val subtitleLanguages: List<String> // null element means no subtitle
    val mediaSources: List<String>

    /**
     * Currently selected alliance, merged from [preference] and [default].
     * It must be one of [alliances].
     *
     * `null` means neither [preference] nor [default] has a value for this property.
     */
    val selectedAlliance: String?
    val selectedResolution: String?
    val selectedSubtitleLanguage: String?
    val selectedMediaSource: String?

    /**
     * Filtered list of media based on the user's preferences and the defaults.
     *
     * There might be multiple media with the same properties, so it is impossible to automatically select one.
     */
    val candidates: List<Media>

    /**
     * The final media selected from the [candidates]. It is guaranteed that [selected] will be one of [candidates].
     */
    val selected: Media?

    /**
     * Selects a media from the [candidates] list. This will update [selected].
     *
     * This will be considered as the user's selection, and will trigger [PreferenceUpdates.select].
     * For default selections, use [makeDefaultSelection]
     *
     * @param candidate must be one of [candidate]. Otherwise this function will have no effect.
     */
    fun select(candidate: Media)

    /**
     * Make a default selection based on current user preferences and the defaults.
     */
    fun makeDefaultSelection()

    /**
     * A event source receiving updates to the user's preferences.
     */
    val preferenceUpdates: PreferenceUpdates
}

interface PreferenceUpdates {
    /**
     * [Flow] representing the stream of all user preference changes.
     * Note that if the user has not explicitly set some properties, it will be null, even if the default has a value.
     */
    val preference: Flow<MediaPreference>

    /**
     * [Flow] representing the stream of all user selections made by [MediaSelectorState.select].
     */
    val select: Flow<Media>
}

internal class MediaSelectorStateImpl(
    mediaListProvider: () -> List<Media>,
    defaultProvider: () -> MediaPreference,
//    mediaListMangler: MediaListMangler = DefaultMediaListMangler(),
) : MediaSelectorState {
    private companion object {
        val defaultUserPreference get() = MediaPreference.Empty
    }

    override val mediaList: List<Media> by derivedStateOf { mediaListProvider() }

//    /**
//     * We need to mangle the media list because different media sources might return medias with the same properties but different titles, etc.
//     * We can't simply randomly choose one because that one might not be able to play.
//     */
//    val mangledMediaList: List<MangledMedia> by derivedStateOf { mediaListMangler.mangle(mediaList) }

    override val default: MediaPreference by derivedStateOf { defaultProvider() }

    override var preference: MediaPreference by mutableStateOf(defaultUserPreference)
    override fun preferAlliance(alliance: String) {
        preference = preference.copy(alliance = alliance)
    }

    override fun preferResolution(resolution: String) {
        preference = preference.copy(resolution = resolution)
    }

    override fun preferSubtitleLanguage(subtitleLanguage: String) {
        preference = preference.copy(subtitleLanguage = subtitleLanguage)
    }

    override fun preferMediaSource(mediaSourceId: String) {
        preference = preference.copy(mediaSourceId = mediaSourceId)
    }

    /*
     * Note:
     * - Distinct by id to avoid duplicates which may crash the UI (since UI may use LazyList which rely on the distinctness of the key)
     * - Sorting is necessary to create stable results
     */

    override val alliances: List<String> by derivedStateOf {
        mediaList.map { it.properties.alliance }
            .fastDistinctBy { it }
            .sorted()
    }
    override val resolutions: List<String> by derivedStateOf {
        mediaList.map { it.properties.resolution }
            .fastDistinctBy { it }
            .sortedByDescending {
                when (it.uppercase()) {
                    "8K", "4320P" -> 6
                    "4K", "2160P" -> 5
                    "2K", "1440P" -> 4
                    "1080P" -> 3
                    "720P" -> 2
                    "480P" -> 1
                    "360P" -> 0
                    else -> -1
                }
            }
    }
    override val subtitleLanguages: List<String> by derivedStateOf {
        mediaList.flatMap { it.properties.subtitleLanguages }
            .fastDistinctBy { it }
            .sortedWith(nullsLast(reverseOrder()))
    }
    override val mediaSources: List<String> by derivedStateOf {
        mediaList.map { it.mediaSourceId }
            .fastDistinctBy { it }
            .sorted()
    }
    override val selectedAlliance: String? by derivedStateOf {
        preference.alliance ?: default.alliance
    }
    override val selectedResolution: String? by derivedStateOf {
        preference.resolution ?: default.resolution
    }
    override val selectedSubtitleLanguage: String? by derivedStateOf {
        preference.subtitleLanguage ?: default.subtitleLanguage
    }
    override val selectedMediaSource: String? by derivedStateOf {
        preference.mediaSourceId ?: default.mediaSourceId
    }
    override val candidates: List<Media> by derivedStateOf {
        infix fun <Pref : Any> Pref?.matches(prop: Pref): Boolean = this == null || this == prop
        infix fun <Pref : Any> Pref?.matches(prop: List<Pref>): Boolean = this == null || this in prop

        mediaList.filter {
            selectedAlliance matches it.properties.alliance &&
                    selectedResolution matches it.properties.resolution &&
                    selectedSubtitleLanguage matches it.properties.subtitleLanguages &&
                    selectedMediaSource matches it.mediaSourceId
        }.sortedByDescending { it.publishedTime }
    }

    // User input
    private var _selected: Media? by mutableStateOf(null)

    override val selected: Media? by derivedStateOf {
        if (_selected in candidates) _selected
        else null
    }

    override fun select(candidate: Media) {
        _selected = candidate
        (preferenceUpdates.select as MutableSharedFlow<Media>).tryEmit(candidate)
    }

    override fun makeDefaultSelection() {
        if (candidates.isNotEmpty()) {
            _selected = candidates.first()
        }
    }

    override val preferenceUpdates = object : PreferenceUpdates {
        override val preference: Flow<MediaPreference> = snapshotFlow {
            this@MediaSelectorStateImpl.preference
        }.flowOn(Dispatchers.Main)
            .filter { it !== defaultUserPreference }
        override val select: MutableSharedFlow<Media> = MutableSharedFlow()
    }
}

//
//internal interface MediaListMangler {
//    /**
//     * Mangles the list of media so that no two media with
//     * the same [properties][MediaProperties] will have the same alliance name.
//     */
//    fun mangle(list: List<Media>): List<MangledMedia>
//}
//
//internal class DefaultMediaListMangler : MediaListMangler {
//    override fun mangle(list: List<Media>): List<MangledMedia> {
//        val groupedByAlliance = list.fastDistinctBy { it.originalTitle }
//            .groupBy { it.properties.alliance }
//
//        if (groupedByAlliance.all { it.value.size == 1 }) {
//            // No need to mangle
//            return list.map { MangledMedia(it, it.properties) }
//        }
//
//        buildList {
//            for ((alliance, medias) in groupedByAlliance) {
//                if (medias.size == 1) {
//                    add(MangledMedia(medias.first(), medias.first().properties))
//                    continue
//                }
//                mangleByDataSource(medias, ::add)
//            }
//        }
//    }
//
//    /**
//     * Input:
//     * ```
//     * A-
//     * ```
//     *
//     * Output:
//     * ```
//     *
//     * ```
//     */
//    private inline fun mangleByDataSource(
//        conflicts: List<Media>,
//        collect: (MangledMedia) -> Unit
//    ): List<Media> {
//        val groupedByDataSource = conflicts.groupBy { it.mediaSourceId }
//        for ((dataSourceId, medias) in groupedByDataSource) {
//            if (medias.size == 1) {
//                collect(MangledMedia(medias.first(), medias.first().properties))
//                continue
//            }
//            if (medias.fastMapTo(mutableSetOf()) { it.id }.size == medias.size) {
//                // No need to mangle
//                medias.forEach { collect(MangledMedia(it, it.properties)) }
//                continue
//            }
//            // Mangle
//            val mangledProperties =
//                medias.first().properties.copy(alliance = "${medias.first().properties.alliance} (${dataSourceId})")
//            medias.forEach { collect(MangledMedia(it, mangledProperties)) }
//        }
//    }
//}