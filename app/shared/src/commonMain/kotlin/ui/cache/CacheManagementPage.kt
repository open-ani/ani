package me.him188.ani.app.ui.cache

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.plus
import kotlinx.coroutines.supervisorScope
import me.him188.ani.app.data.models.subject.SubjectManager
import me.him188.ani.app.data.models.subject.subjectInfoFlow
import me.him188.ani.app.data.source.media.cache.MediaCache
import me.him188.ani.app.data.source.media.cache.MediaCacheManager
import me.him188.ani.app.data.source.media.cache.MediaCacheState
import me.him188.ani.app.data.source.media.cache.MediaStats
import me.him188.ani.app.data.source.media.cache.downloadedSize
import me.him188.ani.app.data.source.media.cache.emptyMediaStats
import me.him188.ani.app.data.source.media.cache.sampled
import me.him188.ani.app.data.source.media.cache.sum
import me.him188.ani.app.navigation.AniNavigator
import me.him188.ani.app.ui.cache.components.CacheEpisodePaused
import me.him188.ani.app.ui.cache.components.CacheEpisodeState
import me.him188.ani.app.ui.cache.components.CacheGroupCard
import me.him188.ani.app.ui.cache.components.CacheGroupCardDefaults
import me.him188.ani.app.ui.cache.components.CacheGroupCardLayoutProperties
import me.him188.ani.app.ui.cache.components.CacheGroupCommonInfo
import me.him188.ani.app.ui.cache.components.CacheGroupState
import me.him188.ani.app.ui.cache.components.CacheManagementOverallStats
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.ifThen
import me.him188.ani.app.ui.foundation.layout.isShowLandscapeUI
import me.him188.ani.app.ui.foundation.produceState
import me.him188.ani.app.ui.foundation.stateOf
import me.him188.ani.app.ui.foundation.widgets.TopAppBarGoBackButton
import me.him188.ani.datasources.api.episodeIdInt
import me.him188.ani.datasources.api.subjectIdInt
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.api.unwrapCached
import me.him188.ani.utils.coroutines.sampleWithInitial
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.seconds

// 为了未来万一要改, 方便
typealias CacheGroupGridLayoutState = LazyStaggeredGridState

@Stable
class CacheManagementViewModel(
    private val navigator: AniNavigator,
) : AbstractViewModel(), KoinComponent {
    private val cacheManager: MediaCacheManager by inject()
    private val subjectManager: SubjectManager by inject()

    val overallStats: MediaStats by cacheManager.enabledStorages.map { list ->
        list.map { it.stats }.sum()
    }.map {
        it.sampled()
    }.produceState(emptyMediaStats())
    
    val lazyGridState: CacheGroupGridLayoutState = LazyStaggeredGridState()

    val state: CacheManagementState = CacheManagementState(
        cacheManager.enabledStorages.flatMapLatest { storages ->
            combine(storages.map { it.listFlow }) { it.asSequence().flatten().toList() }
                .transformLatest { allCaches ->
                    supervisorScope {
                        emitAll(flowOf(createCacheGroupStates(allCaches)))
                    } // supervisorScope won't finish itself
                }
        }.produceState(emptyList()),
    )

    private fun CoroutineScope.createCacheGroupStates(allCaches: List<MediaCache>) =
        allCaches.groupBy { it.origin.unwrapCached().mediaId }.map { (_, episodes) ->
            check(episodes.isNotEmpty())

            val firstCache = episodes.first()
            CacheGroupState(
                media = firstCache.origin.unwrapCached(),
                commonInfo = subjectManager.subjectInfoFlow(firstCache.metadata.subjectIdInt) // 既会查缓存, 也会查网络, 基本上不会有查不到的情况
                    .map {
                        createGroupCommonInfo(
                            subjectId = it.id,
                            firstCache = firstCache,
                            subjectDisplayName = it.displayName,
                            imageUrl = it.imageLarge,
                        )
                    }
                    .catch {
                        emit(
                            createGroupCommonInfo(
                                subjectId = firstCache.metadata.subjectIdInt,
                                firstCache = firstCache,
                                subjectDisplayName = firstCache.metadata.subjectNameCN
                                    ?: firstCache.metadata.subjectNames.firstOrNull()
                                    ?: firstCache.origin.originalTitle,
                                imageUrl = null,
                            ),
                        )
                    }
                    .produceState(null, this),
                episodes = episodes.map { mediaCache ->
                    createCacheEpisode(mediaCache)
                },
                stats = combine(
                    firstCache.sessionDownloadSpeed,
                    firstCache.downloadedSize,
                    firstCache.sessionUploadSpeed,
                ) { downloadSpeed, downloadedSize, uploadSpeed ->
                    CacheGroupState.Stats(
                        downloadSpeed = downloadSpeed,
                        downloadedSize = downloadedSize,
                        uploadSpeed = uploadSpeed,
                    )
                }.sampleWithInitial(1.seconds)
                    .produceState(
                        CacheGroupState.Stats(FileSize.Unspecified, FileSize.Unspecified, FileSize.Unspecified),
                        this,
                    ),
            )
        }

    private fun createGroupCommonInfo(
        subjectId: Int,
        firstCache: MediaCache,
        subjectDisplayName: String,
        imageUrl: String?,
    ) = CacheGroupCommonInfo(
        subjectId = subjectId,
        subjectDisplayName,
        mediaSourceId = firstCache.origin.unwrapCached().mediaSourceId,
        allianceName = firstCache.origin.unwrapCached().properties.alliance,
        imageUrl = imageUrl,
    )

    private fun CoroutineScope.createCacheEpisode(mediaCache: MediaCache) =
        CacheEpisodeState(
            subjectId = mediaCache.metadata.subjectIdInt,
            episodeId = mediaCache.metadata.episodeIdInt,
            cacheId = mediaCache.cacheId,
            sort = mediaCache.metadata.episodeSort,
            displayName = mediaCache.metadata.episodeName,
//            screenShots = flow {
//                if (mediaCache is TorrentMediaCacheEngine.TorrentMediaCache) {
//                    mediaCache.lazyFileHandle
//                }
//                    episodeScreenshotRepository.getScreenshots(mediaCache.metadata.)
//            }.produceState(emptyList(), this),
            screenShots = stateOf(emptyList()),
            stats = combine(
                mediaCache.downloadSpeed,
                mediaCache.progress,
                mediaCache.totalSize,
            ) { downloadSpeed, progress, totalSize ->
                CacheEpisodeState.Stats(
                    downloadSpeed = downloadSpeed,
                    progress = progress,
                    totalSize = totalSize,
                )
            }.sampleWithInitial(1.seconds)
                .produceState(CacheEpisodeState.Stats(FileSize.Unspecified, null, FileSize.Unspecified), this),
            state = mediaCache.state.map {
                when (it) {
                    MediaCacheState.IN_PROGRESS -> CacheEpisodePaused.IN_PROGRESS
                    MediaCacheState.PAUSED -> CacheEpisodePaused.PAUSED
                }
            }.produceState(CacheEpisodePaused.IN_PROGRESS, this),
            onPause = { mediaCache.pause() },
            onResume = { mediaCache.resume() },
            onDelete = { cacheManager.deleteCache(mediaCache) },
            onPlay = {
                navigator.navigateEpisodeDetails(
                    mediaCache.metadata.subjectIdInt,
                    mediaCache.metadata.episodeIdInt,
                )
            },
            backgroundScope = this + CoroutineName("CacheEpisode-${mediaCache.metadata.episodeIdInt}"),
        )
}

/**
 * 全局缓存管理页面状态
 */
@Stable
class CacheManagementState(
    groups: State<List<CacheGroupState>>,
) {
    val groups by groups
}

/**
 * 全局缓存管理页面
 */
@Composable
fun CacheManagementPage(
    vm: CacheManagementViewModel,
    modifier: Modifier = Modifier,
    showBack: Boolean = !isShowLandscapeUI(),
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
) {
    CacheManagementPage(
        vm.state,
        vm.overallStats,
        modifier = modifier,
        lazyGridState = vm.lazyGridState,
        showBack = showBack,
        contentWindowInsets = contentWindowInsets,
    )
}


@Composable
fun CacheManagementPage(
    state: CacheManagementState,
    overallStats: MediaStats,
    modifier: Modifier = Modifier,
    lazyGridState: CacheGroupGridLayoutState = rememberLazyStaggeredGridState(),
    showBack: Boolean = !isShowLandscapeUI(),
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
) {
    Scaffold(
        modifier,
        topBar = {
            TopAppBar(
                title = { Text("缓存管理") },
                navigationIcon = {
                    if (showBack) {
                        TopAppBarGoBackButton()
                    }
                },
            )
        },
        contentWindowInsets = contentWindowInsets,
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues)) {
            Surface(
                Modifier.ifThen(lazyGridState.canScrollBackward) {
                    shadow(2.dp, clip = false)
                },
            ) {
                CacheManagementOverallStats(
                    overallStats,
                    Modifier
                        .padding(horizontal = 16.dp).padding(bottom = 16.dp)
                        .fillMaxWidth(),
                )
            }

            CacheGroupColumn(
                state,
                lazyGridState = lazyGridState,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            )
        }
    }
}

@Composable
fun CacheGroupColumn(
    state: CacheManagementState,
    modifier: Modifier = Modifier,
    lazyGridState: CacheGroupGridLayoutState = rememberLazyStaggeredGridState(),
    layoutProperties: CacheGroupCardLayoutProperties = CacheGroupCardDefaults.LayoutProperties,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    LazyVerticalStaggeredGrid(
        StaggeredGridCells.Adaptive(300.dp),
        modifier,
        state = lazyGridState,
        verticalItemSpacing = 20.dp,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = contentPadding,
    ) {
        items(state.groups, key = { it.media.mediaId }) { group ->
            CacheGroupCard(
                group,
                Modifier, // 动画很怪, 等 1.7.0 的 animateItem 再看看
                layoutProperties,
            )
        }
    }
}
