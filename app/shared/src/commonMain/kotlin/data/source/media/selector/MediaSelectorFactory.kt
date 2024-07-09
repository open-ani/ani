package me.him188.ani.app.data.source.media.selector

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import me.him188.ani.app.data.model.subject.SubjectManager
import me.him188.ani.app.data.model.subject.subjectCompletedFlow
import me.him188.ani.app.data.repository.EpisodePreferencesRepository
import me.him188.ani.app.data.repository.SettingsRepository
import me.him188.ani.app.data.source.media.MediaSourceManager
import me.him188.ani.app.data.source.media.selector.MediaSelectorFactory.Companion.withRepositories
import me.him188.ani.datasources.api.Media
import org.koin.core.Koin
import org.koin.core.context.GlobalContext
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
        fun withKoin(koin: Koin = GlobalContext.get()): MediaSelectorFactory = withRepositories(
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
