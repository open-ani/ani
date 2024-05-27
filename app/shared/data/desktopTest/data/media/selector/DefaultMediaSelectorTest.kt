package me.him188.ani.app.data.media.selector

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaPreference
import me.him188.ani.datasources.api.DefaultMedia
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.MediaProperties
import me.him188.ani.datasources.api.source.MediaSourceKind
import me.him188.ani.datasources.api.source.MediaSourceLocation
import me.him188.ani.datasources.api.topic.EpisodeRange
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.api.topic.FileSize.Companion.megaBytes
import me.him188.ani.datasources.api.topic.ResourceLocation
import me.him188.ani.datasources.api.topic.SubtitleLanguage.ChineseSimplified
import me.him188.ani.datasources.api.topic.SubtitleLanguage.ChineseTraditional
import me.him188.ani.utils.coroutines.cancellableCoroutineScope
import org.junit.jupiter.api.Assertions.assertNotEquals
import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultMediaSelectorTest {
    private val mediaList: MutableStateFlow<MutableList<DefaultMedia>> = MutableStateFlow(mutableListOf())
    private fun addMedia(vararg media: DefaultMedia) {
        mediaList.value.addAll(media)
    }

    private val savedUserPreference = MutableStateFlow(MediaPreference.Empty)
    private val savedDefaultPreference = MutableStateFlow(MediaPreference.Empty)

    private val selector = DefaultMediaSelector(
        mediaListNotCached = mediaList,
        savedUserPreference = savedUserPreference,
        savedDefaultPreference = savedDefaultPreference,
        enableCaching = false,
    )

    ///////////////////////////////////////////////////////////////////////////
    // 单个选项测试
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `initial empty preferences`() = runTest {
        assertEquals(null, selector.alliance.defaultSelected.first())
        assertEquals(null, selector.alliance.userSelected.first().preferredValueOrNull)
        assertEquals(null, selector.alliance.finalSelected.first())
    }

    @Test
    fun `initial empty preferences when list is not empty`() = runTest {
        addMedia(media(alliance = "桜都字幕组"))
        assertEquals(null, selector.alliance.defaultSelected.first())
        assertEquals(null, selector.alliance.userSelected.first().preferredValueOrNull)
        assertEquals(null, selector.alliance.finalSelected.first())
    }

    @Test
    fun `prefer alliance`() = runTest {
        addMedia(media(alliance = "桜都字幕组"))
        selector.alliance.prefer("桜都字幕组")
        assertEquals(null, selector.alliance.defaultSelected.first())
        assertEquals("桜都字幕组", selector.alliance.userSelected.first().preferredValueOrNull)
        assertEquals("桜都字幕组", selector.alliance.finalSelected.first())
    }

    @Test
    fun `default prefer alliance`() = runTest {
        addMedia(media(alliance = "桜都字幕组"))
        savedDefaultPreference.value = MediaPreference(alliance = "桜都字幕组")
        assertEquals("桜都字幕组", selector.alliance.defaultSelected.first())
        assertEquals(null, selector.alliance.userSelected.first().preferredValueOrNull)
        assertEquals("桜都字幕组", selector.alliance.finalSelected.first())
    }

    @Test
    fun `user override alliance`() = runTest {
        addMedia(media(alliance = "桜都字幕组"))
        savedDefaultPreference.value = MediaPreference(alliance = "桜都字幕组")
        assertEquals("桜都字幕组", selector.alliance.defaultSelected.first())
        assertEquals(null, selector.alliance.userSelected.first().preferredValueOrNull)
        assertEquals("桜都字幕组", selector.alliance.finalSelected.first())
    }

    @Test
    fun `user override no preference`() = runTest {
        addMedia(media(alliance = "桜都字幕组"))
        savedDefaultPreference.value = MediaPreference(alliance = "桜都字幕组")
        assertEquals("桜都字幕组", selector.alliance.defaultSelected.first())
        assertEquals(false, selector.alliance.userSelected.first().isPreferNoValue)
        assertNotEquals(null, selector.alliance.finalSelected.first())
        selector.alliance.removePreference()
        assertEquals(true, selector.alliance.userSelected.first().isPreferNoValue)
        assertEquals(null, selector.alliance.finalSelected.first())
    }

    ///////////////////////////////////////////////////////////////////////////
    // 选择数据测试
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `empty preferences select all`() = runTest {
        addMedia(media(alliance = "桜都字幕组"), media(alliance = "桜都字幕组"))
        assertEquals(mediaList.value, selector.filteredCandidates.first())
    }

    @Test
    fun `select by user alliance`() = runTest {
        val media = media(alliance = "桜都字幕组")
        addMedia(media, media(alliance = "字幕组"))
        savedUserPreference.value = MediaPreference(alliance = "桜都字幕组")
        assertEquals(media, selector.filteredCandidates.first().single())
    }

    @Test
    fun `select by default alliance`() = runTest {
        val media = media(alliance = "桜都字幕组")
        addMedia(media, media(alliance = "字幕组"))
        savedDefaultPreference.value = MediaPreference(alliance = "桜都字幕组")
        assertEquals(media, selector.filteredCandidates.first().single())
    }

    @Test
    fun `select by user resolution`() = runTest {
        val media = media(resolution = "桜都字幕组")
        addMedia(media, media(resolution = "字幕组"))
        savedUserPreference.value = MediaPreference(resolution = "桜都字幕组")
        assertEquals(media, selector.filteredCandidates.first().single())
    }

    @Test
    fun `select by default resolution`() = runTest {
        val media = media(resolution = "桜都字幕组")
        addMedia(media, media(resolution = "字幕组"))
        savedDefaultPreference.value = MediaPreference(resolution = "桜都字幕组")
        assertEquals(media, selector.filteredCandidates.first().single())
    }

    @Test
    fun `select by user subtitle one of`() = runTest {
        val media = media(subtitleLanguages = listOf("桜都字幕组", "a"))
        addMedia(media, media(subtitleLanguages = listOf("b")))
        savedUserPreference.value = MediaPreference(subtitleLanguageId = "a")
        assertEquals(media, selector.filteredCandidates.first().single())
    }

    @Test
    fun `select by default subtitle one of`() = runTest {
        val media = media(subtitleLanguages = listOf("桜都字幕组", "a"))
        addMedia(media, media(subtitleLanguages = listOf("b")))
        savedDefaultPreference.value = MediaPreference(subtitleLanguageId = "a")
        assertEquals("a", selector.subtitleLanguageId.finalSelected.first())
        assertEquals(media, selector.filteredCandidates.first().single())
    }

    @Test
    fun `select none because pref not match`() = runTest {
        addMedia(media(alliance = "桜都字幕组"), media(alliance = "字幕组"))
        savedDefaultPreference.value = MediaPreference(alliance = "a")
        assertEquals(0, selector.filteredCandidates.first().size)
    }

    @Test
    fun `select with user override no preference`() = runTest {
        addMedia(media(alliance = "桜都字幕组"))
        savedDefaultPreference.value = MediaPreference(alliance = "组")
        assertEquals(0, selector.filteredCandidates.first().size)
        selector.alliance.removePreference()
        assertEquals(1, selector.filteredCandidates.first().size)
    }

    @Test
    fun `select with user override no preference then prefer`() = runTest {
        addMedia(media(alliance = "桜都字幕组"), media(alliance = "桜都字幕组2"))
        savedDefaultPreference.value = MediaPreference(alliance = "组")
        assertEquals(0, selector.filteredCandidates.first().size)
        selector.alliance.removePreference()
        assertEquals(2, selector.filteredCandidates.first().size)
        selector.alliance.prefer("桜都字幕组")
        assertEquals(1, selector.filteredCandidates.first().size)
    }

    ///////////////////////////////////////////////////////////////////////////
    // Default selection
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `select default first with no preference`() = runTest {
        val target = media(alliance = "桜都字幕组", subtitleLanguages = listOf("CHS"))
        addMedia(
            target,
            media(alliance = "桜都字幕组2", subtitleLanguages = listOf("CHT")),
            media(alliance = "桜都字幕组3", subtitleLanguages = listOf("CHS", "CHT")),
            media(alliance = "桜都字幕组2", subtitleLanguages = listOf("CHS"))
        )
        assertEquals(target, selector.trySelectDefault())
    }

    @Test
    fun `select default by alliance`() = runTest {
        val target = media(alliance = "桜都字幕组", subtitleLanguages = listOf("CHS"))
        addMedia(
            media(alliance = "桜都字幕组2", subtitleLanguages = listOf("CHT")),
            media(alliance = "桜都字幕组3", subtitleLanguages = listOf("CHS", "CHT")),
            target,
            media(alliance = "桜都字幕组2", subtitleLanguages = listOf("CHS"))
        )
        selector.alliance.prefer("桜都字幕组")
        assertEquals(target, selector.trySelectDefault())
    }

    @Test
    fun `select default by alliance regex`() = runTest {
        val target: DefaultMedia
        addMedia(
            media(alliance = "桜都字幕组1", subtitleLanguages = listOf("CHS")),
            media(alliance = "桜都字幕组2", subtitleLanguages = listOf("CHT")),
            media(alliance = "桜都字幕组3", subtitleLanguages = listOf("CHS", "CHT")),
            media(alliance = "桜都字幕组4", subtitleLanguages = listOf("CHS", "CHT")).also { target = it },
            media(alliance = "桜都字幕组5", subtitleLanguages = listOf("CHS"))
        )
        savedDefaultPreference.value = MediaPreference(alliancePatterns = listOf("4"))
        assertEquals(target, selector.trySelectDefault())
    }

    @Test
    fun `select default by subtitle language`() = runTest {
        val target: DefaultMedia
        addMedia(
            media(alliance = "桜都字幕组1", subtitleLanguages = listOf("CHS")),
            media(alliance = "桜都字幕组2", subtitleLanguages = listOf("CHT")),
            media(alliance = "桜都字幕组3", subtitleLanguages = listOf("CHS", "CHT")),
            media(alliance = "桜都字幕组4", subtitleLanguages = listOf("CHS", "CHT", "R")).also { target = it },
            media(alliance = "桜都字幕组5", subtitleLanguages = listOf("CHS"))
        )
        savedDefaultPreference.value = MediaPreference(subtitleLanguageId = "R")
        assertEquals(target, selector.trySelectDefault())
    }

    @Test
    fun `select default by first fallback subtitle language`() = runTest {
        val target: DefaultMedia
        addMedia(
            media(alliance = "桜都字幕组1", subtitleLanguages = listOf("CHS")),
            media(alliance = "桜都字幕组2", subtitleLanguages = listOf("CHT")),
            media(alliance = "桜都字幕组3", subtitleLanguages = listOf("CHS", "CHT")),
            media(alliance = "桜都字幕组4", subtitleLanguages = listOf("CHS", "CHT", "R")).also { target = it },
            media(alliance = "桜都字幕组5", subtitleLanguages = listOf("CHS"))
        )
        savedDefaultPreference.value = MediaPreference(fallbackSubtitleLanguageIds = listOf("R", "CHS"))
        assertEquals(target, selector.trySelectDefault())
    }

    @Test
    fun `select default by resolution`() = runTest {
        val target: DefaultMedia
        addMedia(
            media(alliance = "桜都字幕组1", subtitleLanguages = listOf("CHS")),
            media(alliance = "桜都字幕组2", resolution = "Special").also { target = it },
            media(alliance = "桜都字幕组3", subtitleLanguages = listOf("CHS", "CHT")),
            media(alliance = "桜都字幕组4", subtitleLanguages = listOf("CHS", "CHT", "R")),
            media(alliance = "桜都字幕组5", subtitleLanguages = listOf("CHS"))
        )
        savedDefaultPreference.value = MediaPreference(resolution = "Special")
        assertEquals(target, selector.trySelectDefault())
    }

    @Test
    fun `select default by first fallback resolution`() = runTest {
        val target: DefaultMedia
        addMedia(
            media(alliance = "桜都字幕组1", subtitleLanguages = listOf("CHS")),
            media(alliance = "桜都字幕组2", resolution = "Special").also { target = it },
            media(alliance = "桜都字幕组3", subtitleLanguages = listOf("CHS", "CHT")),
            media(alliance = "桜都字幕组4", subtitleLanguages = listOf("CHS", "CHT", "R")),
            media(alliance = "桜都字幕组5", subtitleLanguages = listOf("CHS"))
        )
        savedDefaultPreference.value = MediaPreference(fallbackResolutions = listOf("Special", "1080P"))
        assertEquals(target, selector.trySelectDefault())
    }

    @Test
    fun `do not select default when user already selected`() = runTest {
        val target: DefaultMedia
        addMedia(
            media(alliance = "桜都字幕组1", subtitleLanguages = listOf("CHS")),
            media(alliance = "桜都字幕组2", resolution = "Special").also { target = it },
            media(alliance = "桜都字幕组3", subtitleLanguages = listOf("CHS", "CHT")),
            media(alliance = "桜都字幕组4", subtitleLanguages = listOf("CHS", "CHT", "R")),
            media(alliance = "桜都字幕组5", subtitleLanguages = listOf("CHS"))
        )
        selector.select(target)
        assertEquals(null, selector.trySelectDefault())
    }

    ///////////////////////////////////////////////////////////////////////////
    // Cached
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `always show cached even if preferences don't match`() = runTest {
        val target: DefaultMedia
        addMedia(
            media(alliance = "桜都字幕组1", subtitleLanguages = listOf("CHS")),
            media(
                alliance = "桜都字幕组2",
                subtitleLanguages = listOf("CHS"),
                kind = MediaSourceKind.LocalCache,
            ).also { target = it },
            media(alliance = "桜都字幕组3", subtitleLanguages = listOf("CHS", "CHT")),
            media(alliance = "桜都字幕组4", subtitleLanguages = listOf("CHS", "CHT", "R")),
            media(alliance = "桜都字幕组5", subtitleLanguages = listOf("CHS"))
        )
        selector.alliance.prefer("a")
        assertEquals(listOf(target), selector.filteredCandidates.first())
    }

    @Test
    fun `select cached`() = runTest {
        val target: DefaultMedia
        addMedia(
            media(alliance = "桜都字幕组1", subtitleLanguages = listOf("CHS")),
            media(
                alliance = "桜都字幕组2", resolution = "Special",
                kind = MediaSourceKind.LocalCache,
            ).also { target = it },
            media(alliance = "桜都字幕组3", subtitleLanguages = listOf("CHS", "CHT")),
            media(alliance = "桜都字幕组4", subtitleLanguages = listOf("CHS", "CHT", "R")),
            media(alliance = "桜都字幕组5", subtitleLanguages = listOf("CHS"))
        )
        assertEquals(target, selector.trySelectCached())
    }

    ///////////////////////////////////////////////////////////////////////////
    // Events
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun `do not save subtitle language when it is ambiguous`() = runTest {
        val target = media(alliance = "桜都字幕组", subtitleLanguages = listOf("CHS", "CHT"))
        addMedia(target)
        runCollectEvents {
            selector.select(target)
        }.run {
            assertEquals(1, onSelect.size)
            assertEquals(SelectEvent(target, null), onSelect.first())
            assertEquals(1, onChangePreference.size)
            assertEquals(
                // 
                MediaPreference.Empty.copy(
                    alliance = "桜都字幕组",
                    resolution = target.properties.resolution,
                    mediaSourceId = "dmhy"
                ), onChangePreference.first()
            )
        }
    }

    @Test
    fun `event select`() = runTest {
        val target = media(alliance = "桜都字幕组", subtitleLanguages = listOf("CHS"))
        addMedia(target)
        runCollectEvents {
            selector.select(target)
        }.run {
            assertEquals(1, onSelect.size)
            assertEquals(SelectEvent(target, null), onSelect.first())
            assertEquals(1, onChangePreference.size)
            assertEquals(
                MediaPreference.Empty.copy(
                    alliance = "桜都字幕组",
                    resolution = target.properties.resolution,
                    subtitleLanguageId = "CHS",
                    mediaSourceId = "dmhy"
                ), onChangePreference.first()
            )
        }
    }

    @Test
    fun `event prefer`() = runTest {
        val target = media(alliance = "桜都字幕组")
        addMedia(target)
        runCollectEvents {
            selector.alliance.prefer("桜都字幕组")
        }.run {
            assertEquals(0, onSelect.size)
            assertEquals(1, onChangePreference.size)
            assertEquals(
                MediaPreference.Empty.copy(
                    alliance = "桜都字幕组",
                ), onChangePreference.first()
            )
        }
    }

    class CollectedEvents(
        val onSelect: MutableList<SelectEvent> = mutableListOf(),
        val onChangePreference: MutableList<MediaPreference> = mutableListOf(),
    )

    private suspend fun runCollectEvents(block: suspend () -> Unit): CollectedEvents {
        return CollectedEvents().apply {
            cancellableCoroutineScope {
                launch(start = CoroutineStart.UNDISPATCHED) {
                    selector.events.onSelect.collect {
                        onSelect.add(it)
                    }
                }
                launch(start = CoroutineStart.UNDISPATCHED) {
                    selector.events.onChangePreference.collect {
                        onChangePreference.add(it)
                    }
                }
                try {
                    block()
                    yield()
                } finally {
                    cancelScope()
                }
            }
        }
    }
}

private const val SOURCE_DMHY = "dmhy"
private const val SOURCE_MIKAN = "mikan"

internal var mediaId: Int = 0
private fun media(
    sourceId: String = SOURCE_DMHY,
    resolution: String = "1080P",
    alliance: String = "桜都字幕组",
    size: FileSize = 1.megaBytes,
    publishedTime: Long = System.currentTimeMillis(),
    subtitleLanguages: List<String> = listOf(ChineseSimplified, ChineseTraditional).map { it.id },
    location: MediaSourceLocation = MediaSourceLocation.Online,
    kind: MediaSourceKind = MediaSourceKind.BitTorrent,
): DefaultMedia {
    val id = mediaId++
    return DefaultMedia(
        mediaId = "$sourceId.$id",
        mediaSourceId = sourceId,
        originalTitle = "[桜都字幕组] 孤独摇滚 $id",
        download = ResourceLocation.MagnetLink("magnet:?xt=urn:btih:$id"),
        originalUrl = "https://example.com/$id",
        publishedTime = publishedTime,
        episodeRange = EpisodeRange.single(EpisodeSort(id)),
        properties = MediaProperties(
            subtitleLanguageIds = subtitleLanguages,
            resolution = resolution,
            alliance = alliance,
            size = size,
        ),
        location = location,
        kind = kind,
    )
}
