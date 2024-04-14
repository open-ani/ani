package me.him188.ani.app.ui.subject.cache

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import me.him188.ani.app.data.media.EpisodeCacheStatus
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.topic.FileSize.Companion.megaBytes
import me.him188.ani.datasources.api.topic.UnifiedCollectionType

@Preview
@Composable
private fun PreviewSubjectCachePage() {
    ProvideCompositionLocalsForPreview {
        SubjectCachePage(
            state = object : SubjectCacheState {
                override val episodes: List<EpisodeCacheState> =
                    listOf(
                        EpisodeCacheState(
                            1,
                            sort = EpisodeSort(1),
                            title = "第一集的标题",
                            watchStatus = UnifiedCollectionType.DONE,
                            cacheStatus = flowOf(EpisodeCacheStatus.Cached(flowOf(300.megaBytes))),
                            cacheProgress = MutableStateFlow(0.2f),
                        ),
                        EpisodeCacheState(
                            2,
                            sort = EpisodeSort(2),
                            title = "第二集的标题",
                            watchStatus = UnifiedCollectionType.DONE,
                            cacheStatus = flowOf(EpisodeCacheStatus.Caching),
                            cacheProgress = MutableStateFlow(0.7f),
                        ),
                        EpisodeCacheState(
                            3,
                            sort = EpisodeSort(3),
                            title = "第三集的标题",
                            watchStatus = UnifiedCollectionType.DOING,
                            cacheStatus = flowOf(EpisodeCacheStatus.NotCached),
                            cacheProgress = MutableStateFlow(0f),
                        ),
                    )
            },
            title = {
                Text(text = "葬送的芙莉莲")
            },
            onClickGlobalCacheSettings = {},
            onClickEpisode = {},
        )
    }
}