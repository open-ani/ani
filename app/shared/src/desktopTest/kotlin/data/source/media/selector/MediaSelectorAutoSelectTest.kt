package me.him188.ani.app.data.source.media.selector

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import me.him188.ani.app.data.models.preference.MediaPreference
import me.him188.ani.app.data.models.preference.MediaSelectorSettings
import me.him188.ani.app.data.source.media.SOURCE_DMHY
import me.him188.ani.app.data.source.media.TestMediaList
import me.him188.ani.app.data.source.media.createTestDefaultMedia
import me.him188.ani.app.data.source.media.fetch.MediaFetchSession
import me.him188.ani.app.data.source.media.fetch.MediaFetcherConfig
import me.him188.ani.app.data.source.media.fetch.MediaSourceFetchState
import me.him188.ani.app.data.source.media.fetch.MediaSourceMediaFetcher
import me.him188.ani.app.data.source.media.instance.createTestMediaSourceInstance
import me.him188.ani.datasources.api.DefaultMedia
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.MediaProperties
import me.him188.ani.datasources.api.paging.SinglePagePagedSource
import me.him188.ani.datasources.api.source.MatchKind
import me.him188.ani.datasources.api.source.MediaFetchRequest
import me.him188.ani.datasources.api.source.MediaMatch
import me.him188.ani.datasources.api.source.MediaSourceKind
import me.him188.ani.datasources.api.source.MediaSourceLocation
import me.him188.ani.datasources.api.source.TestHttpMediaSource
import me.him188.ani.datasources.api.topic.EpisodeRange
import me.him188.ani.datasources.api.topic.FileSize.Companion.megaBytes
import me.him188.ani.datasources.api.topic.Resolution
import me.him188.ani.datasources.api.topic.ResourceLocation
import me.him188.ani.datasources.api.topic.SubtitleLanguage.ChineseSimplified
import me.him188.ani.datasources.api.topic.SubtitleLanguage.ChineseTraditional
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * @see MediaSelectorAutoSelect
 */
class MediaSelectorAutoSelectTest {
    private val mediaList: MutableStateFlow<MutableList<DefaultMedia>> = MutableStateFlow(
        TestMediaList.toMutableList(),
    )
    private fun addMedia(vararg media: DefaultMedia) {
        mediaList.value.addAll(media)
    }

    private val savedUserPreference = MutableStateFlow(DEFAULT_PREFERENCE)
    private val savedDefaultPreference = MutableStateFlow(DEFAULT_PREFERENCE)
    private val mediaSelectorSettings = MutableStateFlow(MediaSelectorSettings.Default)
    private val mediaSelectorContext = MutableStateFlow(
        MediaSelectorContext(
            subjectFinished = false,
            mediaSourcePrecedence = emptyList(),
            subtitlePreferences = MediaSelectorSubtitlePreferences.AllNormal,
        ),
    )

    private val selector = DefaultMediaSelector(
        mediaSelectorContextNotCached = mediaSelectorContext,
        mediaListNotCached = mediaList,
        savedUserPreference = savedUserPreference,
        savedDefaultPreference = savedDefaultPreference,
        enableCaching = false,
        mediaSelectorSettings = mediaSelectorSettings,
    )

    companion object {
        private val DEFAULT_PREFERENCE = MediaPreference.Empty.copy(
            fallbackResolutions = listOf(
                Resolution.R2160P,
                Resolution.R1440P,
                Resolution.R1080P,
                Resolution.R720P,
            ).map { it.id },
            fallbackSubtitleLanguageIds = listOf(
                ChineseSimplified,
                ChineseTraditional,
            ).map { it.id },
        )
    }

    @Suppress("SameParameterValue")
    private fun createMediaSelectorContextFromEmpty(
        subjectCompleted: Boolean = false,
        mediaSourcePrecedence: List<String> = emptyList()
    ) =
        MediaSelectorContext(
            subjectFinished = subjectCompleted,
            mediaSourcePrecedence = mediaSourcePrecedence,
            subtitlePreferences = MediaSelectorSubtitlePreferences.AllNormal,
        )

    private val mediaFetcher: MediaSourceMediaFetcher = MediaSourceMediaFetcher(
        configProvider = { MediaFetcherConfig.Default },
        mediaSources = listOf(
            createTestMediaSourceInstance(
                TestHttpMediaSource(
                    fetch = {
                        SinglePagePagedSource {
                            mediaList.value.map { MediaMatch(it, MatchKind.EXACT) }.asFlow()
                        }
                    },
                ),
            ),
        ),
        flowContext = EmptyCoroutineContext,
    )

    private fun mediaFetchSession() = mediaFetcher.newSession(
        MediaFetchRequest(
            subjectId = "1",
            episodeId = "1",
            subjectNames = setOf("孤独摇滚"),
            episodeSort = EpisodeSort(1),
            episodeName = "test",
        ),
    )

    private val autoSelect get() = selector.autoSelect

    private suspend fun mediaFetchSessionWithFetchHook(
        addBtSources: Boolean,
        addWebSources: Boolean,
        preferKind: MediaSourceKind?,
        beforeBtFetch: suspend () -> Unit = {},
        afterBtFetch: suspend () -> Unit = {},
        beforeWebFetch: suspend () -> Unit = {},
        afterWebFetch: suspend () -> Unit = {},
    ): MediaFetchSession {
        val mediaList = mutableListOf<DefaultMedia>()
        // 至少保持一个 local cache 类型
        mediaList.addAll(TestMediaList.take(1).map { it.copy(kind = MediaSourceKind.LocalCache) })
        if (addWebSources) mediaList.addAll(TestMediaList.map { it.copy(kind = MediaSourceKind.WEB) })
        if (addBtSources) mediaList.addAll(TestMediaList.map { it.copy(kind = MediaSourceKind.BitTorrent) })
        this.mediaList.value = mediaList
        mediaSelectorSettings.value = MediaSelectorSettings.Default.copy(preferKind = preferKind)

        val mediaFetcher = MediaSourceMediaFetcher(
            configProvider = { MediaFetcherConfig.Default },
            mediaSources = listOf(
                createTestMediaSourceInstance(
                    isEnabled = addBtSources,
                    source = TestHttpMediaSource(
                        fetch = {
                            beforeBtFetch()
                            SinglePagePagedSource {
                                mediaList.filter { it.kind == MediaSourceKind.BitTorrent }
                                    .map { MediaMatch(it, MatchKind.EXACT) }.asFlow()
                            }.also { afterBtFetch() }
                        },
                    ),
                ),
                createTestMediaSourceInstance(
                    isEnabled = addWebSources,
                    source = TestHttpMediaSource(
                        kind = MediaSourceKind.WEB,
                        fetch = {
                            beforeWebFetch()
                            SinglePagePagedSource {
                                mediaList.filter { it.kind == MediaSourceKind.WEB }
                                    .map { MediaMatch(it.copy(kind = MediaSourceKind.WEB), MatchKind.EXACT) }.asFlow()
                            }.also { afterWebFetch() }
                        },
                    ),
                ),
            ),
            flowContext = EmptyCoroutineContext,
        )
        return mediaFetcher.newSession(
            MediaFetchRequest(
                subjectId = "1",
                episodeId = "1",
                subjectNames = setOf("孤独摇滚"),
                episodeSort = EpisodeSort(1),
                episodeName = "test",
            ),
        )
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // awaitCompletedAndSelectDefault
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `awaitCompletedAndSelectDefault selects one`() = runTest {
        val selected = autoSelect.awaitCompletedAndSelectDefault(mediaFetchSession())
        assertNotNull(selected)
    }

    @Test
    fun `awaitCompletedAndSelectDefault twice does not select`() = runTest {
        val selected = autoSelect.awaitCompletedAndSelectDefault(mediaFetchSession())
        assertNotNull(selected)
        assertNull(
            autoSelect.awaitCompletedAndSelectDefault(
                mediaFetchSession(),
            ),
        )
    }

    @Test
    fun `awaitCompletedAndSelectDefault does not select if already selected`() = runTest {
        selector.select(TestMediaList.first())
        val selected = autoSelect.awaitCompletedAndSelectDefault(mediaFetchSession())
        assertEquals(null, selected)
    }

    // no need to test preferences, as they have already been tested in MediaSelectorTest

    ///////////////////////////////////////////////////////////////////////////
    // selectCached
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `selectCached selects one when there is one cache`() = runTest {
        val target = createTestDefaultMedia(
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
                subtitleKind = null,
            ),
            kind = MediaSourceKind.LocalCache, // note here
            location = MediaSourceLocation.Online,
        )
        addMedia(target)
        val isSuccess = autoSelect.selectCached(mediaFetchSession(), 1)
        assertEquals(target, isSuccess)
        assertNull(autoSelect.selectCached(mediaFetchSession(), 1)) // already selected
    }

    @Test
    fun `selectCached selects first one when there are multiple caches`() = runTest {
        val target = createTestDefaultMedia(
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
                subtitleKind = null,
            ),
            kind = MediaSourceKind.LocalCache, // note here
            location = MediaSourceLocation.Online,
        )
        addMedia(target)
        addMedia(target.copy(mediaId = "dmhy.7"))
        addMedia(target.copy(mediaId = "dmhy.10"))
        val isSuccess = autoSelect.selectCached(mediaFetchSession(), 1)
        assertEquals(target, isSuccess)

        assertNull(autoSelect.selectCached(mediaFetchSession(), 1)) // already selected
    }

    @Test
    fun `selectCached selects null when there is no cache`() = runTest {
        val isSuccess = autoSelect.selectCached(mediaFetchSession(), 1)
        assertNull(isSuccess)
    }

    @Test
    fun `priority select preferred data sources when prefer bt and bt done`() = runTest {
        val completableDeferred = CompletableDeferred<Unit>()
        val session = mediaFetchSessionWithFetchHook(
            addBtSources = true,
            addWebSources = true,
            preferKind = MediaSourceKind.BitTorrent,
            beforeWebFetch = {
                completableDeferred.await()
            },
        )
        val selected = autoSelect.awaitCompletedAndSelectDefault(session, mediaSelectorSettings.map { it.preferKind })
        assertNotNull(selected)
        assertEquals(MediaSourceKind.BitTorrent, selected.kind)
    }

    @Test
    fun `priority select preferred data sources when prefer bt and web done`() = runTest {
        val completableDeferred = CompletableDeferred<Unit>()
        val session = mediaFetchSessionWithFetchHook(
            addBtSources = true,
            addWebSources = true,
            preferKind = MediaSourceKind.BitTorrent,
            beforeBtFetch = {
                completableDeferred.await()
            },
            afterWebFetch = {
                completableDeferred.complete(Unit)
            },
        )
        val selected = autoSelect.awaitCompletedAndSelectDefault(session, mediaSelectorSettings.map { it.preferKind })
        assertNotNull(selected)
        assertEquals(MediaSourceKind.BitTorrent, selected.kind)
    }

    @Test
    fun `priority select preferred data sources when prefer bt and bt media source disable`() = runTest {
        val session = mediaFetchSessionWithFetchHook(
            addBtSources = false,
            addWebSources = true,
            preferKind = MediaSourceKind.BitTorrent,
        )
        val selected = autoSelect.awaitCompletedAndSelectDefault(session, mediaSelectorSettings.map { it.preferKind })
        val btRes = session.mediaSourceResults[0]
        assertIs<MediaSourceFetchState.Disabled>(btRes.state.value)
        assertNotNull(selected)
        assertEquals(MediaSourceKind.WEB, selected.kind)
    }

    @Test
    fun `priority select preferred data sources when no prefer and bt done`() = runTest {
        val completableDeferred = CompletableDeferred<Unit>()
        val session = mediaFetchSessionWithFetchHook(
            addBtSources = true,
            addWebSources = true,
            preferKind = null,
            afterBtFetch = {
                completableDeferred.complete(Unit)
            },
            beforeWebFetch = {
                completableDeferred.await()
            },
        )
        val selected = autoSelect.awaitCompletedAndSelectDefault(session)
        assertNotNull(selected)
    }

    @Test
    fun `priority select preferred data sources when prefer bt and bt media source no results`() = runTest {
        mediaList.value = TestMediaList.map { it.copy(kind = MediaSourceKind.WEB) }.toMutableList()
        mediaSelectorSettings.value = MediaSelectorSettings.Default.copy(preferKind = MediaSourceKind.BitTorrent)
        val mediaFetcher = MediaSourceMediaFetcher(
            configProvider = { MediaFetcherConfig.Default },
            mediaSources = listOf(
                createTestMediaSourceInstance(
                    source = TestHttpMediaSource(),
                ),
                createTestMediaSourceInstance(
                    source = TestHttpMediaSource(
                        kind = MediaSourceKind.WEB,
                        fetch = {
                            SinglePagePagedSource {
                                mediaList.value.filter { it.kind == MediaSourceKind.WEB }
                                    .map { MediaMatch(it, MatchKind.EXACT) }.asFlow()
                            }
                        },
                    ),
                ),
            ),
            flowContext = EmptyCoroutineContext,
        )
        val session = mediaFetcher.newSession(
            MediaFetchRequest(
                subjectId = "1",
                episodeId = "1",
                subjectNames = setOf("孤独摇滚"),
                episodeSort = EpisodeSort(1),
                episodeName = "test",
            ),
        )

        val selected = autoSelect.awaitCompletedAndSelectDefault(session, mediaSelectorSettings.map { it.preferKind })
        assertNotNull(selected)
        assertEquals(MediaSourceKind.WEB, selected.kind)
    }
}
