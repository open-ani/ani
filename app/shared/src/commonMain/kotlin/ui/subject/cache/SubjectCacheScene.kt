package me.him188.ani.app.ui.subject.cache

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.take
import me.him188.ani.app.data.media.MediaCacheManager
import me.him188.ani.app.data.media.MediaSourceManager
import me.him188.ani.app.data.media.cache.requester.EpisodeCacheRequest
import me.him188.ani.app.data.media.cache.requester.EpisodeCacheRequester
import me.him188.ani.app.data.media.selector.MediaSelectorFactory
import me.him188.ani.app.data.models.MediaSelectorSettings
import me.him188.ani.app.data.repositories.SettingsRepository
import me.him188.ani.app.data.subject.SubjectManager
import me.him188.ani.app.data.subject.isKnownBroadcast
import me.him188.ani.app.data.subject.nameCnOrName
import me.him188.ani.app.data.subject.subjectInfoFlow
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.ui.external.placeholder.placeholder
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.widgets.TopAppBarGoBackButton
import me.him188.ani.app.ui.settings.SettingsTab
import me.him188.ani.app.ui.settings.framework.components.SettingsScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Stable
interface SubjectCacheViewModel {
    val subjectId: Int
    val subjectTitle: String?

    val mediaSelectorSettingsFlow: Flow<MediaSelectorSettings>

    /**
     * 单个条目的缓存管理页面的状态
     */
    val cacheListState: EpisodeCacheListState
}

open class TestSubjectCacheViewModel(
    override val subjectId: Int,
    override val subjectTitle: String?,
    override val cacheListState: EpisodeCacheListState,
    override val mediaSelectorSettingsFlow: Flow<MediaSelectorSettings>
) : SubjectCacheViewModel

@Stable
class SubjectCacheViewModelImpl(
    override val subjectId: Int,
) : AbstractViewModel(), KoinComponent, SubjectCacheViewModel {
    private val subjectManager: SubjectManager by inject()
    private val settingsRepository: SettingsRepository by inject()
    private val cacheManager: MediaCacheManager by inject()
    private val mediaSourceManager: MediaSourceManager by inject()

    private val subjectInfoFlow = subjectManager.subjectInfoFlow(subjectId).shareInBackground()
    override val subjectTitle by subjectInfoFlow.map { it.nameCnOrName }.produceState(null)
    override val mediaSelectorSettingsFlow: Flow<MediaSelectorSettings> get() = settingsRepository.mediaSelectorSettings.flow

    private val episodeCollectionsFlowNotCached = subjectManager.episodeCollectionsFlow(subjectId).retry()

    /**
     * 单个条目的缓存管理页面的状态
     */
    override val cacheListState: EpisodeCacheListState = EpisodeCacheListStateImpl(
        episodesLazy = episodeCollectionsFlowNotCached.take(1).mapLatest { episodes ->
            // 每个 episode 都为一个 flow, 然后合并
            episodes.map { episodeCollection ->
                val episode = episodeCollection.episodeInfo

                val cacheStatusFlow = cacheManager.cacheStatusForEpisode(subjectId, episode.id)

                EpisodeCacheState(
                    episodeId = episode.id,
                    cacheRequesterLazy = {
                        EpisodeCacheRequester(
                            mediaSourceManager.mediaFetcher,
                            MediaSelectorFactory.withKoin(),
                            storagesLazy = cacheManager.enabledStorages,
                        )
                    },
                    info = MutableStateFlow(
                        EpisodeCacheInfo(
                            sort = episode.sort,
                            ep = episode.ep,
                            title = episode.nameCn,
                            watchStatus = episodeCollection.collectionType,
                            hasPublished = episode.isKnownBroadcast,
                        ),
                    ),
                    cacheStatusFlow = cacheStatusFlow,
                    parentCoroutineContext = backgroundScope.coroutineContext,
                )
            }
        },
        onRequestCache = { episode, autoSelectByCached ->
            episode.cacheRequester.request(
                EpisodeCacheRequest(
                    subjectInfoFlow.first(),
                    subjectManager.getEpisodeInfo(episode.episodeId),
                ),
            ).run {
                if (autoSelectByCached) {
                    tryAutoSelectByCachedSeason(
                        cacheManager.listCacheForSubject(subjectId).first(),
                    )
                } else this
            }
        },
        onRequestCacheComplete = { target ->
            target.storage.cache(target.media, target.metadata)
        },
        onDeleteCache = { episode ->
            val episodeId = episode.episodeId.toString()
            cacheManager.deleteFirstCache {
                it.metadata.episodeId == episodeId
            }
        },
        backgroundScope.coroutineContext,
    )
}

@Composable
fun SubjectCacheScene(
    vm: SubjectCacheViewModel,
    modifier: Modifier = Modifier,
) {
    SubjectCachePageScaffold(
        title = {
            val title = vm.subjectTitle
            Text(title.orEmpty(), Modifier.placeholder(title == null))
        },
        autoCacheGroup = {
            val navigator = LocalNavigator.current
            AutoCacheGroup(
                onClickGlobalCacheSettings = {
                    navigator.navigateSettings(SettingsTab.MEDIA)
                },
                onClickGlobalCacheManage = {
                    navigator.navigateCaches()
                },
            )
        },
        cacheListGroup = {
            EpisodeCacheListGroup(
                vm.cacheListState,
                mediaSelectorSettingsProvider = {
                    vm.mediaSelectorSettingsFlow
                },
            )
        },
        modifier,
    )
}

/**
 * 条目缓存页面的布局框架
 *
 * @param title 顶部的标题
 * @param autoCacheGroup 自动缓存设置
 * @param cacheListGroup 管理该条目的所有剧集的缓存情况
 */
@Composable
fun SubjectCachePageScaffold(
    title: @Composable RowScope.() -> Unit,
    autoCacheGroup: @Composable SettingsScope.() -> Unit,
    cacheListGroup: @Composable SettingsScope.() -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text("缓存管理")
                },
                navigationIcon = {
                    TopAppBarGoBackButton()
                },
            )
        },
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues)) {
            Surface(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp)) {
                    ProvideTextStyle(MaterialTheme.typography.titleMedium) {
                        title()
                    }
                }
            }

            SettingsTab {
                Spacer(Modifier.fillMaxWidth()) // tab has spacedBy arrangement
                autoCacheGroup()
                cacheListGroup()
                Spacer(Modifier.fillMaxWidth()) // tab has spacedBy arrangement
            }
        }
    }
}
