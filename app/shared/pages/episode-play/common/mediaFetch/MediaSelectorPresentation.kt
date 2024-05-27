package me.him188.ani.app.ui.subject.episode.mediaFetch

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import kotlinx.coroutines.CoroutineScope
import me.him188.ani.app.data.media.selector.MediaPreferenceItem
import me.him188.ani.app.data.media.selector.MediaSelector
import me.him188.ani.app.ui.foundation.HasBackgroundScope
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.source.MediaSourceLocation

fun MediaSelectorPresentation(
    mediaSelector: MediaSelector,
    backgroundScope: CoroutineScope,
): MediaSelectorPresentation = MediaSelectorPresentationImpl(
    mediaSelector, backgroundScope
)

/**
 * 数据源选择器 UI 的状态.
 */
@Stable
interface MediaSelectorPresentation {
    val mediaSelector: MediaSelector

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
}

@Stable
class MediaPreferenceItemPresentation<T : Any>(
    private val item: MediaPreferenceItem<T>,
    override val backgroundScope: CoroutineScope,
) : HasBackgroundScope {
    val available: List<T> by item.available.produceState(emptyList())
    val finalSelected: T? by item.finalSelected.produceState(null)

    /**
     * 用户选择
     */
    fun prefer(value: T) = item.prefer(value)

    /**
     * 删除已有的选择
     */
    fun removePreference() = item.removePreference()
}

fun <T : Any> MediaPreferenceItemPresentation<T>.preferOrRemove(value: T?) {
    if (value == null || value == finalSelected) {
        removePreference()
    } else {
        prefer(value)
    }
}


internal class MediaSelectorPresentationImpl(
    override val mediaSelector: MediaSelector,
    override val backgroundScope: CoroutineScope,
) : MediaSelectorPresentation, HasBackgroundScope {
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
}

val Media.costForDownload
    get() = when (location) {
        MediaSourceLocation.Local -> 0
        MediaSourceLocation.Lan -> 1
        else -> 2
    }
