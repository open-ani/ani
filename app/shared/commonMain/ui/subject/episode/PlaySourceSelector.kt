package me.him188.ani.app.ui.subject.episode

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import me.him188.ani.app.data.PreferredAllianceRepository
import me.him188.ani.app.ui.foundation.HasBackgroundScope
import me.him188.ani.datasources.api.topic.Resolution
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * 播放源筛选器.
 */
@Stable
class PlaySourceSelector(
    subjectId: Flow<Int>,
    initialPlaySources: List<PlaySource>,
    playSources: Flow<Collection<PlaySource>>, // may change concurrently
    override val backgroundScope: CoroutineScope,
) : KoinComponent, HasBackgroundScope {
    private val preferredAllianceRepository: PreferredAllianceRepository by inject()
    private val subjectId = subjectId.stateIn(backgroundScope, SharingStarted.Eagerly, 1)

    /// subtitleLanguages

    val subtitleLanguages: StateFlow<List<String>> =
        playSources
            .map { list ->
                println("subtitleLanguages: ${list.map { it.subtitleLanguage }.distinct()}")
                list.map { it.subtitleLanguage }.distinct()
            }
            .stateIn(
                backgroundScope,
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
            backgroundScope,
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

    /**
     * 记录的上次选择的联盟.
     */
    private val storedPreferredAlliance = subjectId
        .flatMapLatest { preferredAllianceRepository.preferredAlliance(it) }
        .map { it }
        .stateInBackground()

    /**
     * 当前可用的联盟.
     */
    val candidates = combine(
        playSources,
        preferredResolution,
        preferredSubtitleLanguage,
    ) { list, resolution, language ->
        list.filter { it.resolution == resolution && it.subtitleLanguage == language }
            .groupBy { it.alliance }
            .flatMap { (name, values) ->
                values.createCandidates(name)
            }
    }.stateInBackground()

    /**
     * 第一个可用的联盟.
     */
    private val firstAlliance = candidates.map {
        it?.firstOrNull()?.allianceMangled
    }.shareInBackground()

    /**
     * 用户本次会话中选择的联盟.
     */
    private val userPreferredAlliance: MutableStateFlow<PlaySourceCandidate?> = MutableStateFlow(null)

    val finalSelectedAllianceMangled = combine(
        userPreferredAlliance,
        firstAlliance,
        storedPreferredAlliance
    ) { userPreferredAlliance, firstAlliance, storedPreferredAlliance ->
        userPreferredAlliance?.allianceMangled ?: storedPreferredAlliance ?: firstAlliance
    }.shareInBackground()

    /**
     * 设置用户期望的字幕组.
     */
    suspend fun setPreferredCandidate(candidate: PlaySourceCandidate) {
        userPreferredAlliance.value = candidate
        preferredAllianceRepository.setPreferredAlliance(subjectId.value, candidate.playSource.alliance)
    }

    /// filtered

    /**
     * 最终观看或下载的播放源.
     */
    val targetPlaySourceCandidate = combine(
        candidates.filterNotNull(),
        preferredResolution,
        preferredSubtitleLanguage,
        finalSelectedAllianceMangled,
    ) { list, resolution, language, allianceMangled ->
        list.firstOrNull { playSource ->
            playSource.playSource.resolution == resolution && playSource.playSource.subtitleLanguage == language && playSource.allianceMangled == allianceMangled
        }
    }.stateInBackground()
}

@Immutable
class PlaySourceCandidate(
    val allianceMangled: String,
    val playSource: PlaySource,
) {
    val id get() = playSource.id
}

/**
 * 展示给用户的内容
 */
@Stable
fun PlaySourceCandidate.render(): String {
    val playing = this
    return listOf(
        playing.playSource.resolution,
        playing.playSource.subtitleLanguage,
        playing.playSource.size,
        playing.allianceMangled,
    ).joinToString(" · ")
}

private fun List<PlaySource>.createCandidates(name: String): List<PlaySourceCandidate> =
    if (size == 1) {
        map { PlaySourceCandidate(name, it) } // 只有一个, 不用区分
    } else {
        mapIndexed { index, it ->
            PlaySourceCandidate("$name ${index + 1}", it) // 有多个, 加上序号区分
        }
    }