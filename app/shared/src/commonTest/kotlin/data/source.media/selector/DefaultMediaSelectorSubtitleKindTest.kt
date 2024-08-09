package me.him188.ani.app.data.source.media.selector

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import me.him188.ani.app.platform.Arch
import me.him188.ani.app.platform.Platform
import me.him188.ani.datasources.api.DefaultMedia
import me.him188.ani.datasources.api.SubtitleKind
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * @see SubtitleKind
 */
class DefaultMediaSelectorSubtitleKindTest : AbstractDefaultMediaSelectorTest() {
    @Test
    fun `AllNormal does not hide`() = runTest {
        setSubtitlePreferences(MediaSelectorSubtitlePreferences.AllNormal)
        val target: DefaultMedia
        addMedia(
            media(alliance = "字幕组1").also { target = it },
            media(alliance = "字幕组2"),
            media(alliance = "字幕组3"),
            media(alliance = "字幕组4"),
            media(alliance = "字幕组5"),
        )
        savedDefaultPreference.value = DEFAULT_PREFERENCE
        assertEquals(target, selector.trySelectDefault())
    }

    @Test
    fun `does not select hidden`() = runTest {
        setSubtitlePreference(SubtitleKind.CLOSED, SubtitleKindPreference.HIDE)
        val target: DefaultMedia
        addMedia(
            media(alliance = "字幕组1", subtitleKind = SubtitleKind.CLOSED),
            media(alliance = "字幕组2").also { target = it },
            media(alliance = "字幕组3"),
            media(alliance = "字幕组4"),
            media(alliance = "字幕组5"),
        )
        savedDefaultPreference.value = DEFAULT_PREFERENCE
        assertEquals(target, selector.trySelectDefault())
    }

    @Test
    fun `low priority ones are selected last`() = runTest {
        setSubtitlePreference(SubtitleKind.CLOSED, SubtitleKindPreference.LOW_PRIORITY)
        val target: DefaultMedia
        addMedia(
            media(alliance = "字幕组1", subtitleKind = SubtitleKind.CLOSED),
            media(alliance = "字幕组2").also { target = it },
            media(alliance = "字幕组3"),
            media(alliance = "字幕组4"),
            media(alliance = "字幕组5"),
        )
        savedDefaultPreference.value = DEFAULT_PREFERENCE
        assertEquals(target, selector.trySelectDefault())
    }

    @Test
    fun `hidden items are not in mediaList`() = runTest {
        setSubtitlePreference(SubtitleKind.CLOSED, SubtitleKindPreference.HIDE)
        addMedia(
            media(alliance = "字幕组1", subtitleKind = SubtitleKind.CLOSED),
            media(alliance = "字幕组2"),
            media(alliance = "字幕组3"),
            media(alliance = "字幕组4"),
            media(alliance = "字幕组5"),
        )
        assertEquals(4, selector.mediaList.first().size)
    }

    @Test
    fun `hidden items are not in filteredCandidates`() = runTest {
        setSubtitlePreference(SubtitleKind.CLOSED, SubtitleKindPreference.HIDE)
        addMedia(
            media(alliance = "字幕组1", subtitleKind = SubtitleKind.CLOSED),
            media(alliance = "字幕组2"),
            media(alliance = "字幕组3"),
            media(alliance = "字幕组4"),
            media(alliance = "字幕组5"),
        )
        assertEquals(4, selector.filteredCandidates.first().size)
    }
}

/**
 * 测试各个平台的默认设置
 *
 * @see SubtitleKind
 */
sealed class DefaultMediaSelectorSubtitleKindPlatformTest(
    platform: Platform
) : AbstractDefaultMediaSelectorTest() {
    class MacOS : DefaultMediaSelectorSubtitleKindPlatformTest(Platform.MacOS(Arch.AARCH64)) {
        @Test
        fun `does not select CLOSED_OR_EXTERNAL_DISCOVER`() = runTest {
            val target: DefaultMedia
            addMedia(
                media(alliance = "字幕组1", subtitleKind = SubtitleKind.CLOSED_OR_EXTERNAL_DISCOVER),
                media(alliance = "字幕组2").also { target = it },
                media(alliance = "字幕组3"),
                media(alliance = "字幕组4"),
                media(alliance = "字幕组5"),
            )
            savedDefaultPreference.value = DEFAULT_PREFERENCE
            assertEquals(target, selector.trySelectDefault())
        }

        @Test
        fun `does not select CLOSED`() = runTest {
            val target: DefaultMedia
            addMedia(
                media(alliance = "字幕组1", subtitleKind = SubtitleKind.CLOSED),
                media(alliance = "字幕组2").also { target = it },
                media(alliance = "字幕组3"),
                media(alliance = "字幕组4"),
                media(alliance = "字幕组5"),
            )
            savedDefaultPreference.value = DEFAULT_PREFERENCE
            assertEquals(target, selector.trySelectDefault())
        }
    }

    class Windows : DefaultMediaSelectorSubtitleKindPlatformTest(Platform.Windows(Arch.X86_64))
    class Android : DefaultMediaSelectorSubtitleKindPlatformTest(Platform.Android) {
        @Test
        fun `CLOSED is low priority`() = runTest {
            val target: DefaultMedia
            addMedia(
                media(alliance = "字幕组1", subtitleKind = SubtitleKind.CLOSED),
                media(alliance = "字幕组2").also { target = it },
            )
            savedDefaultPreference.value = DEFAULT_PREFERENCE
            assertEquals(target, selector.trySelectDefault())
        }
    }

    // TODO: Test DefaultMediaSelectorSubtitleKindPlatformTest for iOS

    init {
        setSubtitlePreferences(MediaSelectorSubtitlePreferences.forPlatform(platform))
    }

    @Test
    fun `does not select EXTERNAL_DISCOVER`() = runTest {
        val target: DefaultMedia
        addMedia(
            media(alliance = "字幕组1", subtitleKind = SubtitleKind.EXTERNAL_DISCOVER),
            media(alliance = "字幕组2").also { target = it },
            media(alliance = "字幕组3"),
            media(alliance = "字幕组4"),
            media(alliance = "字幕组5"),
        )
        savedDefaultPreference.value = DEFAULT_PREFERENCE
        assertEquals(target, selector.trySelectDefault())
    }
}
