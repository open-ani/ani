package me.him188.ani.app.ui.cache

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import me.him188.ani.app.data.media.MediaCacheManager
import me.him188.ani.app.data.media.cache.MediaCache
import me.him188.ani.app.data.media.cache.MediaCacheStorage
import me.him188.ani.app.data.media.cache.MediaStats
import me.him188.ani.app.data.media.cache.TestMediaCache
import me.him188.ani.app.data.media.cache.emptyMediaStats
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.feedback.ErrorMessage
import me.him188.ani.app.ui.subject.episode.mediaFetch.previewMediaList
import me.him188.ani.datasources.api.CachedMedia
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.MediaCacheMetadata
import me.him188.ani.datasources.api.source.MediaFetchRequest
import me.him188.ani.datasources.api.source.MediaSource
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.api.topic.FileSize.Companion.bytes
import me.him188.ani.datasources.api.topic.FileSize.Companion.megaBytes
import me.him188.ani.datasources.api.topic.ResourceLocation

// TIPS: use interactive preview
@Preview
@Composable
fun PreviewCacheManagementPage() {
    ProvideCompositionLocalsForPreview {
        CacheManagementPage(

            vm = remember {
                createTestVM()
            }
        )
    }
}

// TIPS: use interactive preview
@Preview(heightDp = 200)
@Composable
fun PreviewCacheManagementPageScroll() {
    ProvideCompositionLocalsForPreview {
        CacheManagementPage(

            vm = remember {
                createTestVM()
            }
        )
    }
}

private fun createTestVM() = object : CacheManagementPageViewModel {
    override val overallStats: MediaStats get() = emptyMediaStats()
    override val storages: List<MediaCacheStorageState> = listOf(
        MediaCacheStorageState(TestMediaCacheStorage())
    )
    override val accumulatedList: List<MediaCachePresentation> = listOf(
        MediaCachePresentation(testMediaCache1),
        MediaCachePresentation(testMediaCache2)
    )
    override val errorMessage = MutableStateFlow<ErrorMessage?>(null)

    override fun delete(item: MediaCachePresentation) {
    }
}


private class TestMediaCacheStorage : MediaCacheStorage {
    override val mediaSourceId: String
        get() = MediaCacheManager.LOCAL_FS_MEDIA_SOURCE_ID
    override val isEnabled: Flow<Boolean> = flowOf(true)
    override val cacheMediaSource: MediaSource
        get() = throw UnsupportedOperationException()
    override val listFlow: MutableStateFlow<List<MediaCache>> = MutableStateFlow(
        listOf(
            TestMediaCache(
                CachedMedia(
                    previewMediaList[0],
                    MediaCacheManager.LOCAL_FS_MEDIA_SOURCE_ID,
                    ResourceLocation.MagnetLink("magnet:?xt=urn:btih:1"),
                ),
                MediaCacheMetadata(
                    MediaFetchRequest(
                        subjectId = "123123",
                        episodeId = "1231231",
                        subjectNames = emptySet(),
                        episodeSort = EpisodeSort("02"),
                        episodeName = "测试剧集",
                    ),
                ),
                progress = MutableStateFlow(0.3f),
                totalSize = MutableStateFlow(233.megaBytes)
            )
        )
    )
    override val count: Flow<Int> = listFlow.map { it.size }
    override val totalSize: Flow<FileSize> = listFlow.flatMapLatest { caches ->
        combine(caches.map { it.totalSize }) { sizes ->
            sizes.sumOf { it.inBytes }.bytes
        }
    }
    override val stats: MediaStats = emptyMediaStats()
//    override suspend fun findCache(media: Media, metadata: MediaCacheMetadata, resume: Boolean): MediaCache? {
//        return listFlow.first().firstOrNull { it.origin.mediaId == media.mediaId }
//    }

    override suspend fun cache(media: Media, metadata: MediaCacheMetadata, resume: Boolean): MediaCache {
        throw UnsupportedOperationException()
    }

    override suspend fun delete(cache: MediaCache): Boolean {
        if (listFlow.first().any { it == cache }) {
            listFlow.value = listFlow.first().filter { it != cache }
            return true
        }
        return false
    }

    override fun close() {
    }
}

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
    totalSize = MutableStateFlow(233.megaBytes)
)

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
    totalSize = MutableStateFlow(233.megaBytes)
)

@Preview
@Composable
private fun PreviewStorageManagerView() {
    ProvideCompositionLocalsForPreview {
        StorageManagerView(
            listOf(
                MediaCachePresentation(
                    testMediaCache1,
                ),
                MediaCachePresentation(
                    testMediaCache2,
                ),
            ),
            onDelete = {},
        )
    }
}