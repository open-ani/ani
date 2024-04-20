package me.him188.ani.app.ui.subject.episode.mediaFetch

import androidx.annotation.MainThread
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.util.fastDistinctBy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.source.MediaSourceLocation

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
): MediaSelectorState = MediaSelectorStateImpl(
    mediaListProvider,
    defaultPreferenceProvider
)

/**
 * 数据源选择器 UI 的状态.
 */
@Stable
interface MediaSelectorState {
    /**
     * The list of media available for selection.
     */
    val mediaList: List<Media>

    /**
     * Preferences explicit set by the user in this session.
     *
     * Use [selectedAlliance], [selectedResolution], [selectedSubtitleLanguageId]
     * for selections made from both [preference] and [default].
     */
    val preference: MediaPreference

    /**
     * Updates the user's preference.
     *
     * @param removeOnExist If true, and the user has already set the same preference for this property, it will be removed.
     */
    @MainThread
    fun preferAlliance(alliance: String, removeOnExist: Boolean = false)

    @MainThread
    fun preferResolution(resolution: String, removeOnExist: Boolean = false)

    @MainThread
    fun preferSubtitleLanguage(subtitleLanguageId: String, removeOnExist: Boolean = false)

    @MainThread
    fun preferMediaSource(mediaSourceId: String, removeOnExist: Boolean = false)

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
    val subtitleLanguageIds: List<String>
    val mediaSources: List<String>

    /**
     * Currently selected alliance, merged from [preference] and [default].
     * It must be one of [alliances].
     *
     * `null` means neither [preference] nor [default] has a value for this property.
     */
    val selectedAlliance: String?
    val selectedResolution: String?
    val selectedSubtitleLanguageId: String?
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
    val selectedFlow: StateFlow<Media?>

    /**
     * Selects a media from the [candidates] list. This will update [selected].
     *
     * This will be considered as the user's selection, and will trigger [PreferenceUpdates.select].
     * For default selections, use [makeDefaultSelection]
     *
     * @param candidate must be one of [candidate]. Otherwise this function will have no effect.
     */
    @MainThread
    fun select(candidate: Media)

    /**
     * Make a default selection based on current user preferences and the defaults.
     */
    @MainThread
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
        /**
         * Placeholder for [preference]
         */
        val initialUserPreference get() = MediaPreference.Empty
    }

    override val mediaList: List<Media> by derivedStateOf { mediaListProvider() }

//    /**
//     * We need to mangle the media list because different media sources might return medias with the same properties but different titles, etc.
//     * We can't simply randomly choose one because that one might not be able to play.
//     */
//    val mangledMediaList: List<MangledMedia> by derivedStateOf { mediaListMangler.mangle(mediaList) }

    /**
     * User-set default
     */
    override val default: MediaPreference by derivedStateOf { defaultProvider() }

    override var preference: MediaPreference by mutableStateOf(initialUserPreference)
        @Deprecated("") set

    @JvmName("setPreference1")
    private fun setPreference(value: MediaPreference) {
        @Suppress("DEPRECATION")
        preference = value
        preferenceUpdates.preference.tryEmit(value)
    }

    private val mergedPreference by derivedStateOf {
        default.merge(preference)
    }

    private var explicitlyRemovedAlliance: Boolean by mutableStateOf(false)
    private var explicitlyRemovedResolution: Boolean by mutableStateOf(false)
    private var explicitlyRemovedSubtitleLanguage: Boolean by mutableStateOf(false)
    private var explicitlyRemovedMediaSource: Boolean by mutableStateOf(false)

    override fun preferAlliance(alliance: String, removeOnExist: Boolean) {
        if (removeOnExist && selectedAlliance == alliance && !explicitlyRemovedAlliance) {
            // was selected either by default or by user, then we remove it
            setPreference(preference.copy(alliance = null))
            explicitlyRemovedAlliance = true
            return
        }
        explicitlyRemovedAlliance = false
        setPreference(preference.copy(alliance = alliance))
    }

    override fun preferResolution(resolution: String, removeOnExist: Boolean) {
        if (removeOnExist && selectedResolution == resolution && !explicitlyRemovedResolution) {
            // was selected either by default or by user, then we remove it
            setPreference(preference.copy(resolution = null))
            explicitlyRemovedResolution = true
            return
        }
        explicitlyRemovedResolution = false
        setPreference(preference.copy(resolution = resolution))
    }

    override fun preferSubtitleLanguage(subtitleLanguageId: String, removeOnExist: Boolean) {
        if (removeOnExist && selectedSubtitleLanguageId == subtitleLanguageId && !explicitlyRemovedSubtitleLanguage) {
            // was selected either by default or by user, then we remove it
            setPreference(preference.copy(subtitleLanguageId = null))
            explicitlyRemovedSubtitleLanguage = true
            return
        }
        explicitlyRemovedSubtitleLanguage = false
        setPreference(preference.copy(subtitleLanguageId = subtitleLanguageId))
    }

    override fun preferMediaSource(mediaSourceId: String, removeOnExist: Boolean) {
        if (removeOnExist && selectedMediaSource == mediaSourceId && !explicitlyRemovedMediaSource) {
            // was selected either by default or by user, then we remove it
            setPreference(preference.copy(mediaSourceId = null))
            explicitlyRemovedMediaSource = true
            return
        }
        explicitlyRemovedMediaSource = false
        setPreference(preference.copy(mediaSourceId = mediaSourceId))
    }

    /*
     * Note:
     * - Distinct by id to avoid duplicates which may crash the UI (since UI may use LazyList which rely on the distinctness of the key)
     * - Sorting is necessary to create stable results
     */

    override val alliances: List<String> by derivedStateOf {
        mediaList.map { it.properties.alliance }
            .fastDistinctBy { it }
            .sortedBy { it.length }
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
    override val subtitleLanguageIds: List<String> by derivedStateOf {
        mediaList.flatMap { it.properties.subtitleLanguageIds }
            .fastDistinctBy { it }
            .sorted()
    }
    override val mediaSources: List<String> by derivedStateOf {
        mediaList.map { it.mediaSourceId }
            .fastDistinctBy { it }
            .sorted()
    }

    private val allianceRegexes by derivedStateOf {
        mergedPreference.alliancePatterns?.map { it.toRegex() } ?: emptyList()
    }

    /**
     * Made by [makeDefaultSelection]
     */
    private var allianceByDefaultSelection by mutableStateOf<String?>(null)
    override val selectedAlliance: String? by derivedStateOf {
        if (explicitlyRemovedAlliance) return@derivedStateOf null
        mergedPreference.alliance?.takeIf { it in alliances }?.let { return@derivedStateOf it }
        allianceByDefaultSelection
    }

    private var resolutionByDefaultSelection by mutableStateOf<String?>(null)
    override val selectedResolution: String? by derivedStateOf {
        if (explicitlyRemovedResolution) return@derivedStateOf null
        mergedPreference.resolution?.takeIf { it in resolutions }?.let { return@derivedStateOf it }
        resolutionByDefaultSelection
    }

    private var subtitleLanguageIdByDefaultSelection by mutableStateOf<String?>(null)
    override val selectedSubtitleLanguageId: String? by derivedStateOf {
        if (explicitlyRemovedSubtitleLanguage) return@derivedStateOf null
        mergedPreference.subtitleLanguageId?.takeIf { it in subtitleLanguageIds }
            ?.let { return@derivedStateOf it }

        subtitleLanguageIdByDefaultSelection
    }

    private var mediaSourceByDefaultSelection by mutableStateOf<String?>(null)
    override val selectedMediaSource: String? by derivedStateOf {
        if (explicitlyRemovedMediaSource) return@derivedStateOf null
        mergedPreference.mediaSourceId?.takeIf { it in mediaSources }?.let { return@derivedStateOf it }
        mediaSourceByDefaultSelection
    }

    override val candidates: List<Media> by derivedStateOf {
        infix fun <Pref : Any> Pref?.matches(prop: Pref): Boolean = this == null || this == prop
        infix fun <Pref : Any> Pref?.matches(prop: List<Pref>): Boolean = this == null || this in prop

        mediaList.filter {
            selectedAlliance matches it.properties.alliance &&
                    selectedResolution matches it.properties.resolution &&
                    selectedSubtitleLanguageId matches it.properties.subtitleLanguageIds &&
                    (selectedMediaSource matches it.mediaSourceId || it.location == MediaSourceLocation.LOCAL) // always show local, so that [makeDefaultSelection] will select a local one
        }.sortedWith(
            compareByDescending<Media> {
                if (it.location == MediaSourceLocation.LOCAL) 1 else 0
            }.thenByDescending { it.publishedTime }
        )
    }

    // User input
    private var _selected: Media? by mutableStateOf(null)

    override val selectedFlow: MutableStateFlow<Media?> = MutableStateFlow(null)
    override val selected: Media? by derivedStateOf {
        if (_selected in candidates) _selected
        else null
    }

    override fun select(candidate: Media) {
        _selected = candidate
        selectedFlow.value = candidate
        preferenceUpdates.select.tryEmit(candidate)
    }

    private fun selectDefault(
        media: Media,
        languageId: String?,
    ) {
        this.allianceByDefaultSelection = media.properties.alliance
        this.resolutionByDefaultSelection = media.properties.resolution
        this.subtitleLanguageIdByDefaultSelection = languageId ?: media.properties.subtitleLanguageIds.firstOrNull()
        this.mediaSourceByDefaultSelection = media.mediaSourceId
        this._selected = media
        selectedFlow.value = media
    }

    override fun makeDefaultSelection() {
        if (candidates.isEmpty()) return

        val languageIds = sequence {
            selectedSubtitleLanguageId?.let {
                yield(it)
                return@sequence
            }
            yieldAll(mergedPreference.fallbackSubtitleLanguageIds.orEmpty())
        }
        val resolutions = sequence {
            selectedResolution?.let {
                yield(it)
                return@sequence
            }
            yieldAll(mergedPreference.fallbackResolutions.orEmpty())
        }
        val alliances = sequence {
            selectedAlliance?.let {
                yield(it)
                return@sequence
            }
            if (allianceRegexes.isEmpty()) {
                yieldAll(alliances)
            } else {
                for (regex in allianceRegexes) {
                    for (alliance in alliances) {
                        // lazy 匹配, 但没有 cache, 若 `alliances` 反复访问则会进行多次匹配
                        if (regex.find(alliance) != null) yield(alliance)
                    }
                }
            }
        }
        val mediaSources = sequence {
            selectedMediaSource?.let {
                yield(it)
                return@sequence
            }
            val fallback = mergedPreference.fallbackMediaSourceIds
            if (fallback == null) {
                yield(null)
            } else {
                yieldAll(fallback)
            }
        }

        // For rules discussion, see #174

        // 选择顺序
        // 1. 分辨率
        // 2. 字幕语言
        // 3. 字幕组
        // 4. 数据源

        // 规则: 
        // - 分辨率最高优先: 1080P >> 720P, 但不能为了要 4K 而选择不想要的字幕语言
        // - 不要为了选择偏好字幕组而放弃其他字幕组的更好的语言

        // 注意, 这个函数会跑在主线程
        // 实际上这些 loop 都只需要跑一次, 除了分辨率. 而这也只需要多遍历两次 list 而已.
        // 例如: 4K (无匹配) -> 2K (无匹配) -> 1080P -> 简中 -> 桜都 -> Mikan

        for (resolution in resolutions) { // DFS 尽可能匹配第一个分辨率
            val filteredByResolution = candidates.filter { resolution == it.properties.resolution }
            if (filteredByResolution.isEmpty()) continue

            for (languageId in languageIds) {
                val filteredByLanguage = filteredByResolution.filter { languageId in it.properties.subtitleLanguageIds }
                if (filteredByLanguage.isEmpty()) continue

                for (alliance in alliances) { // 能匹配第一个最好
                    // 这里是消耗最大的地方, 因为有正则匹配
                    val filteredByAlliance = filteredByLanguage.filter { alliance == it.properties.alliance }
                    if (filteredByAlliance.isEmpty()) continue

                    for (mediaSource in mediaSources) {
                        val filteredByMediaSource = filteredByAlliance.filter {
                            mediaSource == null || mediaSource == it.mediaSourceId
                        }
                        if (filteredByMediaSource.isEmpty()) continue
                        selectDefault(filteredByMediaSource.first(), languageId)
                        return
                    }
                }

                // 字幕组没匹配到, 但最好不要换更差语言

                for (mediaSource in mediaSources) {
                    val filteredByMediaSource = filteredByLanguage.filter {
                        mediaSource == null || mediaSource == it.mediaSourceId
                    }
                    if (filteredByMediaSource.isEmpty()) continue
                    selectDefault(filteredByMediaSource.first(), languageId)
                    return
                }
            }

            // 该分辨率下无字幕语言, 换下一个分辨率
        }

        selectDefault(candidates.first(), null)
    }

    class PreferenceUpdatesImpl : PreferenceUpdates {
        override val preference: MutableSharedFlow<MediaPreference> = MutableSharedFlow(extraBufferCapacity = 1)
        override val select: MutableSharedFlow<Media> =
            MutableSharedFlow(extraBufferCapacity = 1) // see usage before you change it
    }

    override val preferenceUpdates = PreferenceUpdatesImpl()
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