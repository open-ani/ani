@file:Suppress("CanBeParameter", "MemberVisibilityCanBePrivate")

package me.him188.ani.app.data.media.framework

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import me.him188.ani.app.data.media.selector.DefaultMediaSelector
import me.him188.ani.app.data.media.selector.MediaPreferenceItem
import me.him188.ani.app.data.media.selector.MediaSelector
import me.him188.ani.app.data.media.selector.MediaSelectorEvents
import me.him188.ani.app.data.media.selector.MutableMediaSelectorEvents
import me.him188.ani.app.data.media.selector.OptionalPreference
import me.him188.ani.app.data.media.selector.orElse
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaPreference
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.topic.Resolution
import me.him188.ani.datasources.api.topic.SubtitleLanguage.ChineseSimplified
import me.him188.ani.datasources.api.topic.SubtitleLanguage.ChineseTraditional

class TestMediaPreferenceItem<T : Any>(
    override val available: MutableStateFlow<List<T>> = MutableStateFlow(emptyList()),
    override val userSelected: MutableStateFlow<OptionalPreference<T>> = MutableStateFlow(OptionalPreference.noPreference()),
    override val defaultSelected: MutableStateFlow<T?> = MutableStateFlow(null),
) : MediaPreferenceItem<T> {
    override val finalSelected: Flow<T?> = combine(userSelected, defaultSelected) { user, default ->
        user.orElse { default }
    }

    override suspend fun prefer(value: T) {
        userSelected.value = OptionalPreference.prefer(value)
    }

    override suspend fun removePreference() {
        userSelected.value = OptionalPreference.noPreference()
    }
}

open class TestMediaSelector(
    final override val mediaList: Flow<List<Media>>,
    val defaultPreference: MutableStateFlow<MediaPreference> = MutableStateFlow(
        MediaPreference.Empty.copy(
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
        ),
    ),
) : MediaSelector {
    final override val alliance: TestMediaPreferenceItem<String> = TestMediaPreferenceItem()
    final override val resolution: TestMediaPreferenceItem<String> = TestMediaPreferenceItem()
    final override val subtitleLanguageId: TestMediaPreferenceItem<String> = TestMediaPreferenceItem()
    final override val mediaSourceId: TestMediaPreferenceItem<String> = TestMediaPreferenceItem()

    private val mergedPreference = combine(
        defaultPreference,
        alliance.finalSelected,
        resolution.finalSelected,
        subtitleLanguageId.finalSelected,
        mediaSourceId.finalSelected,
    ) { default, alliance, resolution, subtitleLanguage, mediaSourceId ->
        default.copy(
            alliance = alliance,
            resolution = resolution,
            subtitleLanguageId = subtitleLanguage,
            mediaSourceId = mediaSourceId,
        )
    }

    final override val filteredCandidates: Flow<List<Media>> =
        combine(mediaList, mergedPreference) { mediaList, preference ->
            DefaultMediaSelector.filterCandidates(mediaList, preference)
        }

    final override val selected: MutableStateFlow<Media?> = MutableStateFlow(null)
    final override val events: MediaSelectorEvents = MutableMediaSelectorEvents()

    override suspend fun select(candidate: Media): Boolean {
        if (this.selected.value == candidate) return false
        this.selected.value = candidate
        return true
    }

    override fun unselect() {
        this.selected.value = null
    }

    override suspend fun trySelectDefault(): Media? {
        throw UnsupportedOperationException()
    }

    override suspend fun trySelectCached(): Media? {
        throw UnsupportedOperationException()
    }

    override suspend fun removePreferencesUntilFirstCandidate() {
        throw UnsupportedOperationException()
    }
}
