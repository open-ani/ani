/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.domain.media.selector

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import me.him188.ani.app.data.models.subject.SubjectManager
import me.him188.ani.app.data.models.subject.subjectCompletedFlow
import me.him188.ani.app.data.repository.EpisodePreferencesRepository
import me.him188.ani.app.data.repository.SettingsRepository
import me.him188.ani.app.domain.media.fetch.MediaSourceManager
import me.him188.ani.app.domain.media.selector.MediaSelectorFactory.Companion.withRepositories
import me.him188.ani.datasources.api.Media
import org.koin.core.Koin
import org.koin.mp.KoinPlatform
import kotlin.coroutines.CoroutineContext

/**
 * 提前给予 episode 和 subject 的 context, 用于构造 [MediaSelector].
 *
 * @see withRepositories
 */
interface MediaSelectorFactory {
    fun create(
        subjectId: Int,
        mediaList: Flow<List<Media>>,
        flowCoroutineContext: CoroutineContext = Dispatchers.Default,
    ): MediaSelector // 如果要'挂载'自动保存配置, 可以为这个的返回值操作.

    companion object {
        fun withKoin(koin: Koin = KoinPlatform.getKoin()): MediaSelectorFactory = withRepositories(
            episodePreferencesRepository = koin.get(),
            settingsRepository = koin.get(),
            subjectManager = koin.get(),
            mediaSourceManager = koin.get(),
        )

        fun withRepositories(
            episodePreferencesRepository: EpisodePreferencesRepository,
            settingsRepository: SettingsRepository,
            subjectManager: SubjectManager,
            mediaSourceManager: MediaSourceManager,
            subtitlePreferences: MediaSelectorSubtitlePreferences = MediaSelectorSubtitlePreferences.CurrentPlatform,
        ): MediaSelectorFactory = object : MediaSelectorFactory {
            override fun create(
                subjectId: Int,
                mediaList: Flow<List<Media>>,
                flowCoroutineContext: CoroutineContext
            ): MediaSelector {
                return DefaultMediaSelector(
                    MediaSelectorContext.createFlow(
                        subjectManager.subjectCompletedFlow(subjectId),
                        mediaSourceManager.allInstances,
                        flowOf(subtitlePreferences),
                    ),
                    mediaList,
                    savedUserPreference = episodePreferencesRepository.mediaPreferenceFlow(subjectId),
                    savedDefaultPreference = settingsRepository.defaultMediaPreference.flow,
                    mediaSelectorSettings = settingsRepository.mediaSelectorSettings.flow,
                    flowCoroutineContext = flowCoroutineContext,
                )
            }
        }
    }
}
