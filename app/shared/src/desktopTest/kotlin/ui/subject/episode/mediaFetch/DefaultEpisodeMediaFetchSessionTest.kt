package me.him188.ani.app.ui.subject.episode.mediaFetch

import me.him188.ani.datasources.acgrip.AcgRipMediaSource
import me.him188.ani.datasources.api.DefaultMedia
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.MediaProperties
import me.him188.ani.datasources.api.source.MediaSourceKind
import me.him188.ani.datasources.api.source.MediaSourceLocation
import me.him188.ani.datasources.api.topic.EpisodeRange
import me.him188.ani.datasources.api.topic.FileSize.Companion.bytes
import me.him188.ani.datasources.api.topic.FileSize.Companion.megaBytes
import me.him188.ani.datasources.api.topic.ResourceLocation
import me.him188.ani.datasources.api.topic.SubtitleLanguage.ChineseSimplified
import me.him188.ani.datasources.api.topic.SubtitleLanguage.ChineseTraditional
import me.him188.ani.datasources.dmhy.DmhyMediaSource

//class DefaultEpisodeMediaFetchSessionTest {
//    private val episodePreferences: DataStore<Preferences> = MemoryDataStore(mutablePreferencesOf())
//    private val defaultMediaPreferences: MutableStateFlow<MediaPreference> = MutableStateFlow(MediaPreference.Empty)
//    private val mediaSelectorSettings = MutableStateFlow(MediaSelectorSettings())
//    private val subjectCompleted = MutableStateFlow(false)
//    private val mediaSourceInstances = MutableStateFlow<List<MediaSourceInstance>>(emptyList())
//
//    private fun createSession(
//        subjectInfo: SubjectInfo = SubjectInfo(),
//    ): DefaultEpisodeMediaFetchSession {
//        return DefaultEpisodeMediaFetchSession(
//            subjectId = 100,
//            subject = flowOf(subjectInfo),
//            episode = flowOf(EpisodeInfo(100)),
//            episodePreferencesRepository = EpisodePreferencesRepositoryImpl(
//                episodePreferences,
//                flowOf(MediaPreference.Empty)
//            ),
//            defaultMediaPreferenceFlow = defaultMediaPreferences,
//            mediaSelectorSettingsFlow = mediaSelectorSettings,
//            subjectCompletedNotCached = subjectCompleted,
//            mediaSourceInstances = mediaSourceInstances,
//            parentCoroutineContext = EmptyCoroutineContext,
//        )
//    }
//
//    @Test
//    fun `can create session`() {
//        createSession()
//    }
//
//    @Test
//    fun `auto fetch media after completion`() = runTest {
//        mediaSourceInstances.value = listOf(
//            createTestMediaSourceInstance(
//                TestHttpMediaSource(fetch = {
//                    SinglePagePagedSource {
//                        TestMediaList.map { MediaMatch(it, MatchKind.EXACT) }.asFlow()
//                    }
//                })
//            )
//        )
//        val session = createSession()
//        cancellableCoroutineScope {
//            launch { // watchdog
//                yield()
//                val fetch = session.mediaFetchSession.first()
//                while (isActive) {
//                    println(fetch.cumulativeResults.first().size)
//                    println(fetch.resultsPerSource.values.first().state.value)
//                    println()
//                    withContext(Dispatchers.Default) { delay(1000) }
//                }
//            }
//            session.doAutoSelectOnFetchCompletion()
//            cancelScope()
//        }
//    }
//}

private const val SOURCE_DMHY = DmhyMediaSource.ID
private const val SOURCE_ACG = AcgRipMediaSource.ID

internal val TestMediaList = listOf(
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
