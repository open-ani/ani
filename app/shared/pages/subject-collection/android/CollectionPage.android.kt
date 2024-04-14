package me.him188.ani.app.ui.collection

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import me.him188.ani.app.data.media.EpisodeCacheStatus
import me.him188.ani.app.tools.caching.LazyDataCache
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.rememberViewModel
import me.him188.ani.datasources.api.paging.SingleShotPagedSource
import me.him188.ani.datasources.api.topic.FileSize.Companion.megaBytes
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
                        EpisodeCacheStatus.Cached(flowOf(300.megaBytes))
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