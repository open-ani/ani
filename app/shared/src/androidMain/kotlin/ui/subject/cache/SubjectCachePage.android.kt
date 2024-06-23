package me.him188.ani.app.ui.subject.cache

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.PreviewLightDark
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import me.him188.ani.app.data.media.EpisodeCacheStatus
import me.him188.ani.app.data.media.cache.requester.EpisodeCacheRequest
import me.him188.ani.app.data.media.cache.requester.EpisodeCacheRequester
import me.him188.ani.app.data.media.fetch.MediaFetcherConfig
import me.him188.ani.app.data.media.fetch.MediaSourceMediaFetcher
import me.him188.ani.app.data.media.instance.createTestMediaSourceInstance
import me.him188.ani.app.data.media.selector.MediaSelector
import me.him188.ani.app.data.media.selector.MediaSelectorFactory
import me.him188.ani.app.data.models.MediaSelectorSettings
import me.him188.ani.app.data.subject.EpisodeInfo
import me.him188.ani.app.data.subject.SubjectInfo
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.ui.cache.testMediaCache1
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.settings.SettingsTab
import me.him188.ani.datasources.acgrip.AcgRipMediaSource
import me.him188.ani.datasources.api.DefaultMedia
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.MediaProperties
import me.him188.ani.datasources.api.paging.SinglePagePagedSource
import me.him188.ani.datasources.api.source.MatchKind
import me.him188.ani.datasources.api.source.MediaMatch
import me.him188.ani.datasources.api.source.MediaSourceKind
import me.him188.ani.datasources.api.source.MediaSourceLocation
import me.him188.ani.datasources.api.source.TestHttpMediaSource
import me.him188.ani.datasources.api.topic.EpisodeRange
import me.him188.ani.datasources.api.topic.FileSize.Companion.bytes
import me.him188.ani.datasources.api.topic.FileSize.Companion.megaBytes
import me.him188.ani.datasources.api.topic.ResourceLocation
import me.him188.ani.datasources.api.topic.SubtitleLanguage.ChineseSimplified
import me.him188.ani.datasources.api.topic.SubtitleLanguage.ChineseTraditional
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import me.him188.ani.datasources.dmhy.DmhyMediaSource
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

const val SOURCE_DMHY = DmhyMediaSource.ID
const val SOURCE_ACG = AcgRipMediaSource.ID

val TestMediaList = listOf(
    DefaultMedia(
        mediaId = "$SOURCE_DMHY.1",
        mediaSourceId = SOURCE_DMHY,
        originalTitle = "[桜都字幕组] 孤独摇滚 ABC ABC ABC ABC ABC ABC ABC ABC ABC ABC",
        download = ResourceLocation.MagnetLink("magnet:?xt=urn:btih:1"),
        originalUrl = "https://example.com/1",
        publishedTime = System.currentTimeMillis(),
        episodeRange = EpisodeRange.single(EpisodeSort(1)),
        properties = MediaProperties(
            subtitleLanguageIds = listOf(ChineseSimplified, ChineseTraditional).map { it.id },
            resolution = "1080P",
            alliance = "桜都字幕组",
            size = 122.megaBytes,
        ),
        kind = MediaSourceKind.BitTorrent,
        location = MediaSourceLocation.Online,
    ),
    // exactly same properties as the first one, except for the ids.
    DefaultMedia(
        mediaId = "$SOURCE_ACG.1",
        mediaSourceId = SOURCE_ACG,
        originalTitle = "[桜都字幕组] 孤独摇滚 ABC ABC ABC ABC ABC ABC ABC ABC ABC ABC",
        download = ResourceLocation.MagnetLink("magnet:?xt=urn:btih:1"),
        originalUrl = "https://example.com/1",
        publishedTime = System.currentTimeMillis(),
        episodeRange = EpisodeRange.single(EpisodeSort(1)),
        properties = MediaProperties(
            subtitleLanguageIds = listOf(ChineseSimplified, ChineseTraditional).map { it.id },
            resolution = "1080P",
            alliance = "桜都字幕组",
            size = 122.megaBytes,
        ),
        kind = MediaSourceKind.BitTorrent,
        location = MediaSourceLocation.Online,
    ),

    DefaultMedia(
        mediaId = "$SOURCE_DMHY.2",
        mediaSourceId = SOURCE_DMHY,
        originalTitle = "夜晚的水母不会游泳",
        download = ResourceLocation.MagnetLink("magnet:?xt=urn:btih:1"),
        originalUrl = "https://example.com/1",
        publishedTime = System.currentTimeMillis(),
        episodeRange = EpisodeRange.single(EpisodeSort(2)),
        properties = MediaProperties(
            subtitleLanguageIds = listOf(ChineseTraditional).map { it.id },
            resolution = "1080P",
            alliance = "北宇治字幕组北宇治字幕组北宇治字幕组北宇治字幕组北宇治字幕组北宇治字幕组北宇治字幕组北宇治字幕组",
            size = 233.megaBytes,
        ),
        kind = MediaSourceKind.BitTorrent,
        location = MediaSourceLocation.Online,
    ),
    DefaultMedia(
        mediaId = "$SOURCE_ACG.2",
        mediaSourceId = SOURCE_ACG,
        originalTitle = "葬送的芙莉莲",
        download = ResourceLocation.MagnetLink("magnet:?xt=urn:btih:1"),
        originalUrl = "https://example.com/1",
        publishedTime = System.currentTimeMillis(),
        episodeRange = EpisodeRange.single(EpisodeSort(2)),
        properties = MediaProperties(
            subtitleLanguageIds = listOf(ChineseSimplified).map { it.id },
            resolution = "1080P",
            alliance = "桜都字幕组",
            size = 0.bytes,
        ),
        kind = MediaSourceKind.BitTorrent,
        location = MediaSourceLocation.Online,
    ),
    DefaultMedia(
        mediaId = "$SOURCE_ACG.3",
        mediaSourceId = SOURCE_ACG,
        originalTitle = "某个生肉",
        download = ResourceLocation.MagnetLink("magnet:?xt=urn:btih:1"),
        originalUrl = "https://example.com/1",
        publishedTime = System.currentTimeMillis(),
        episodeRange = EpisodeRange.single(EpisodeSort(3)),
        properties = MediaProperties(
            subtitleLanguageIds = listOf(),
            resolution = "1080P",
            alliance = "Lilith-Raws",
            size = 702.megaBytes,
        ),
        kind = MediaSourceKind.BitTorrent,
        location = MediaSourceLocation.Online,
    ),
)


@Suppress("FunctionName")
fun PreviewEpisodeCacheRequester(): EpisodeCacheRequester = EpisodeCacheRequester(
    flowOf(
        MediaSourceMediaFetcher(
            configProvider = { MediaFetcherConfig.Default },
            mediaSources = listOf(
                createTestMediaSourceInstance(
                    TestHttpMediaSource(
                        fetch = {
                            SinglePagePagedSource {
                                TestMediaList.map { MediaMatch(it, MatchKind.EXACT) }.asFlow()
                            }
                        },
                    ),
                ),
            ),
            flowContext = EmptyCoroutineContext,
        ),
    ),
    mediaSelectorFactory = object : MediaSelectorFactory {
        override fun create(
            subjectId: Int,
            mediaList: Flow<List<Media>>,
            flowCoroutineContext: CoroutineContext
        ): MediaSelector {
            throw UnsupportedOperationException()
        }
    },
    storagesLazy = flowOf(),
)


private val episodeCacheStateList = listOf(
    TestEpisodeCacheState(
        1,
        cacheRequesterLazy = { PreviewEpisodeCacheRequester() },
        infoFlow = MutableStateFlow(
            EpisodeCacheInfo(
                sort = EpisodeSort(1),
                ep = EpisodeSort(1),
                title = "第一集的标题",
                watchStatus = UnifiedCollectionType.DONE,
                hasPublished = true,
            ),
        ),
        cacheStatusFlow = MutableStateFlow(
            EpisodeCacheStatus.Cached(
                300.megaBytes,
                testMediaCache1,
            ),
        ),
        parentCoroutineContext = EmptyCoroutineContext,
    ),
    TestEpisodeCacheState(
        2,
        cacheRequesterLazy = { PreviewEpisodeCacheRequester() },
        infoFlow = MutableStateFlow(
            EpisodeCacheInfo(
                sort = EpisodeSort(2),
                ep = EpisodeSort(2),
                title = "第二集的标题第二集的标题第二集的标题第二集的标题第二集的标题第二集的标题第二集的标题第二集的标题",
                watchStatus = UnifiedCollectionType.DONE,
                hasPublished = true,
            ),
        ),
        cacheStatusFlow = MutableStateFlow(
            EpisodeCacheStatus.Caching(
                progress = 0.3f,
                totalSize = 300.megaBytes,
                testMediaCache1,
            ),
        ),
        parentCoroutineContext = EmptyCoroutineContext,
    ),
    TestEpisodeCacheState(
        3,
        cacheRequesterLazy = { PreviewEpisodeCacheRequester() },
        infoFlow = MutableStateFlow(
            EpisodeCacheInfo(
                sort = EpisodeSort(3),
                ep = EpisodeSort(3),
                title = "第三集的标题第三集的标题第三集的标题第三集的标题第三集的标题第三集的标题第三集的标题第三集的标题",
                watchStatus = UnifiedCollectionType.DOING,
                hasPublished = true,
            ),
        ),
        cacheStatusFlow = MutableStateFlow(
            EpisodeCacheStatus.NotCached,
        ),
        parentCoroutineContext = EmptyCoroutineContext,
    ),
    TestEpisodeCacheState(
        4,
        cacheRequesterLazy = { PreviewEpisodeCacheRequester() },
        infoFlow = MutableStateFlow(
            EpisodeCacheInfo(
                sort = EpisodeSort(4),
                ep = EpisodeSort(4),
                title = "第四集的标题",
                watchStatus = UnifiedCollectionType.DOING,
                hasPublished = false,
            ),
        ),
        cacheStatusFlow = MutableStateFlow(
            EpisodeCacheStatus.NotCached,
        ),
        parentCoroutineContext = EmptyCoroutineContext,
    ),
)


///////////////////////////////////////////////////////////////////////////
// Previews
///////////////////////////////////////////////////////////////////////////


@PreviewLightDark
@Composable
private fun PreviewSubjectCachePage() {
    ProvideCompositionLocalsForPreview {
        val cacheListState = remember {
            EpisodeCacheListStateImpl(
                MutableStateFlow(episodeCacheStateList),
                onRequestCache = { it, _ ->
                    delay(2000)
                    it.cacheRequester.request(
                        EpisodeCacheRequest(
                            subjectInfo = SubjectInfo(),
                            episodeInfo = EpisodeInfo(1),
                        ),
                    )
                },
                onRequestCacheComplete = { target ->
                    delay(2000)
                    target.episode as TestEpisodeCacheState
                    target.episode.cacheStatusFlow.value = EpisodeCacheStatus.Cached(300.megaBytes, testMediaCache1)
                },
                onDeleteCache = { episode ->
                    delay(2000)
                    episode as TestEpisodeCacheState
                    episode.cacheStatusFlow.value = EpisodeCacheStatus.NotCached
                },
                EmptyCoroutineContext,
            )
        }

        SubjectCachePageScaffold(
            title = {
                Text("测试")
            },
            autoCacheGroup = {
                val navigator = LocalNavigator.current
                AutoCacheGroup(
                    onClickGlobalCacheSettings = {
                        navigator.navigateSettings(SettingsTab.MEDIA)
                    },
                    onClickGlobalCacheManage = {
                        navigator.navigateCaches()
                    },
                )
            },
            cacheListGroup = {
                EpisodeCacheListGroup(
                    cacheListState,
                    mediaSelectorSettingsProvider = {
                        flowOf(MediaSelectorSettings.Default)
                    },
                )
            },
        )
    }
}