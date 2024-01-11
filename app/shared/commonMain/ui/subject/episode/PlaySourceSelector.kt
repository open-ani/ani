package me.him188.ani.app.ui.subject.episode

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import me.him188.ani.datasources.api.topic.Resolution

/**
 * 播放源筛选器.
 *
 * [setPreferredResolution] 和 [setPreferredSubtitleLanguage] 后, [availableAlliances] 会更新.
 *
 * [setPreferredAlliance] 后, [targetPlaySource] 即为目标播放源 [PlaySource].
 */
@Stable
class PlaySourceSelector(
    initialPlaySources: List<PlaySource>,
    playSources: Flow<Collection<PlaySource>>, // may change concurrently
    coroutineScope: CoroutineScope,
) {
    /// subtitleLanguages

    val subtitleLanguages: StateFlow<List<String>> =
        playSources
            .map { list ->
                println("subtitleLanguages: ${list.map { it.subtitleLanguage }.distinct()}")
                list.map { it.subtitleLanguage }.distinct()
            }
            .stateIn(
                coroutineScope,
                started = SharingStarted.Eagerly,
                initialPlaySources.map { it.subtitleLanguage }.distinct()
            )

    // TODO: 默认字幕语言
    private val _preferredSubtitleLanguage: MutableStateFlow<String> =
        MutableStateFlow(subtitleLanguages.value.firstOrNull() ?: "简中") // 只有在初始化和没找到时为空

    val preferredSubtitleLanguage: StateFlow<String> get() = _preferredSubtitleLanguage

    fun setPreferredSubtitleLanguage(language: String) {
        _preferredSubtitleLanguage.value = language
    }

    /// resolutions

    // 从大到小
    val resolutions: StateFlow<List<Resolution>> = playSources
        .map { list ->
            list.map { it.resolution }
                .distinct()
                .sortedByDescending { it }
        }
        .stateIn(
            coroutineScope,
            started = SharingStarted.Eagerly,
            initialPlaySources.map { it.resolution }.distinct(),
        )
    private val _preferredResolution: MutableStateFlow<Resolution> =
        MutableStateFlow(resolutions.value.maxOrNull() ?: Resolution.R1080P) // 只有在初始化和没找到时为空

    val preferredResolution: StateFlow<Resolution> get() = _preferredResolution

    fun setPreferredResolution(resolution: Resolution) {
        _preferredResolution.value = resolution
    }


    /// alliances

    val availableAlliances = combine(
        playSources,
        preferredResolution,
        preferredSubtitleLanguage,
    ) { list, resolution, language ->
        list.filter { it.resolution == resolution && it.subtitleLanguage == language }
            .groupBy { it.alliance }
            .flatMap { (name, values) ->
                values.createCandidates(name)
            }
    }.shareIn(coroutineScope, started = SharingStarted.Eagerly, replay = 1)

    private val _preferredAlliance: MutableStateFlow<PlaySourceCandidate?> = MutableStateFlow(null) // TODO: 记录起来 

    val preferredAlliance: StateFlow<PlaySourceCandidate?> get() = _preferredAlliance

    fun setPreferredAlliance(resolution: PlaySourceCandidate) {
        _preferredAlliance.value = resolution
    }

    /// filtered

    val targetPlaySource =
        combine(
            playSources,
            preferredResolution,
            preferredSubtitleLanguage,
            preferredAlliance,
        ) { list, resolution, language, alliance ->
            list.firstOrNull { it.resolution == resolution && it.subtitleLanguage == language && it.alliance == alliance?.playSource?.alliance }
        }.stateIn(coroutineScope, started = SharingStarted.Eagerly, null)
}

@Immutable
class PlaySourceCandidate(
    val displayName: String,
    val playSource: PlaySource,
) {
    val id get() = playSource.id
}

private fun List<PlaySource>.createCandidates(name: String): List<PlaySourceCandidate> =
    if (size == 1) {
        map { PlaySourceCandidate(name, it) } // 只有一个, 不用区分
    } else {
        mapIndexed { index, it ->
            PlaySourceCandidate("$name ${index + 1}", it) // 有多个, 加上序号区分
        }
    }