package me.him188.ani.app.ui.subject.episode.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import me.him188.ani.app.data.models.episode.EpisodeCollection
import me.him188.ani.app.data.models.episode.EpisodeInfo
import me.him188.ani.app.data.models.episode.episode
import me.him188.ani.app.data.source.media.EpisodeCacheStatus
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.rememberBackgroundScope
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.topic.FileSize.Companion.megaBytes
import me.him188.ani.datasources.api.topic.UnifiedCollectionType

val TestEpisodes = buildList {
    repeat(12) { id ->
        add(
            EpisodeInfo(
                id,
                nameCn = "中文剧集名称 $id",
                name = "Episode Name $id",
                sort = EpisodeSort((24 + id).toString()),
                ep = EpisodeSort(id.toString()),
            ),
        )
    }
}

val TestEpisodeCollections = TestEpisodes.map {
    EpisodeCollection(
        it,
        when ((it.ep?.number ?: 0).toInt().rem(3)) {
            0 -> UnifiedCollectionType.DONE
            1 -> UnifiedCollectionType.WISH
            else -> UnifiedCollectionType.DOING
        },
    )
}

// Preview only
val PreviewScope = CoroutineScope(
    CoroutineExceptionHandler { _, _ ->
    },
)

@Composable
@Preview
fun PreviewEpisodeCarousel() = ProvideCompositionLocalsForPreview {
    val backgroundScope = rememberBackgroundScope()
    EpisodeCarousel(
        state = remember {
            EpisodeCarouselState(
                episodes = mutableStateOf(TestEpisodeCollections),
                playingEpisode = mutableStateOf(TestEpisodeCollections[2]),
                cacheStatus = {
                    when ((it.episode.ep?.number ?: 0).toInt().rem(3)) {
                        0 -> EpisodeCacheStatus.Cached(123.megaBytes)
                        1 -> EpisodeCacheStatus.Caching(0.3f, 123.megaBytes)
                        else -> EpisodeCacheStatus.NotCached
                    }
                },
                onSelect = {},
                onChangeCollectionType = { _, _ -> },
                backgroundScope = PreviewScope,
            )
        },
    )
}

@Composable
private fun PreviewEpisodeCarouselItemImpl(
    episode: EpisodeInfo,
    isPlaying: () -> Boolean,
    cacheStatus: () -> EpisodeCacheStatus,
    collectionType: UnifiedCollectionType,
) {
    var collectionTypeState by remember(collectionType) {
        mutableStateOf(collectionType)
    }
    EpisodeCarouselItem(
        episode,
        onClick = {},
        isPlaying = isPlaying,
        cacheStatus = cacheStatus,
        collectionButton = {
            EpisodeCollectionIconButton(
                type = collectionType,
                onChange = { collectionTypeState = it },
            )
        },
    )
}


@Composable
@Preview
fun PreviewEpisodeCarouselItem() = ProvideCompositionLocalsForPreview {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val id = 1
        PreviewEpisodeCarouselItemImpl(
            episode = EpisodeInfo(
                id,
                nameCn = "中文剧集名称 $id",
                name = "Episode Name $id",
                sort = EpisodeSort((24 + id).toString()),
                ep = EpisodeSort(id.toString()),
            ),
            isPlaying = { true },
            cacheStatus = { EpisodeCacheStatus.Cached(123.megaBytes) },
            collectionType = UnifiedCollectionType.DONE,
        )
        PreviewEpisodeCarouselItemImpl(
            episode = EpisodeInfo(
                id,
                nameCn = "中文剧集名称 $id",
                name = "Episode Name $id",
                sort = EpisodeSort((24 + id).toString()),
                ep = EpisodeSort(id.toString()),
            ),
            isPlaying = { true },
            cacheStatus = { EpisodeCacheStatus.Caching(0.3f, 123.megaBytes) },
            collectionType = UnifiedCollectionType.DONE,
        )
        PreviewEpisodeCarouselItemImpl(
            episode = EpisodeInfo(
                id,
                nameCn = "中文剧集名称 $id",
                name = "Episode Name $id",
                sort = EpisodeSort((24 + id).toString()),
                ep = EpisodeSort(id.toString()),
            ),
            isPlaying = { false },
            cacheStatus = { EpisodeCacheStatus.Caching(0.3f, 123.megaBytes) },
            collectionType = UnifiedCollectionType.DONE,
        )
        PreviewEpisodeCarouselItemImpl(
            episode = EpisodeInfo(
                id,
                nameCn = "中文剧集名称中文剧集名称中文剧集名称中文剧集名称 $id",
                name = "Episode Name Episode Name Episode Name Episode Name $id",
                sort = EpisodeSort((24 + id).toString()),
                ep = EpisodeSort(id.toString()),
            ),
            isPlaying = { true },
            cacheStatus = { EpisodeCacheStatus.Cached(123.megaBytes) },
            collectionType = UnifiedCollectionType.DONE,
        )
        PreviewEpisodeCarouselItemImpl(
            episode = EpisodeInfo(
                id,
                nameCn = "中文剧集名称中文剧集名称中文剧集名称中文剧集名称 $id",
                name = "Episode Name Episode Name Episode Name Episode Name $id",
                sort = EpisodeSort((24 + id).toString()),
                ep = EpisodeSort(id.toString()),
            ),
            isPlaying = { false },
            cacheStatus = { EpisodeCacheStatus.Cached(123.megaBytes) },
            collectionType = UnifiedCollectionType.DONE,
        )
    }
}
