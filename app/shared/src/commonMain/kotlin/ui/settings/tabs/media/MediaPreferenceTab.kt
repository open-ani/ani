package me.him188.ani.app.ui.settings.tabs.media

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.him188.ani.app.data.models.preference.MediaCacheSettings
import me.him188.ani.app.data.models.preference.VideoResolverSettings
import me.him188.ani.app.tools.torrent.engines.AnitorrentConfig
import me.him188.ani.app.ui.settings.SettingsTab
import me.him188.ani.app.ui.settings.framework.SettingsState

@Composable
fun MediaPreferenceTab(
    videoResolverSettingsState: SettingsState<VideoResolverSettings>,
    mediaCacheSettingsState: SettingsState<MediaCacheSettings>,
    torrentSettingsState: SettingsState<AnitorrentConfig>,
    cacheDirectoryGroupState: CacheDirectoryGroupState,
    mediaSelectorGroupState: MediaSelectionGroupState,
    modifier: Modifier = Modifier,
) {
    SettingsTab(modifier) {
        VideoResolverGroup(videoResolverSettingsState)
        AutoCacheGroup(mediaCacheSettingsState)

        TorrentEngineGroup(torrentSettingsState)
        CacheDirectoryGroup(cacheDirectoryGroupState)
        MediaSelectionGroup(mediaSelectorGroupState)
    }
}
