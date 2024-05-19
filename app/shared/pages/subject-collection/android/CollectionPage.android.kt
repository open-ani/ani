package me.him188.ani.app.ui.collection

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.runBlocking
import me.him188.ani.app.data.media.EpisodeCacheStatus
import me.him188.ani.app.data.subject.SubjectCollectionItem
import me.him188.ani.app.data.subject.SubjectInfo
import me.him188.ani.app.tools.caching.LazyDataCache
import me.him188.ani.app.tools.caching.mutate
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.rememberViewModel
import me.him188.ani.datasources.api.paging.SinglePagePagedSource
import me.him188.ani.datasources.api.topic.FileSize.Companion.megaBytes
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import org.openapitools.client.models.Collection
import org.openapitools.client.models.Count
import org.openapitools.client.models.Episode
import org.openapitools.client.models.EpisodeCollectionType
import org.openapitools.client.models.Images
import org.openapitools.client.models.Rating
import org.openapitools.client.models.Subject
import org.openapitools.client.models.SubjectType
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
            contentPadding = PaddingValues(0.dp)
        )
    }
}

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
                subjectId = ++id,
                displayName = "葬送的芙莉莲",
                image = "",
                rate = null,
                date = "2023 年 10 月",
                totalEps = 2,
                _episodes = eps,
                collectionType = UnifiedCollectionType.DOING,
                info = SubjectInfo.Empty,
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
                _episodes = eps,
                collectionType = UnifiedCollectionType.DOING,
                info = SubjectInfo.Empty,
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
                _episodes = eps,
                collectionType = UnifiedCollectionType.DOING,
                info = SubjectInfo.Empty,
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
                _episodes = eps,
                collectionType = UnifiedCollectionType.WISH,
                info = SubjectInfo.Empty,
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
                {
                    SinglePagePagedSource {
                        testCollections().asFlow()
                    }
                },
                debugName = "test"
            ),
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
                    onSetAllEpisodesDone = { },
                    onSetCollectionType = {}
                )
            },
            onEmpty = {}
        )
    }
}

fun testSubject(
    id: Int = 0,
): Subject {
    return Subject(
        id = id,
        type = SubjectType.Music,
        name = "Doreen Vaughn",
        nameCn = "Lena Cortez",
        summary = "feugiat",
        nsfw = false,
        locked = false,
        platform = "himenaeos",
        images = Images(
            large = "donec",
            common = "mandamus",
            medium = "pellentesque",
            small = "ferri",
            grid = "natoque"
        ),
        volumes = 8709,
        eps = 8315,
        totalEpisodes = 2238,
        rating = Rating(
            rank = 5821, total = 4784, count = Count(
                _1 = null,
                _2 = null,
                _3 = null,
                _4 = null,
                _5 = null,
                _6 = null,
                _7 = null,
                _8 = null,
                _9 = null,
                _10 = null
            ),
            score = BigDecimal.ZERO
        ),
        collection = Collection(
            wish = 6848,
            collect = 6029,
            doing = 4929,
            onHold = 2523,
            dropped = 3158
        ),
        tags = listOf(),
        date = null,
        infobox = listOf()
    )
}