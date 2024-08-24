package me.him188.ani.app.ui.cache

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.PreviewLightDark
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import me.him188.ani.app.data.source.media.TestMediaList
import me.him188.ani.app.data.source.media.cache.MediaCacheManager
import me.him188.ani.app.data.source.media.cache.MediaStats
import me.him188.ani.app.data.source.media.cache.TestMediaCache
import me.him188.ani.app.data.source.media.cache.emptyMediaStats
import me.him188.ani.app.ui.cache.components.CacheEpisodePaused
import me.him188.ani.app.ui.cache.components.CacheEpisodeState
import me.him188.ani.app.ui.cache.components.CacheGroupCommonInfo
import me.him188.ani.app.ui.cache.components.CacheGroupState
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.stateOf
import me.him188.ani.app.ui.subject.episode.mediaFetch.previewMediaList
import me.him188.ani.datasources.api.CachedMedia
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.MediaCacheMetadata
import me.him188.ani.datasources.api.source.MediaFetchRequest
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.api.topic.FileSize.Companion.megaBytes
import me.him188.ani.datasources.api.topic.ResourceLocation
import me.him188.ani.utils.platform.annotations.TestOnly

@OptIn(TestOnly::class)
@PreviewLightDark
@Composable
fun PreviewCacheManagementPage() {
    ProvideCompositionLocalsForPreview {
        CacheManagementPage(
            state = CacheManagementState(stateOf(TestCacheGroupSates)),
            rememberTestMediaStats(),
        )
    }
}

@Composable
fun rememberTestMediaStats(): MediaStats {
    return remember { emptyMediaStats() }
}

@TestOnly
internal val TestCacheEpisodes = listOf(
    createTestCacheEpisode(1, "翻转孤独", 1),
    createTestCacheEpisode(2, "明天见", 1),
    createTestCacheEpisode(3, "火速增员", 1),
)

@OptIn(DelicateCoroutinesApi::class)
@Suppress("SameParameterValue")
@TestOnly
internal fun createTestCacheEpisode(
    sort: Int,
    displayName: String = "第 $sort 话",
    subjectId: Int = 1,
    episodeId: Int = sort,
    initialState: CacheEpisodePaused = when (sort % 2) {
        0 -> CacheEpisodePaused.PAUSED
        else -> CacheEpisodePaused.IN_PROGRESS
    },
    downloadSpeed: FileSize = 233.megaBytes,
    progress: Float? = 0.3f,
    totalSize: FileSize = 888.megaBytes,
): CacheEpisodeState {
    val state = mutableStateOf(initialState)
    return CacheEpisodeState(
        subjectId = subjectId,
        episodeId = episodeId,
        cacheId = "1",
        sort = EpisodeSort(sort),
        displayName = displayName,
        screenShots = stateOf(emptyList()),
        stats = stateOf(
            CacheEpisodeState.Stats(
                downloadSpeed = downloadSpeed,
                progress = progress,
                totalSize = totalSize,
            ),
        ),
        state = state,
        onPause = { state.value = CacheEpisodePaused.PAUSED },
        onResume = { state.value = CacheEpisodePaused.IN_PROGRESS },
        onDelete = {},
        onPlay = {},
        backgroundScope = GlobalScope,
    )
}

@TestOnly
internal val TestCacheGroupSates = listOf(
    CacheGroupState(
        media = TestMediaList[0],
        commonInfo = stateOf(
            CacheGroupCommonInfo(
                subjectId = 1,
                "孤独摇滚",
                mediaSourceId = "mikan-mikanime-tv",
                allianceName = "某某字幕组",
            ),
        ),
        episodes = TestCacheEpisodes,
        stats = stateOf(
            CacheGroupState.Stats(
                downloadSpeed = 233.megaBytes,
                downloadedSize = 233.megaBytes,
                uploadSpeed = 233.megaBytes,
            ),
        ),
    ),
)

@TestOnly
internal val testMediaCache1 = TestMediaCache(
    CachedMedia(
        previewMediaList[0],
        MediaCacheManager.LOCAL_FS_MEDIA_SOURCE_ID,
        ResourceLocation.MagnetLink("magnet:?xt=urn:btih:1"),
    ),
    MediaCacheMetadata(
        MediaFetchRequest(
            subjectId = "123123",
            episodeId = "1231231",
            subjectNames = setOf("夜晚的水母不会游泳"),
            episodeSort = EpisodeSort("02"),
            episodeName = "测试剧集",
        ),
    ),
    progress = MutableStateFlow(0.9999f),
    totalSize = MutableStateFlow(233.megaBytes),
)

@TestOnly
internal val testMediaCache2 = TestMediaCache(
    CachedMedia(
        previewMediaList[1],
        MediaCacheManager.LOCAL_FS_MEDIA_SOURCE_ID,
        ResourceLocation.MagnetLink("magnet:?xt=urn:btih:1"),
    ),
    MediaCacheMetadata(
        MediaFetchRequest(
            subjectId = "123123",
            episodeId = "1231231",
            subjectNames = setOf("夜晚的水母不会游泳"),
            episodeSort = EpisodeSort("03"),
            episodeName = "测试剧集2",
        ),
    ),
    progress = MutableStateFlow(1.0f),
    totalSize = MutableStateFlow(233.megaBytes),
)
