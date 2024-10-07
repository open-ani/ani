/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.subject.episode.mediaFetch

import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import me.him188.ani.app.data.models.preference.MediaPreference
import me.him188.ani.app.data.models.preference.MediaSelectorSettings
import me.him188.ani.app.domain.media.TestMediaList
import me.him188.ani.app.domain.media.selector.DefaultMediaSelector
import me.him188.ani.app.domain.media.selector.MediaPreferenceItem
import me.him188.ani.app.domain.media.selector.MediaSelector
import me.him188.ani.app.domain.media.selector.MediaSelectorContext
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
    mediaSourceInfoProvider: MediaSourceInfoProvider,
    parentCoroutineContext: CoroutineContext,
): MediaSelectorPresentation = MediaSelectorPresentationImpl(
    mediaSelector, mediaSourceInfoProvider, parentCoroutineContext,
)

@Composable
fun rememberMediaSelectorPresentation(
    mediaSourceInfoProvider: MediaSourceInfoProvider,
    mediaSelector: () -> MediaSelector,// lambda remembered
): MediaSelectorPresentation {
    val scope = rememberBackgroundScope()
    val selector by remember {
        derivedStateOf(mediaSelector)
    }
    return remember {
        MediaSelectorPresentation(selector, mediaSourceInfoProvider, scope.backgroundScope.coroutineContext)
    }
}

/**
 * 数据源选择器 UI 的状态.
 */
@Stable
interface MediaSelectorPresentation : AutoCloseable {
    val mediaSourceInfoProvider: MediaSourceInfoProvider

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

    val groupedMediaList: List<MediaGroup>

    @Stable
    fun getGroupState(groupId: MediaGroupId): MediaGroupState

    fun bringIntoViewRequester(media: Media): State<BringIntoViewRequester?>

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
    override val mediaSourceInfoProvider: MediaSourceInfoProvider,
    parentCoroutineContext: CoroutineContext,
) : MediaSelectorPresentation, HasBackgroundScope by BackgroundScope(parentCoroutineContext) {
    override val mediaList: List<Media> by mediaSelector.mediaList.produceState(emptyList())
    override val filteredCandidates: List<Media> by mediaSelector.filteredCandidates.produceState(emptyList())

    private val groupStates: SnapshotStateMap<MediaGroupId, MediaGroupState> = SnapshotStateMap()
    override val groupedMediaList: List<MediaGroup> by derivedStateOf {
        MediaGrouper.buildGroups(filteredCandidates)
    }

    override fun getGroupState(groupId: MediaGroupId): MediaGroupState {
        return groupStates.getOrPut(groupId) {
            MediaGroupState(
                groupId,
                derivedStateOf {
                    groupedMediaList.find { it.groupId == groupId }
                },
            )
        }
    }

    private val bringIntoViewRequesters by derivedStateOf {
        mediaList.associateWith { BringIntoViewRequester() }
    }

    override val alliance: MediaPreferenceItemPresentation<String> =
        MediaPreferenceItemPresentation(mediaSelector.alliance, backgroundScope)
    override val resolution: MediaPreferenceItemPresentation<String> =
        MediaPreferenceItemPresentation(mediaSelector.resolution, backgroundScope)
    override val subtitleLanguageId: MediaPreferenceItemPresentation<String> =
        MediaPreferenceItemPresentation(mediaSelector.subtitleLanguageId, backgroundScope)
    override val mediaSource: MediaPreferenceItemPresentation<String> =
        MediaPreferenceItemPresentation(mediaSelector.mediaSourceId, backgroundScope)
    override val selected: Media? by mediaSelector.selected.produceState(null)

    override fun select(candidate: Media) {
        launchInBackground {
            mediaSelector.select(candidate)
        }
    }

    override fun bringIntoViewRequester(media: Media): State<BringIntoViewRequester?> = derivedStateOf {
        bringIntoViewRequesters.get(media)
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

@Stable
class MediaGroupState(
    val groupId: MediaGroupId,
    private val group: State<MediaGroup?>,
) {
    var selectedItem: Media? by mutableStateOf(null)
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
        createTestMediaSourceInfoProvider(),
        backgroundScope.coroutineContext,
    )

