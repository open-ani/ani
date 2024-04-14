package me.him188.ani.app.ui.collection

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.PreviewLightDark
import kotlinx.coroutines.flow.flowOf
import me.him188.ani.app.data.media.EpisodeCacheStatus
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.datasources.api.topic.FileSize.Companion.megaBytes
import me.him188.ani.datasources.api.topic.UnifiedCollectionType


@PreviewLightDark
@Composable
private fun PreviewEpisodeProgressDialog() {
    ProvideCompositionLocalsForPreview {
        EpisodeProgressDialog(
            onDismissRequest = {},
            onClickDetails = {},
            title = { Text(text = "葬送的芙莉莲") },
            onClickCache = {},
        ) {
            EpisodeProgressRow(
                episodes = remember {
                    listOf(
                        EpisodeProgressItem(
                            episodeId = 0,
                            episodeSort = "00",
                            watchStatus = UnifiedCollectionType.DONE,
                            isOnAir = false,
                            cacheStatus = EpisodeCacheStatus.Caching,
                        ),
                        EpisodeProgressItem(
                            episodeId = 1,
                            episodeSort = "01",
                            watchStatus = UnifiedCollectionType.DONE,
                            isOnAir = false,
                            cacheStatus = EpisodeCacheStatus.NotCached,
                        ),
                        EpisodeProgressItem(
                            episodeId = 2,
                            episodeSort = "02",
                            watchStatus = UnifiedCollectionType.DONE,
                            isOnAir = false,
                            cacheStatus = EpisodeCacheStatus.Cached(flowOf(300.megaBytes)),
                        ),
                        EpisodeProgressItem(
                            episodeId = 3,
                            episodeSort = "03",
                            watchStatus = UnifiedCollectionType.WISH,
                            isOnAir = false,
                            cacheStatus = EpisodeCacheStatus.Cached(flowOf(300.megaBytes)),
                        ),
                        EpisodeProgressItem(
                            episodeId = 4,
                            episodeSort = "04",
                            watchStatus = UnifiedCollectionType.WISH,
                            isOnAir = false,
                            cacheStatus = EpisodeCacheStatus.Caching,
                        ),
                        EpisodeProgressItem(
                            episodeId = 5,
                            episodeSort = "05",
                            watchStatus = UnifiedCollectionType.WISH,
                            isOnAir = false,
                            cacheStatus = EpisodeCacheStatus.NotCached,
                        ),
                        EpisodeProgressItem(
                            episodeId = 6,
                            episodeSort = "06",
                            watchStatus = UnifiedCollectionType.WISH,
                            isOnAir = true,
                            cacheStatus = EpisodeCacheStatus.NotCached,
                        ),
                        EpisodeProgressItem(
                            episodeId = 7,
                            episodeSort = "07",
                            watchStatus = UnifiedCollectionType.WISH,
                            isOnAir = true,
                            cacheStatus = EpisodeCacheStatus.Cached(flowOf(300.megaBytes)),
                        ),
                        EpisodeProgressItem(
                            episodeId = 8,
                            episodeSort = "08",
                            watchStatus = UnifiedCollectionType.WISH,
                            isOnAir = true,
                            cacheStatus = EpisodeCacheStatus.Caching,
                        ),
                    )
                },
                onClickEpisodeState = {},
                onLongClickEpisode = {}
            )
        }
    }
}
