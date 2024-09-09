package me.him188.ani.app.ui.settings.tabs.media.source

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.test.TestScope
import me.him188.ani.app.data.source.media.source.RssMediaSourceArguments
import me.him188.ani.app.ui.foundation.stateOf
import me.him188.ani.app.ui.framework.runComposeStateTest
import me.him188.ani.app.ui.settings.tabs.media.source.rss.EditRssMediaSourceState
import me.him188.ani.app.ui.settings.tabs.media.source.rss.SaveableStorage
import me.him188.ani.utils.platform.Uuid
import kotlin.test.Test
import kotlin.test.assertEquals

class EditRssMediaSourceStateTest {

    private fun TestScope.createState(
        arguments: RssMediaSourceArguments = RssMediaSourceArguments.Default,
        mediaSourceId: String = Uuid.randomString(),
    ): EditRssMediaSourceState {
        val argumentsState = mutableStateOf(arguments)
        return EditRssMediaSourceState(
            SaveableStorage(
                argumentsState,
                onSave = { argumentsState.value = it },
                isSavingState = stateOf(false),
            ),
            mediaSourceId,
        )
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