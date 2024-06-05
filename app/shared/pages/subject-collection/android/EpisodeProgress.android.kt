package me.him188.ani.app.ui.collection

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.PreviewLightDark
import me.him188.ani.app.data.media.EpisodeCacheStatus
import me.him188.ani.app.data.subject.PackedDate
import me.him188.ani.app.ui.collection.progress.EpisodeProgressDefaults
import me.him188.ani.app.ui.collection.progress.EpisodeProgressDialog
import me.him188.ani.app.ui.collection.progress.EpisodeProgressItem
import me.him188.ani.app.ui.collection.progress.EpisodeProgressRow
import me.him188.ani.app.ui.collection.progress.EpisodeProgressTheme
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.datasources.api.topic.FileSize.Companion.megaBytes
import me.him188.ani.datasources.api.topic.UnifiedCollectionType


private val airDate = PackedDate(2021, 1, 1)

private val testEpisodes = listOf(
    EpisodeProgressItem(
        episodeId = 0,
        episodeSort = "00",
        watchStatus = UnifiedCollectionType.DONE,
        isOnAir = false,
        cacheStatus = EpisodeCacheStatus.Caching(0.3f, 300.megaBytes),
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
        cacheStatus = EpisodeCacheStatus.Cached(300.megaBytes),
    ),
    EpisodeProgressItem(
        episodeId = 3,
        episodeSort = "03",
        watchStatus = UnifiedCollectionType.WISH,
        isOnAir = false,
        cacheStatus = EpisodeCacheStatus.Cached(300.megaBytes),
    ),
    EpisodeProgressItem(
        episodeId = 4,
        episodeSort = "04",
        watchStatus = UnifiedCollectionType.WISH,
        isOnAir = false,
        cacheStatus = EpisodeCacheStatus.Caching(0.7f, 300.megaBytes),
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
        cacheStatus = EpisodeCacheStatus.Cached(300.megaBytes),
    ),
    EpisodeProgressItem(
        episodeId = 8,
        episodeSort = "08",
        watchStatus = UnifiedCollectionType.WISH,
        isOnAir = true,
        cacheStatus = EpisodeCacheStatus.Caching(0.3f, 300.megaBytes),
    ),
)

@PreviewLightDark
@Composable
private fun PreviewEpisodeProgressDialog() {
    ProvideCompositionLocalsForPreview {
        EpisodeProgressDialog(
            onDismissRequest = {},
            title = { Text(text = "葬送的芙莉莲") },
            onClickCache = {},
        ) {
            val episodes = remember {
                testEpisodes
            }
            EpisodeProgressRow(
                episodes = {
                    episodes
                },
                onClickEpisodeState = {},
                onLongClickEpisode = {}
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewEpisodeProgressDialogLightUp() {
    ProvideCompositionLocalsForPreview {
        EpisodeProgressDialog(
            onDismissRequest = {},
            title = { Text(text = "葬送的芙莉莲") },
            onClickCache = {},
        ) {
            val episodes = remember {
                testEpisodes
            }
            EpisodeProgressRow(
                episodes = {
                    episodes
                },
                onClickEpisodeState = {},
                onLongClickEpisode = {},
                colors = EpisodeProgressDefaults.colors(EpisodeProgressTheme.LIGHT_UP)
            )
        }
    }
}

// 特别长需要限制高度并且滚动, #182
@PreviewLightDark
@Composable
private fun PreviewEpisodeProgressDialogVeryLong() {
    ProvideCompositionLocalsForPreview {
        EpisodeProgressDialog(
            onDismissRequest = {},
            title = { Text(text = "银魂") },
            onClickCache = {},
        ) {
            val episodes = remember {
                // 数字太大 preview 会很卡
                (0..70).map { item(it) }
            }
            EpisodeProgressRow(
                episodes = {
                    episodes
                },
                onClickEpisodeState = {},
                onLongClickEpisode = {}
            )
        }
    }
}

private fun item(id: Int) = EpisodeProgressItem(
    episodeId = id,
    episodeSort = id.toString(),
    watchStatus = UnifiedCollectionType.WISH,
    isOnAir = true,
    cacheStatus = EpisodeCacheStatus.Caching(0.3f, 300.megaBytes),
)
