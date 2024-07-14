package me.him188.ani.app.ui.subject.collection

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.content.res.Configuration.UI_MODE_TYPE_NORMAL
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.runBlocking
import me.him188.ani.app.data.models.episode.EpisodeCollection
import me.him188.ani.app.data.models.episode.EpisodeInfo
import me.him188.ani.app.data.models.subject.SelfRatingInfo
import me.him188.ani.app.data.models.subject.SubjectCollection
import me.him188.ani.app.data.models.subject.SubjectInfo
import me.him188.ani.app.data.source.media.EpisodeCacheStatus
import me.him188.ani.app.tools.caching.LazyDataCache
import me.him188.ani.app.tools.caching.mutate
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.rememberViewModel
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.paging.SinglePagePagedSource
import me.him188.ani.datasources.api.topic.FileSize.Companion.megaBytes
import me.him188.ani.datasources.api.topic.UnifiedCollectionType

internal val TestSelfRatingInfo = SelfRatingInfo(
    score = 7,
    comment = "test",
    tags = listOf("My tag"),
    isPrivate = false,
)

@Composable
@Preview
internal actual fun PreviewCollectionPage() {
    ProvideCompositionLocalsForPreview {
        rememberViewModel {
            MyCollectionsViewModelImpl().apply {
                val testData = testCollections()
                runBlocking {
                    collectionsByType.forEach { c ->
                        c.cache.mutate {
                            testData.filter { it.collectionType == c.type }
                        }
                    }
                }
            }
        }

        CollectionPage(
            onClickCaches = {},
            contentPadding = PaddingValues(0.dp),
        )
    }
}

private fun testCollections(): List<SubjectCollection> {
    return buildList {
        var id = 0
        val eps = listOf(
            EpisodeCollection(
                episodeInfo = EpisodeInfo(
                    id = 6385,
                    name = "Diana Houston",
                    nameCn = "Nita O'Donnell",
                    comment = 5931,
                    duration = "",
                    desc = "gubergren",
                    disc = 2272,
                    sort = EpisodeSort(1),
                    ep = EpisodeSort(1),
                ),
                collectionType = UnifiedCollectionType.DONE,
            ),
            EpisodeCollection(
                episodeInfo = EpisodeInfo(
                    id = 6386,
                    name = "Diana Houston",
                    nameCn = "Nita O'Donnell",
                    sort = EpisodeSort(2),
                    comment = 5931,
                    duration = "",
                    desc = "gubergren",
                    disc = 2272,
                    ep = EpisodeSort(2),
                ),
                collectionType = UnifiedCollectionType.DONE,
            ),

            )
        val latestEp = eps[1]
        add(
            testSubjectCollection(eps, UnifiedCollectionType.DOING),
        )
        add(
            testSubjectCollection(eps, UnifiedCollectionType.DOING),
        )
        add(
            testSubjectCollection(eps, UnifiedCollectionType.DOING),
        )
        add(
            testSubjectCollection(eps, collectionType = UnifiedCollectionType.WISH),
        )
        repeat(20) {
            add(
                testSubjectCollection(
                    episodes = eps + EpisodeCollection(
                        episodeInfo = EpisodeInfo(
                            id = 6386,
                            name = "Diana Houston",
                            nameCn = "Nita O'Donnell",
                            sort = EpisodeSort(2),
                            comment = 5931,
                            duration = "",
                            desc = "gubergren",
                            disc = 2272,
                            ep = EpisodeSort(2),
                        ),
                        collectionType = UnifiedCollectionType.DONE,
                    ),
                    collectionType = UnifiedCollectionType.WISH,
                ),
            )
        }
    }
}

private fun testSubjectCollection(
    episodes: List<EpisodeCollection>,
    collectionType: UnifiedCollectionType,
) = SubjectCollection(
    info = SubjectInfo.Empty,
    episodes = episodes,
    collectionType = collectionType,
    selfRatingInfo = TestSelfRatingInfo,
)

@PreviewLightDark
@Composable
private fun PreviewSubjectCollectionsColumnPhone() {
    ProvideCompositionLocalsForPreview {
        SubjectCollectionsColumn(
            testLazyDataCache(),
            onRequestMore = {},
            item = {
                SubjectCollectionItem(
                    item = it,
                    episodeCacheStatus = { _, _ ->
                        EpisodeCacheStatus.Cached(300.megaBytes)
                    },
                    onClick = { },
                    onClickEpisode = {},
                    onClickSelectEpisode = { },
                    onSetCollectionType = {},
                )
            },
            onEmpty = {},
        )
    }
}

@Preview(
    heightDp = 1600, widthDp = 1600,
    uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL,
)
@Preview(
    heightDp = 1600, widthDp = 1600,
)
@Composable
private fun PreviewSubjectCollectionsColumnDesktopLarge() {
    ProvideCompositionLocalsForPreview {
        SubjectCollectionsColumn(
            testLazyDataCache(),
            onRequestMore = {},
            item = {
                SubjectCollectionItem(
                    item = it,
                    episodeCacheStatus = { _, _ ->
                        EpisodeCacheStatus.Cached(300.megaBytes)
                    },
                    onClick = { },
                    onClickEpisode = {},
                    onClickSelectEpisode = { },
                    onSetCollectionType = {},
                )
            },
            onEmpty = {},
        )
    }
}

private fun testLazyDataCache(): LazyDataCache<SubjectCollection> {
    return LazyDataCache(
        {
            SinglePagePagedSource {
                testCollections().asFlow()
            }
        },
        debugName = "test",
    ).apply {
        runBlocking { requestMore() }
    }
}
