package me.him188.ani.app.ui.settings.tabs.media.source

import kotlinx.coroutines.test.TestScope
import me.him188.ani.app.data.source.media.source.RssMediaSourceArguments
import me.him188.ani.app.ui.foundation.stateOf
import me.him188.ani.app.ui.framework.runComposeStateTest
import me.him188.ani.app.ui.settings.tabs.media.source.rss.EditRssMediaSourceState
import me.him188.ani.datasources.api.source.FactoryId
import me.him188.ani.utils.platform.Uuid
import kotlin.test.Test
import kotlin.test.assertEquals

class EditRssMediaSourceStateTest {

    private fun TestScope.createState(
        arguments: RssMediaSourceArguments = RssMediaSourceArguments.Default,
        mode: EditMediaSourceMode = EditMediaSourceMode.Add(FactoryId("RSS")),
        mediaSourceId: String = Uuid.randomString(),
    ): EditRssMediaSourceState {
        return EditRssMediaSourceState(
            stateOf(arguments),
            mode,
            mediaSourceId,
            onSave = {},
        )
    }

    @Test
    fun `searchUrl initial empty but not error`() = runComposeStateTest {
        val state = createState()
        assertEquals("", state.searchUrl)
        assertEquals(false, state.searchUrlIsError)
    }

    @Test
    fun `searchUrl is error if edited to empty`() = runComposeStateTest {
        val state = createState()
        assertEquals("", state.searchUrl)
        state.searchUrl = ""
        assertEquals(true, state.searchUrlIsError)
    }

    @Test
    fun `searchUrl is not error if edited to non-empty`() = runComposeStateTest {
        val state = createState()
        assertEquals("", state.searchUrl)
        state.searchUrl = "https://test.com"
        assertEquals(false, state.searchUrlIsError)
    }

    @Test
    fun `searchUrl is not error if edited to non-empty then empty`() = runComposeStateTest {
        val state = createState()
        assertEquals("", state.searchUrl)
        state.searchUrl = "1"
        assertEquals(false, state.searchUrlIsError)
        state.searchUrl = ""
        assertEquals(true, state.searchUrlIsError)
    }
}