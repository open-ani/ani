package me.him188.ani.app.ui.subject.collection

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.content.res.Configuration.UI_MODE_TYPE_NORMAL
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import me.him188.ani.app.data.models.episode.EpisodeCollection
import me.him188.ani.app.data.models.episode.EpisodeInfo
import me.him188.ani.app.data.models.subject.SubjectCollection
import me.him188.ani.app.data.models.subject.SubjectInfo
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.stateOf
import me.him188.ani.app.ui.subject.collection.progress.ContinueWatchingStatus
import me.him188.ani.app.ui.subject.collection.progress.PlaySubjectButton
import me.him188.ani.app.ui.subject.collection.progress.SubjectProgressInfo
import me.him188.ani.app.ui.subject.collection.progress.SubjectProgressState
import me.him188.ani.app.ui.subject.details.components.rememberTestEditableSubjectCollectionTypeState
import me.him188.ani.app.ui.subject.episode.details.PreviewScope
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.topic.UnifiedCollectionType


val TestSubjectCollections = buildList {
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
        testSubjectCollection(++id, eps, UnifiedCollectionType.DOING),
    )
    add(
        testSubjectCollection(++id, eps, UnifiedCollectionType.DOING),
    )
    add(
        testSubjectCollection(++id, eps, UnifiedCollectionType.DOING),
    )
    add(
        testSubjectCollection(++id, eps, collectionType = UnifiedCollectionType.WISH),
    )
    repeat(10) {
        add(
            testSubjectCollection(
                ++id,
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

@Composable
internal fun rememberTestSubjectCollectionColumnState(
    cachedData: List<SubjectCollection> = TestSubjectCollections,
    hasMore: Boolean = false,
    isKnownEmpty: Boolean = false,
) = remember {
    SubjectCollectionColumnState(
        cachedData = mutableStateOf(cachedData),
        hasMore = mutableStateOf(hasMore),
        isKnownEmpty = mutableStateOf(isKnownEmpty),
        onRequestMore = {},
        backgroundScope = PreviewScope,
    )
}

private fun testSubjectCollection(
    id: Int,
    episodes: List<EpisodeCollection>,
    collectionType: UnifiedCollectionType,
) = SubjectCollection(
    info = SubjectInfo.Empty.copy(
        id,
        nameCn = "中文条目名称",
        name = "Subject Name",
    ),
    episodes = episodes,
    collectionType = collectionType,
    selfRatingInfo = TestSelfRatingInfo,
)

@Stable
object TestSubjectProgressInfos {
    @Stable
    val NotOnAir = SubjectProgressInfo(
        continueWatchingStatus = ContinueWatchingStatus.NotOnAir,
        nextEpisodeToPlay = null,
    )

    @Stable
    val ContinueWatching2 = SubjectProgressInfo(
        continueWatchingStatus = ContinueWatchingStatus.Continue(1, EpisodeSort(2)),
        nextEpisodeToPlay = null,
    )

    @Stable
    val Done = SubjectProgressInfo(
        continueWatchingStatus = ContinueWatchingStatus.Done,
        nextEpisodeToPlay = null,
    )
}

@Composable
fun rememberTestSubjectProgressState(
    info: SubjectProgressInfo = SubjectProgressInfo.Empty,
): SubjectProgressState {
    return remember {
        SubjectProgressState(
            stateOf(1),
            info = stateOf(info),
            episodeProgressInfos = mutableStateOf(emptyList()),
            onPlay = { _, _ -> },
        )
    }
}

@PreviewLightDark
@Composable
private fun PreviewSubjectCollectionsColumnPhone() {
    ProvideCompositionLocalsForPreview {
        SubjectCollectionsColumn(
            state = rememberTestSubjectCollectionColumnState(),
            item = { TestSubjectCollectionItem(it) },
            onEmpty = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun PreviewSubjectCollectionsColumnEmptyButLoading() {
    ProvideCompositionLocalsForPreview {
        SubjectCollectionsColumn(
            state = rememberTestSubjectCollectionColumnState(
                cachedData = emptyList(),
                hasMore = true,
                isKnownEmpty = false,
            ),
            item = { TestSubjectCollectionItem(it) },
            onEmpty = {},
            Modifier.fillMaxWidth(),
        )
    }
}

@PreviewLightDark
@Composable
private fun PreviewSubjectCollectionsColumnEmpty() {
    ProvideCompositionLocalsForPreview {
        SubjectCollectionsColumn(
            state = rememberTestSubjectCollectionColumnState(
                cachedData = emptyList(),
                hasMore = false,
                isKnownEmpty = true,
            ),
            item = { TestSubjectCollectionItem(it) },
            onEmpty = {},
            Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun TestSubjectCollectionItem(it: SubjectCollection) {
    SubjectCollectionItem(
        item = it,
        editableSubjectCollectionTypeState = rememberTestEditableSubjectCollectionTypeState(),
        onClick = { },
        onShowEpisodeList = { },
        playButton = {
            PlaySubjectButton(
                state = rememberTestSubjectProgressState(
                    when (it.subjectId % 3) {
                        0 -> TestSubjectProgressInfos.NotOnAir
                        1 -> TestSubjectProgressInfos.ContinueWatching2
                        else -> TestSubjectProgressInfos.Done
                    },
                ),
            )
        },
    )
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
            rememberTestSubjectCollectionColumnState(),
            item = { TestSubjectCollectionItem(it) },
            onEmpty = {},
        )
    }
}
