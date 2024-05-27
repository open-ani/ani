package me.him188.ani.app.pages.cache.manage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.flow.MutableStateFlow
import me.him188.ani.app.data.media.MediaCacheManager
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.subject.episode.mediaFetch.previewMediaList
import me.him188.ani.datasources.api.CachedMedia
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.MediaCacheMetadata
import me.him188.ani.datasources.api.source.MediaFetchRequest
import me.him188.ani.datasources.api.topic.FileSize.Companion.megaBytes
import me.him188.ani.datasources.api.topic.ResourceLocation

// See interactive preview
@Preview
@Composable
private fun PreviewCacheItemDownloading() = ProvideCompositionLocalsForPreview {
    val media = remember {
        MediaCachePresentation(
            testData(0.3f)
        )
    }
    CacheItemView(item = media, onDelete = {}, mediaSourceId = { "本地" })
}

// See interactive preview
@Preview
@Composable
private fun PreviewCacheItemUploading() = ProvideCompositionLocalsForPreview {
    val media = remember {
        MediaCachePresentation(
            testData(1f)
        )
    }
    CacheItemView(item = media, onDelete = {}, mediaSourceId = { "本地" })
}

private fun testData(progress: Float) = TestMediaCache(
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
    progress = MutableStateFlow(progress),
    totalSize = MutableStateFlow(233.megaBytes)
)
