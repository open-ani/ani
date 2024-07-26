package me.him188.ani.app.ui.settings.tabs.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.map
import me.him188.ani.app.data.models.preference.MediaCacheSettings
import me.him188.ani.app.data.models.preference.MediaSelectorSettings
import me.him188.ani.app.data.models.preference.VideoResolverSettings
import me.him188.ani.app.data.repository.SettingsRepository
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.tools.MonoTasker
import me.him188.ani.app.tools.torrent.TorrentManager
import me.him188.ani.app.ui.foundation.rememberViewModel
import me.him188.ani.app.ui.settings.SettingsTab
import me.him188.ani.app.ui.settings.framework.AbstractSettingsViewModel
import me.him188.ani.app.ui.settings.framework.components.SelectableItem
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaPreference
import me.him188.ani.datasources.api.topic.Resolution
import me.him188.ani.datasources.api.topic.SubtitleLanguage
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Stable
class MediaSettingsViewModel : AbstractSettingsViewModel(), KoinComponent {
    private val settingsRepository: SettingsRepository by inject()
    private val torrentManager: TorrentManager by inject()

    @Stable
    private val placeholderMediaPreference = MediaPreference.Empty.copy() // don't remove .copy, we need identity check

    val mediaSelectorSettings by settings(
        settingsRepository.mediaSelectorSettings,
        MediaSelectorSettings.Default.copy(_placeholder = -1),
    )

    val defaultMediaPreference by settingsRepository.defaultMediaPreference.flow
        .map {
            it
        }.produceState(placeholderMediaPreference)

    val defaultMediaPreferenceLoading by derivedStateOf {
        defaultMediaPreference === placeholderMediaPreference // pointer identity
    }


    val allSubtitleLanguageIds = SubtitleLanguage.matchableEntries.map { it.id }

    val allResolutionIds = Resolution.entries.map { it.id }

    val sortedLanguages by derivedStateOf {
        defaultMediaPreference.fallbackSubtitleLanguageIds.extendTo(allSubtitleLanguageIds)
    }

    val sortedResolutions by derivedStateOf {
        defaultMediaPreference.fallbackResolutions.extendTo(allResolutionIds)
    }

    /**
     * 将 [this] 扩展到 [all]，并保持顺序.
     */
    private fun List<String>?.extendTo(
        all: List<String>
    ): List<SelectableItem<String>> {
        val fallback = this ?: return all.map { SelectableItem(it, selected = true) }

        return fallback.map {
            SelectableItem(it, selected = true)
        } + (all - fallback.toSet()).map {
            SelectableItem(it, selected = false)
        }
    }


    private val defaultMediaPreferenceTasker = MonoTasker(this.backgroundScope)
    fun updateDefaultMediaPreference(copy: MediaPreference) {
        defaultMediaPreferenceTasker.launch {
            settingsRepository.defaultMediaPreference.set(copy)
        }
    }


    val mediaCacheSettings by settings(
        settingsRepository.mediaCacheSettings,
        MediaCacheSettings(placeholder = -1),
    )

    private val mediaCacheSettingsTasker = MonoTasker(this.backgroundScope)
    fun updateMediaCacheSettings(copy: MediaCacheSettings) {
        mediaCacheSettingsTasker.launch {
            settingsRepository.mediaCacheSettings.set(copy)
        }
    }
    
    val videoResolverSettings by settings(
        settingsRepository.videoResolverSettings,
        VideoResolverSettings.Default.copy(_placeholder = -1),
    )
}

@Composable
fun MediaPreferenceTab(
    vm: MediaSettingsViewModel = rememberViewModel { MediaSettingsViewModel() },
    modifier: Modifier = Modifier,
) {
    val navigator by rememberUpdatedState(LocalNavigator.current)
    SettingsTab(modifier) {
        VideoResolverGroup(vm)
        AutoCacheGroup(vm, navigator)
        TorrentEngineGroup(vm)
        CacheDirectoryGroup(vm)
        MediaSelectionGroup(vm)
    }
}
