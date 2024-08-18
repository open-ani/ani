package me.him188.ani.app.ui.subject.episode.mediaFetch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import me.him188.ani.app.data.models.preference.MediaSelectorSettings
import me.him188.ani.app.data.source.media.TestMediaList
import me.him188.ani.app.data.source.media.selector.DefaultMediaSelector
import me.him188.ani.app.data.source.media.selector.MediaPreferenceItem
import me.him188.ani.app.data.source.media.selector.MediaSelector
import me.him188.ani.app.data.source.media.selector.MediaSelectorContext
import me.him188.ani.app.tools.MonoTasker
import me.him188.ani.app.ui.foundation.BackgroundScope
import me.him188.ani.app.ui.foundation.HasBackgroundScope
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.app.ui.foundation.rememberBackgroundScope
import me.him188.ani.datasources.api.Media
import me.him188.ani.utils.platform.annotations.TestOnly
import kotlin.coroutines.CoroutineContext

fun MediaSelectorPresentation(
    mediaSelector: MediaSelector,
    parentCoroutineContext: CoroutineContext,
): MediaSelectorPresentation = MediaSelectorPresentationImpl(
    mediaSelector, parentCoroutineContext,
)

@Composable
fun rememberMediaSelectorPresentation(
    mediaSelector: () -> MediaSelector // lambda remembered
): MediaSelectorPresentation {
    val scope = rememberBackgroundScope()
    val selector by remember {
        derivedStateOf(mediaSelector)
    }
    return remember {
        MediaSelectorPresentation(selector, scope.backgroundScope.coroutineContext)
    }
}

/**
 * 数据源选择器 UI 的状态.
 */
@Stable
interface MediaSelectorPresentation : AutoCloseable {
    /**
     * The list of media available for selection.
     */
    val mediaList: List<Media>

    val alliance: MediaPreferenceItemPresentation<String>
    val resolution: MediaPreferenceItemPresentation<String>
    val subtitleLanguageId: MediaPreferenceItemPresentation<String>
    val mediaSource: MediaPreferenceItemPresentation<String>

    /**
     * @see MediaSelector.filteredCandidates
     */
    val filteredCandidates: List<Media>

    /**
     * @see MediaSelector.selected
     */
    val selected: Media?

    /**
     * @see MediaSelector.select
     */
    fun select(candidate: Media)

    fun removePreferencesUntilFirstCandidate()
}

@Stable
class MediaPreferenceItemPresentation<T : Any>(
    @PublishedApi internal val item: MediaPreferenceItem<T>,
    override val backgroundScope: CoroutineScope,
) : HasBackgroundScope {
    val available: List<T> by item.available.produceState(emptyList())
    val finalSelected: T? by item.finalSelected.produceState(null)

    private val tasker = MonoTasker(backgroundScope)

    /**
     * 用户选择
     */
    fun prefer(value: T) {
        tasker.launch(start = CoroutineStart.UNDISPATCHED) { item.prefer(value) }
    }

    /**
     * 删除已有的选择
     */
    fun removePreference() {
        tasker.launch(start = CoroutineStart.UNDISPATCHED) { item.removePreference() }
    }
}

fun <T : Any> MediaPreferenceItemPresentation<T>.preferOrRemove(value: T?) {
    if (value == null || value == finalSelected) {
        removePreference()
    } else {
        prefer(value)
    }
}


/**
 * Wraps [MediaSelector] to provide states for UI.
 */
internal class MediaSelectorPresentationImpl(
    private val mediaSelector: MediaSelector,
    parentCoroutineContext: CoroutineContext,
) : MediaSelectorPresentation, HasBackgroundScope by BackgroundScope(parentCoroutineContext) {
    override val mediaList: List<Media> by mediaSelector.mediaList.produceState(emptyList())

    override val alliance: MediaPreferenceItemPresentation<String> =
        MediaPreferenceItemPresentation(mediaSelector.alliance, backgroundScope)
    override val resolution: MediaPreferenceItemPresentation<String> =
        MediaPreferenceItemPresentation(mediaSelector.resolution, backgroundScope)
    override val subtitleLanguageId: MediaPreferenceItemPresentation<String> =
        MediaPreferenceItemPresentation(mediaSelector.subtitleLanguageId, backgroundScope)
    override val mediaSource: MediaPreferenceItemPresentation<String> =
        MediaPreferenceItemPresentation(mediaSelector.mediaSourceId, backgroundScope)
    override val filteredCandidates: List<Media> by mediaSelector.filteredCandidates.produceState(emptyList())
    override val selected: Media? by mediaSelector.selected.produceState(null)

    override fun select(candidate: Media) {
        launchInBackground {
            mediaSelector.select(candidate)
        }
    }

    override fun removePreferencesUntilFirstCandidate() {
        launchInBackground {
            mediaSelector.removePreferencesUntilFirstCandidate()
        }
    }

    override fun close() {
        backgroundScope.cancel()
    }
}


///////////////////////////////////////////////////////////////////////////
// Testing
///////////////////////////////////////////////////////////////////////////

@Composable
@TestOnly
fun rememberTestMediaSelectorPresentation(): MediaSelectorPresentation {
    val backgroundScope = rememberBackgroundScope()
    return remember(backgroundScope) { createState(backgroundScope.backgroundScope) }
}

@OptIn(TestOnly::class)
private fun createState(backgroundScope: CoroutineScope) =
    MediaSelectorPresentation(
        DefaultMediaSelector(
            mediaSelectorContextNotCached = flowOf(MediaSelectorContext.EmptyForPreview),
            mediaListNotCached = MutableStateFlow(TestMediaList),
            savedUserPreference = flowOf(MediaPreference.Empty),
            savedDefaultPreference = flowOf(MediaPreference.Empty),
            mediaSelectorSettings = flowOf(MediaSelectorSettings.Default),
        ),
        backgroundScope.coroutineContext,
    )

