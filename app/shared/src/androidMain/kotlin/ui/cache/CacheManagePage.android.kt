package me.him188.ani.app.ui.cache

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
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
import me.him188.ani.datasources.api.topic.FileSize.Companion.megaBytes
import me.him188.ani.datasources.api.topic.ResourceLocation
import me.him188.ani.utils.platform.annotations.TestOnly

@OptIn(TestOnly::class)
@Preview
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
    cacheEpisode(1, 2, 1, "翻转孤独"),
    cacheEpisode(1, 3, 2, "明天见"),
    cacheEpisode(1, 4, 3, "火速增员"),
)

@OptIn(DelicateCoroutinesApi::class)
@Suppress("SameParameterValue")
private fun cacheEpisode(
    subjectId: Int,
    episodeId: Int,
    sort: Int,
    displayName: String,
    initialState: CacheEpisodePaused = when (sort % 2) {
        0 -> CacheEpisodePaused.PAUSED
        else -> CacheEpisodePaused.IN_PROGRESS
    },
): CacheEpisodeState {
    val state = mutableStateOf(initialState)
    return CacheEpisodeState(
        subjectId = subjectId,
        episodeId = episodeId,
        sort = EpisodeSort(sort),
        displayName = displayName,
        screenShots = stateOf(emptyList()),
        downloadSpeed = stateOf(233.megaBytes),
        progress = stateOf(0.9999f),
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
        media = previewMediaList[0],
        commonInfo = stateOf(CacheGroupCommonInfo("孤独摇滚")),
        episodes = TestCacheEpisodes,
        downloadSpeed = stateOf(233.megaBytes),
        downloadedSize = stateOf(233.megaBytes),
        uploadSpeed = stateOf(233.megaBytes),
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
