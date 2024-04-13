package me.him188.ani.app.ui.collection

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.runBlocking
import me.him188.ani.app.data.media.EpisodeCacheStatus
import me.him188.ani.app.tools.caching.LazyDataCache
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.rememberViewModel
import me.him188.ani.datasources.api.paging.SingleShotPagedSource
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import org.openapitools.client.models.Episode
import org.openapitools.client.models.EpisodeCollectionType
import org.openapitools.client.models.SubjectCollectionType
import org.openapitools.client.models.UserEpisodeCollection
import java.math.BigDecimal

@Composable
@Preview
internal actual fun PreviewCollectionPage() {
    ProvideCompositionLocalsForPreview {
        rememberViewModel {
            MyCollectionsViewModelImpl().apply {
                val testData = testCollections()
                runBlocking {
                    collectionsByType.forEach { (type, cache) ->
                        cache.mutate {
                            testData.filter { it.collectionType == type }
                        }
                    }
                }
            }
        }

        CollectionPage(
            onClickCaches = {},
            contentPadding = PaddingValues(0.dp)
        )
    }
}

@Suppress("UNUSED_CHANGED_VALUE")
private fun testCollections(): List<SubjectCollectionItem> {
    return buildList {
        var id = 0
        val eps = listOf(
            UserEpisodeCollection(
                episode = Episode(
                    id = 6385,
                    type = 5956,
                    name = "Diana Houston",
                    nameCn = "Nita O'Donnell",
                    sort = BigDecimal.ONE,
                    airdate = "phasellus",
                    comment = 5931,
                    duration = "",
                    desc = "gubergren",
                    disc = 2272,
                    ep = BigDecimal.ONE,
                    durationSeconds = null
                ),
                type = EpisodeCollectionType.WATCHED
            ),
            UserEpisodeCollection(
                episode = Episode(
                    id = 6386,
                    type = 5956,
                    name = "Diana Houston",
                    nameCn = "Nita O'Donnell",
                    sort = BigDecimal.valueOf(2),
                    airdate = "phasellus",
                    comment = 5931,
                    duration = "",
                    desc = "gubergren",
                    disc = 2272,
                    ep = BigDecimal.valueOf(2),
                    durationSeconds = null
                ),
                type = EpisodeCollectionType.WATCHED
            )

        )
        val latestEp = eps[1]
        add(
            SubjectCollectionItem(
                subjectId = id++,
                displayName = "葬送的芙莉莲",
                image = "",
                rate = null,
                date = "2023 年 10 月",
                totalEps = 2,
                isOnAir = true,
                latestEp = latestEp,
                lastWatchedEpIndex = null,
                episodes = eps,
                collectionType = SubjectCollectionType.Doing
            )
        )
        add(
            SubjectCollectionItem(
                subjectId = id++,
                displayName = "葬送的芙莉莲 2",
                image = "",
                rate = null,
                date = "2023 年 10 月",
                totalEps = 2,
                isOnAir = true,
                latestEp = latestEp,
                lastWatchedEpIndex = 0,
                episodes = eps,
                collectionType = SubjectCollectionType.Doing
            )
        )
        add(
            SubjectCollectionItem(
                subjectId = id++,
                displayName = "葬送的芙莉莲 3",
                image = "",
                rate = null,
                date = "2023 年 10 月",
                totalEps = 2,
                isOnAir = true,
                latestEp = latestEp,
                lastWatchedEpIndex = 1,
                episodes = eps,
                collectionType = SubjectCollectionType.Doing
            )
        )
        add(
            SubjectCollectionItem(
                subjectId = id++,
                displayName = "葬送的芙莉莲 4",
                image = "",
                rate = null,
                date = "2023 年 10 月",
                totalEps = 2,
                isOnAir = true,
                latestEp = latestEp,
                lastWatchedEpIndex = 1,
                episodes = eps,
                collectionType = SubjectCollectionType.Wish
            )
        )
    }
}

@PreviewLightDark
@Composable
private fun PreviewEpisodeProgressDialog() {
    ProvideCompositionLocalsForPreview {
        EpisodeProgressDialog(
            onDismissRequest = {},
            onClickDetails = {},
            title = { Text(text = "葬送的芙莉莲") },
        ) {
            EpisodeProgressRow(
                episodes = remember {
                    listOf(
                        EpisodeProgressItem(
                            episodeId = 0,
                            episodeSort = "00",
                            watchStatus = UnifiedCollectionType.DONE,
                            isOnAir = false,
                            cacheStatus = EpisodeCacheStatus.CACHING,
                        ),
                        EpisodeProgressItem(
                            episodeId = 1,
                            episodeSort = "01",
                            watchStatus = UnifiedCollectionType.DONE,
                            isOnAir = false,
                            cacheStatus = EpisodeCacheStatus.NOT_CACHED,
                        ),
                        EpisodeProgressItem(
                            episodeId = 2,
                            episodeSort = "02",
                            watchStatus = UnifiedCollectionType.DONE,
                            isOnAir = false,
                            cacheStatus = EpisodeCacheStatus.CACHED,
                        ),
                        EpisodeProgressItem(
                            episodeId = 3,
                            episodeSort = "03",
                            watchStatus = UnifiedCollectionType.WISH,
                            isOnAir = false,
                            cacheStatus = EpisodeCacheStatus.CACHED,
                        ),
                        EpisodeProgressItem(
                            episodeId = 4,
                            episodeSort = "04",
                            watchStatus = UnifiedCollectionType.WISH,
                            isOnAir = false,
                            cacheStatus = EpisodeCacheStatus.CACHING,
                        ),
                        EpisodeProgressItem(
                            episodeId = 5,
                            episodeSort = "05",
                            watchStatus = UnifiedCollectionType.WISH,
                            isOnAir = false,
                            cacheStatus = EpisodeCacheStatus.NOT_CACHED,
                        ),
                        EpisodeProgressItem(
                            episodeId = 6,
                            episodeSort = "06",
                            watchStatus = UnifiedCollectionType.WISH,
                            isOnAir = true,
                            cacheStatus = EpisodeCacheStatus.NOT_CACHED,
                        ),
                        EpisodeProgressItem(
                            episodeId = 7,
                            episodeSort = "07",
                            watchStatus = UnifiedCollectionType.WISH,
                            isOnAir = true,
                            cacheStatus = EpisodeCacheStatus.CACHED,
                        ),
                        EpisodeProgressItem(
                            episodeId = 8,
                            episodeSort = "08",
                            watchStatus = UnifiedCollectionType.WISH,
                            isOnAir = true,
                            cacheStatus = EpisodeCacheStatus.CACHING,
                        ),
                    )
                },
                onClickEpisodeState = {},
                onLongClickEpisode = {}
            )
        }
    }
}

@Preview
@Composable
private fun PreviewSubjectCollectionsColumn() {
    ProvideCompositionLocalsForPreview {
        SubjectCollectionsColumn(
            LazyDataCache(
                SingleShotPagedSource {
                    testCollections().asFlow()
                },
                "test"
            ),
            item = {
                SubjectCollectionItem(
                    item = it,
                    episodeCacheStatus = { _, _ ->
                        EpisodeCacheStatus.CACHED
                    },
                    onClick = { },
                    onClickEpisode = {},
                    onClickSelectEpisode = { },
                    onSetAllEpisodesDone = { },
                    onSetCollectionType = {}
                )
            },
            onEmpty = {}
        )
    }
}