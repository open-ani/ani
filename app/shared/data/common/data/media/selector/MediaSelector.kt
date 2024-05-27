package me.him188.ani.app.data.media.selector

import androidx.compose.runtime.Stable
import androidx.compose.ui.util.fastFirstOrNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaPreference
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.source.MediaSourceKind
import kotlin.coroutines.CoroutineContext

/**
 * 数据源选择器
 */
interface MediaSelector {
    /**
     * 搜索到的全部的列表
     */
    val mediaList: Flow<List<Media>>

    val alliance: MediaPreferenceItem<String>
    val resolution: MediaPreferenceItem<String>
    val subtitleLanguageId: MediaPreferenceItem<String>
    val mediaSourceId: MediaPreferenceItem<String>

    /**
     * 经过筛选后的列表
     */
    val filteredCandidates: Flow<List<Media>>

    /**
     * The final media selected from the [filteredCandidates].
     */
    val selected: Flow<Media?>

    /**
     * 用于监听 [select] 等事件
     */
    val events: MediaSelectorEvents

    /**
     * Selects a media from the [filteredCandidates] list. This will update [selected].
     *
     * This will be considered as the user's selection, and will trigger [MediaSelectorEvents.onSelect].
     * For default selections, use [trySelectDefault]
     *
     * @param candidate must be one of [candidate]. Otherwise this function will have no effect.
     */
    suspend fun select(candidate: Media)

    /**
     * 尝试使用目前的偏好设置, 自动选择一个. 当已经有用户选择或默认选择时返回 `null`.
     */
    suspend fun trySelectDefault(): Media?

    /**
     * 尝试选择缓存作为默认选择, 如果没有缓存则不做任何事情
     * @return 是否成功选择了缓存
     */
    suspend fun trySelectCached(): Media?

    /**
     * 逐渐取消选择, 直到 [filteredCandidates] 有至少一个元素.
     */
    suspend fun removePreferencesUntilFirstCandidate()
}

interface MediaSelectorEvents {
    val onSelect: Flow<SelectEvent>

    /**
     * 用户偏好发生变化, 这可能是 [MediaSelector.select], 也可能是 [MediaPreferenceItem.prefer].
     *
     * flow 的值为新的用户设置
     */
    val onChangePreference: Flow<MediaPreference>
}

data class SelectEvent(
    val media: Media?,
    val subtitleLanguageId: String?,
)

class MutableMediaSelectorEvents(
    replay: Int = 0,
    extraBufferCapacity: Int = 1,
    onBufferOverflow: BufferOverflow = BufferOverflow.DROP_OLDEST,
) : MediaSelectorEvents {
    override val onSelect: MutableSharedFlow<SelectEvent> =
        MutableSharedFlow(replay, extraBufferCapacity, onBufferOverflow)
    override val onChangePreference: MutableSharedFlow<MediaPreference> =
        MutableSharedFlow(replay, extraBufferCapacity, onBufferOverflow)
}


/**
 * 一个筛选项目
 * @param T 例如字幕语言
 */
@Stable
interface MediaPreferenceItem<T : Any> {
    /**
     * 目前搜索到的列表
     */
    val available: Flow<List<T>>

    /**
     * 用户的选择, 可能为空
     */
    val userSelected: Flow<OptionalPreference<T>>

    /**
     * 默认的选择, 为空表示没有默认的选择
     */
    val defaultSelected: Flow<T?>

    /**
     * [userSelected] 与 [defaultSelected] 合并考虑的选择
     */
    val finalSelected: Flow<T?>

    /**
     * 用户选择
     */
    suspend fun prefer(value: T)

    /**
     * 删除已有的选择
     */
    suspend fun removePreference()
}

class DefaultMediaSelector(
    mediaListNotCached: Flow<List<Media>>,
    /**
     * 数据库中的用户偏好. 仅当用户在本次会话中没有设置新的偏好时, 才会使用此偏好 (跟随 flow 更新). 不能为空 flow, 否则 select 会一直挂起.
     */
    savedUserPreference: Flow<MediaPreference>,
    /**
     * 若 [savedUserPreference] 未指定某个属性的偏好, 则使用此默认值. 不能为空 flow, 否则 select 会一直挂起.
     */
    private val savedDefaultPreference: Flow<MediaPreference>,
    /**
     * context for flow
     */
    private val flowCoroutineContext: CoroutineContext = Dispatchers.Default,
    /**
     * 是否将 [savedDefaultPreference] 和计算工作缓存. 这会导致有些许延迟. 在测试时需要关闭.
     */
    private val enableCaching: Boolean = true,
) : MediaSelector {
    private fun <T> Flow<T>.cached(): Flow<T> {
        if (!enableCaching) return this
        return this.shareIn(CoroutineScope(flowCoroutineContext), SharingStarted.WhileSubscribed(), replay = 1)
    }

    override val mediaList: Flow<List<Media>> = mediaListNotCached.cached()
    private val savedUserPreferenceNotCached = savedUserPreference
    private val savedUserPreference: Flow<MediaPreference> = savedUserPreference.cached()

    override val alliance = mediaPreferenceItem(
        "alliance",
        getFromMediaList = { list ->
            list.mapTo(HashSet(list.size)) { it.properties.alliance }
                .sortedBy { it }
        },
        getFromPreference = { it.alliance }
    )
    override val resolution = mediaPreferenceItem(
        "resolution",
        getFromMediaList = { list ->
            list.mapTo(HashSet(list.size)) { it.properties.resolution }
                .sortedBy { it }
        },
        getFromPreference = { it.resolution }
    )
    override val subtitleLanguageId = mediaPreferenceItem(
        "subtitleLanguage",
        getFromMediaList = { list ->
            list.flatMapTo(HashSet(list.size)) { it.properties.subtitleLanguageIds }
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
        },
        getFromPreference = { it.subtitleLanguageId }
    )
    override val mediaSourceId = mediaPreferenceItem(
        "mediaSource",
        getFromMediaList = { list ->
            list.mapTo(HashSet(list.size)) { it.properties.resolution }
                .sortedBy { it }
        },
        getFromPreference = { it.mediaSourceId }
    )

    /**
     * 当前会话中的生效偏好
     */
    private val newPreferences = combine(
        savedDefaultPreference,
        alliance.finalSelected,
        resolution.finalSelected,
        subtitleLanguageId.finalSelected,
        mediaSourceId.finalSelected,
    ) { default, alliance, resolution, subtitleLanguage, mediaSourceId ->
        default.copy(
            alliance = alliance,
            resolution = resolution,
            subtitleLanguageId = subtitleLanguage,
            mediaSourceId = mediaSourceId,
        )
    }.flowOn(flowCoroutineContext) // must not cache

    // collect 一定会计算
    private val filteredCandidatesNotCached = combine(this.mediaList, newPreferences) { mediaList, mergedPreferences ->
        infix fun <Pref : Any> Pref?.matches(prop: Pref): Boolean = this == null || this == prop
        infix fun <Pref : Any> Pref?.matches(prop: List<Pref>): Boolean = this == null || this in prop

        /**
         * 当 [it] 满足当前筛选条件时返回 `true`.
         */
        fun filterCandidate(it: Media): Boolean {
            if (it.kind == MediaSourceKind.LocalCache) {
                return true // always show local, so that [makeDefaultSelection] will select a local one
            }

            return mergedPreferences.alliance matches it.properties.alliance &&
                    mergedPreferences.resolution matches it.properties.resolution &&
                    mergedPreferences.subtitleLanguageId matches it.properties.subtitleLanguageIds &&
                    mergedPreferences.mediaSourceId matches it.mediaSourceId
        }

        mediaList.filter {
            filterCandidate(it)
        }
    }
    override val filteredCandidates: Flow<List<Media>> = filteredCandidatesNotCached.cached()

    override val selected: MutableStateFlow<Media?> = MutableStateFlow(null)
    override val events = MutableMediaSelectorEvents()

    override suspend fun select(candidate: Media) {
        selected.value = candidate

        alliance.preferWithoutBroadcast(candidate.properties.alliance)
        resolution.preferWithoutBroadcast(candidate.properties.resolution)
        mediaSourceId.preferWithoutBroadcast(candidate.mediaSourceId)
        candidate.properties.subtitleLanguageIds.singleOrNull()?.let {
            subtitleLanguageId.preferWithoutBroadcast(it)
        }

        // Publish events
        broadcastChangePreference()
        events.onSelect.emit(SelectEvent(candidate, null))
    }

    private fun selectDefault(candidate: Media): Media? {
        if (!selected.compareAndSet(null, candidate)) return null
        // 自动选择时不更新 preference
        return candidate
    }

    private suspend fun broadcastChangePreference(overrideLanguageId: String? = null) {
        if (events.onChangePreference.subscriptionCount.value == 0) return // 没人监听, 就不用算新的 preference 了
        val savedUserPreference = savedUserPreferenceNotCached.first()
        val preference = newPreferences.first() // must access un-cached
        events.onChangePreference.emit(
            savedUserPreference.copy(
                alliance = preference.alliance,
                resolution = preference.resolution,
                subtitleLanguageId = overrideLanguageId ?: preference.subtitleLanguageId,
                mediaSourceId = preference.mediaSourceId,
            )
        )
    }


    override suspend fun trySelectDefault(): Media? {
        if (selected.value != null) return null

        val mergedPreference = newPreferences.first()
        val selectedSubtitleLanguageId = mergedPreference.subtitleLanguageId
        val selectedResolution = mergedPreference.resolution
        val selectedAlliance = mergedPreference.alliance
        val selectedMediaSource = mergedPreference.mediaSourceId
        val allianceRegexes = mergedPreference.alliancePatterns.orEmpty().map { it.toRegex() }
        val availableAlliances = alliance.available.first()

        val candidates = filteredCandidates.first()
        if (candidates.isEmpty()) return null

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
                yieldAll(availableAlliances)
            } else {
                for (regex in allianceRegexes) {
                    for (alliance in availableAlliances) {
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
                        return selectDefault(filteredByMediaSource.first())
                    }
                }

                // 字幕组没匹配到, 但最好不要换更差语言

                for (mediaSource in mediaSources) {
                    val filteredByMediaSource = filteredByLanguage.filter {
                        mediaSource == null || mediaSource == it.mediaSourceId
                    }
                    if (filteredByMediaSource.isEmpty()) continue
                    return selectDefault(filteredByMediaSource.first())
                }
            }

            // 该分辨率下无字幕语言, 换下一个分辨率
        }

        return selectDefault(candidates.first())
    }

    override suspend fun trySelectCached(): Media? {
        if (selected.value != null) return null
        val candidates = filteredCandidates.first()
        val cached = candidates.fastFirstOrNull { it.kind == MediaSourceKind.LocalCache } ?: return null
        return selectDefault(cached)
    }

    override suspend fun removePreferencesUntilFirstCandidate() {
        if (filteredCandidates.first().isNotEmpty()) return
        alliance.removePreference()
        if (filteredCandidatesNotCached.first().isNotEmpty()) return
        resolution.removePreference()
        if (filteredCandidatesNotCached.first().isNotEmpty()) return
        subtitleLanguageId.removePreference()
        if (filteredCandidatesNotCached.first().isNotEmpty()) return
        mediaSourceId.removePreference()
    }

    interface MediaPreferenceItemImpl<T : Any> : MediaPreferenceItem<T> {
        fun preferWithoutBroadcast(value: T)
    }

    private inline fun <reified T : Any> mediaPreferenceItem(
        debugName: String,
        crossinline getFromMediaList: (list: List<Media>) -> List<T>,
        crossinline getFromPreference: (MediaPreference) -> T?,
    ) = object : MediaPreferenceItemImpl<T> {
        override val available: Flow<List<T>> = mediaList.map { list ->
            getFromMediaList(list)
        }.flowOn(flowCoroutineContext).cached()

        // 当前用户覆盖的选择. 一旦用户有覆盖, 就不要用默认去修改它了
        private val overridePreference: MutableStateFlow<OptionalPreference<T>> =
            MutableStateFlow(OptionalPreference.noPreference())

        /**
         * must not cache, see [removePreferencesUntilFirstCandidate]
         */
        override val userSelected: Flow<OptionalPreference<T>> =
            combine(savedUserPreference, overridePreference) { preference, override ->
                override.flatMapNoPreference {
                    OptionalPreference.preferIfNotNull(getFromPreference(preference))
                }
            }.flowOn(flowCoroutineContext)

        override val defaultSelected: Flow<T?> = savedDefaultPreference.map { getFromPreference(it) }
            .flowOn(flowCoroutineContext).cached()

        /**
         * must not cache, see [removePreferencesUntilFirstCandidate]
         */
        override val finalSelected: Flow<T?> = combine(userSelected, defaultSelected) { user, default ->
            user.orElse { default }
        }.flowOn(flowCoroutineContext)

        override suspend fun removePreference() {
            overridePreference.value = OptionalPreference.preferNoValue()
        }

        override fun preferWithoutBroadcast(value: T) {
            overridePreference.value = OptionalPreference.prefer(value)
        }

        override suspend fun prefer(value: T) {
            preferWithoutBroadcast(value)
            broadcastChangePreference(null)
        }

        override fun toString(): String = "MediaPreferenceItem($debugName)"
    }
}