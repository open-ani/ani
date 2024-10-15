/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.cache

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.plus
import kotlinx.coroutines.supervisorScope
import me.him188.ani.app.data.models.subject.SubjectManager
import me.him188.ani.app.data.models.subject.subjectInfoFlow
import me.him188.ani.app.domain.media.cache.MediaCacheManager
import me.him188.ani.app.domain.media.cache.engine.MediaStats
import me.him188.ani.app.domain.media.cache.engine.sum
import me.him188.ani.app.navigation.AniNavigator
import me.him188.ani.app.torrent.api.files.averageRate
import me.him188.ani.app.ui.adaptive.AniTopAppBar
import me.him188.ani.app.ui.adaptive.AniTopAppBarDefaults
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
import me.him188.ani.app.ui.foundation.produceState
import me.him188.ani.app.ui.foundation.stateOf
import me.him188.ani.app.ui.foundation.theme.AniThemeDefaults
import me.him188.ani.app.ui.foundation.widgets.TopAppBarGoBackButton
import me.him188.ani.datasources.api.creationTimeOrNull
import me.him188.ani.datasources.api.episodeIdInt
import me.him188.ani.datasources.api.subjectIdInt
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.api.topic.FileSize.Companion.bytes
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

    val lazyGridState: CacheGroupGridLayoutState = LazyStaggeredGridState()

    val state: CacheManagementState = CacheManagementState(
        overallStats = cacheManager.enabledStorages.flatMapLatest { list ->
            list.map { it.stats }.sum()
        }.sampleWithInitial(1.seconds).produceState(MediaStats.Unspecified),
        groups = cacheManager.enabledStorages.flatMapLatest { storages ->
            combine(storages.map { it.listFlow }) { it.asSequence().flatten().toList() }
                .transformLatest { allCaches ->
                    supervisorScope {
                        emitAll(flowOf(createCacheGroupStates(allCaches)))
                    } // supervisorScope won't finish itself
                }
        }.produceState(emptyList()),
    )

    private fun CoroutineScope.createCacheGroupStates(allCaches: List<_root_ide_package_.me.him188.ani.app.domain.media.cache.MediaCache>) =
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
                stats = firstCache.sessionStats.combine(
                    firstCache.sessionStats.map { it.downloadedBytes.inBytes }.averageRate(),
                ) { stats, downloadSpeed ->
                    CacheGroupState.Stats(
                        downloadSpeed = downloadSpeed.bytes,
                        downloadedSize = stats.downloadedBytes,
                        uploadSpeed = stats.uploadSpeed,
                    )
                }.sampleWithInitial(1.seconds)
                    .produceState(
                        CacheGroupState.Stats(FileSize.Unspecified, FileSize.Unspecified, FileSize.Unspecified),
                        this,
                    ),
            )
        }.sortedWith(
            compareByDescending<CacheGroupState> { it.latestCreationTime }
                .thenByDescending { it.cacheId }, // 只有旧的缓存会没有时间, 才会走这个
        )

    private fun createGroupCommonInfo(
        subjectId: Int,
        firstCache: _root_ide_package_.me.him188.ani.app.domain.media.cache.MediaCache,
        subjectDisplayName: String,
        imageUrl: String?,
    ) = CacheGroupCommonInfo(
        subjectId = subjectId,
        subjectDisplayName,
        mediaSourceId = firstCache.origin.unwrapCached().mediaSourceId,
        allianceName = firstCache.origin.unwrapCached().properties.alliance,
        imageUrl = imageUrl,
    )

    private fun CoroutineScope.createCacheEpisode(mediaCache: _root_ide_package_.me.him188.ani.app.domain.media.cache.MediaCache): CacheEpisodeState {
        val fileStats = mediaCache.fileStats
            .shareIn(this, started = SharingStarted.Eagerly, replay = 1)
        return CacheEpisodeState(
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
            creationTime = mediaCache.metadata.creationTimeOrNull,
            screenShots = stateOf(emptyList()),
            stats = fileStats.combine(
                fileStats.map { it.downloadedBytes.inBytes }.averageRate(),
            ) { stats, downloadSpeed ->
                CacheEpisodeState.Stats(
                    downloadSpeed = downloadSpeed.bytes,
                    progress = stats.downloadProgress,
                    totalSize = stats.totalSize,
                )
            }.sampleWithInitial(1.seconds)
                .produceState(CacheEpisodeState.Stats.Unspecified, this),
            state = mediaCache.state.map {
                when (it) {
                    _root_ide_package_.me.him188.ani.app.domain.media.cache.MediaCacheState.IN_PROGRESS -> CacheEpisodePaused.IN_PROGRESS
                    _root_ide_package_.me.him188.ani.app.domain.media.cache.MediaCacheState.PAUSED -> CacheEpisodePaused.PAUSED
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
}

/**
 * 全局缓存管理页面状态
 */
@Stable
class CacheManagementState(
    overallStats: State<MediaStats>,
    groups: State<List<CacheGroupState>>,
) {
    val overallStats by overallStats
    val groups by groups
}

/**
 * 全局缓存管理页面
 */
@Composable
fun CacheManagementPage(
    vm: CacheManagementViewModel,
    showBack: Boolean,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
) {
    CacheManagementPage(
        vm.state,
        showBack = showBack,
        modifier = modifier,
        lazyGridState = vm.lazyGridState,
        windowInsets = windowInsets,
    )
}


@Composable
fun CacheManagementPage(
    state: CacheManagementState,
    showBack: Boolean,
    modifier: Modifier = Modifier,
    lazyGridState: CacheGroupGridLayoutState = rememberLazyStaggeredGridState(),
    windowInsets: WindowInsets = WindowInsets.systemBars,
) {
    val appBarColors = AniThemeDefaults.topAppBarColors()
    Scaffold(
        modifier,
        topBar = {
            AniTopAppBar(
                title = { AniTopAppBarDefaults.Title("缓存管理") },
                windowInsets = windowInsets.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
                navigationIcon = {
                    if (showBack) {
                        TopAppBarGoBackButton()
                    }
                },
                colors = appBarColors,
            )
        },
        containerColor = Color.Unspecified,
        contentWindowInsets = windowInsets.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues)) {
            Surface(
                Modifier.ifThen(lazyGridState.canScrollBackward) {
                    shadow(2.dp, clip = false)
                },
                color = appBarColors.containerColor,
                contentColor = contentColorFor(appBarColors.containerColor),
            ) {
                CacheManagementOverallStats(
                    { state.overallStats },
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
        StaggeredGridCells.Adaptive(320.dp),
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
