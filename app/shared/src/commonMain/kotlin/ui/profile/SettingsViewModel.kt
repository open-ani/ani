package me.him188.ani.app.ui.profile

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import me.him188.ani.app.data.models.preference.DanmakuSettings
import me.him188.ani.app.data.models.preference.DebugSettings
import me.him188.ani.app.data.models.preference.MediaCacheSettings
import me.him188.ani.app.data.models.preference.MediaSelectorSettings
import me.him188.ani.app.data.models.preference.ProxySettings
import me.him188.ani.app.data.models.preference.UISettings
import me.him188.ani.app.data.models.preference.UpdateSettings
import me.him188.ani.app.data.models.preference.VideoResolverSettings
import me.him188.ani.app.data.models.preference.VideoScaffoldConfig
import me.him188.ani.app.data.repository.MediaSourceInstanceRepository
import me.him188.ani.app.data.repository.SettingsRepository
import me.him188.ani.app.data.source.danmaku.AniBangumiSeverBaseUrls
import me.him188.ani.app.data.source.media.fetch.MediaSourceManager
import me.him188.ani.app.platform.PermissionManager
import me.him188.ani.app.tools.MonoTasker
import me.him188.ani.app.tools.torrent.engines.AnitorrentConfig
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.settings.framework.ConnectionTestResult
import me.him188.ani.app.ui.settings.framework.ConnectionTester
import me.him188.ani.app.ui.settings.framework.DefaultConnectionTesterRunner
import me.him188.ani.app.ui.settings.framework.SettingsState
import me.him188.ani.app.ui.settings.tabs.app.SoftwareUpdateGroupState
import me.him188.ani.app.ui.settings.tabs.media.CacheDirectoryGroupState
import me.him188.ani.app.ui.settings.tabs.media.MediaSelectionGroupState
import me.him188.ani.app.ui.settings.tabs.media.source.EditMediaSourceState
import me.him188.ani.app.ui.settings.tabs.media.source.MediaSourceGroupState
import me.him188.ani.app.ui.settings.tabs.media.source.MediaSourceLoader
import me.him188.ani.app.ui.subject.episode.mediaFetch.MediaPreference
import me.him188.ani.datasources.api.source.ConnectionStatus
import me.him188.ani.datasources.api.source.asAutoCloseable
import me.him188.ani.datasources.api.subject.SubjectProvider
import me.him188.ani.datasources.bangumi.BangumiSubjectProvider
import me.him188.ani.utils.ktor.createDefaultHttpClient
import me.him188.ani.utils.platform.Uuid
import me.him188.ani.utils.platform.currentTimeMillis
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SettingsViewModel : AbstractViewModel(), KoinComponent {
    private val settingsRepository: SettingsRepository by inject()
    private val permissionManager: PermissionManager by inject()
    private val bangumiSubjectProvider: SubjectProvider by inject()

    private val mediaSourceManager: MediaSourceManager by inject()
    private val mediaSourceInstanceRepository: MediaSourceInstanceRepository by inject()

    val softwareUpdateGroupState: SoftwareUpdateGroupState = SoftwareUpdateGroupState(
        updateSettings = settingsRepository.updateSettings.stateInBackground(UpdateSettings.Default.copy(_placeholder = -1)),
        backgroundScope,
    )

    val uiSettings: SettingsState<UISettings> =
        settingsRepository.uiSettings.stateInBackground(UISettings.Default.copy(_placeholder = -1))
    val videoScaffoldConfig: SettingsState<VideoScaffoldConfig> =
        settingsRepository.videoScaffoldConfig.stateInBackground(VideoScaffoldConfig.Default.copy(_placeholder = -1))

    val videoResolverSettingsState: SettingsState<VideoResolverSettings> =
        settingsRepository.videoResolverSettings.stateInBackground(VideoResolverSettings.Default.copy(_placeholder = -1))

    val mediaCacheSettingsState: SettingsState<MediaCacheSettings> =
        settingsRepository.mediaCacheSettings.stateInBackground(MediaCacheSettings.Default.copy(_placeholder = -1))

    val torrentSettingsState: SettingsState<AnitorrentConfig> =
        settingsRepository.anitorrentConfig.stateInBackground(AnitorrentConfig.Default.copy(_placeholder = -1))

    val cacheDirectoryGroupState = CacheDirectoryGroupState(
        mediaCacheSettingsState,
        permissionManager,
    )

    private val mediaSelectorSettingsState: SettingsState<MediaSelectorSettings> =
        settingsRepository.mediaSelectorSettings.stateInBackground(MediaSelectorSettings.Default.copy(_placeholder = -1))

    private val defaultMediaPreferenceState =
        settingsRepository.defaultMediaPreference.stateInBackground(MediaPreference.PlatformDefault.copy(_placeholder = -1))

    val mediaSelectionGroupState = MediaSelectionGroupState(
        defaultMediaPreferenceState = defaultMediaPreferenceState,
        mediaSelectorSettingsState = mediaSelectorSettingsState,
    )

    val debugSettingsState = settingsRepository.debugSettings.stateInBackground(DebugSettings(_placeHolder = -1))
    val isInDebugMode by derivedStateOf {
        debugSettingsState.value.enabled
    }


    private val httpClient = createDefaultHttpClient {
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 30_000
        }
    }.also {
        addCloseable(it.asAutoCloseable())
    }

    val proxySettingsState =
        settingsRepository.proxySettings.stateInBackground(ProxySettings.Default.copy(_placeHolder = -1))

    val danmakuSettingsState =
        settingsRepository.danmakuSettings.stateInBackground(placeholder = DanmakuSettings(_placeholder = -1))

    val danmakuServerTesters = DefaultConnectionTesterRunner(
        AniBangumiSeverBaseUrls.list.map {
            ConnectionTester(id = it) {
                httpClient.get("$it/status")
                ConnectionTestResult.SUCCESS
            }
        },
        backgroundScope,
    )


    // do not add more, check ui first.
    val otherTesters: DefaultConnectionTesterRunner<ConnectionTester> = DefaultConnectionTesterRunner(
        listOf(
            ConnectionTester(
                id = BangumiSubjectProvider.ID, // Bangumi 顺便也测一下
            ) {
                if (bangumiSubjectProvider.testConnection() == ConnectionStatus.SUCCESS) {
                    ConnectionTestResult.SUCCESS
                } else {
                    ConnectionTestResult.FAILED
                }
            },
        ),
        backgroundScope,
    )

    private val mediaSourceLoader = MediaSourceLoader(
        mediaSourceManager,
        backgroundScope.coroutineContext,
    )
    val mediaSourceGroupState = MediaSourceGroupState(
        mediaSourceLoader.mediaSourcesFlow.produceState(emptyList()),
        mediaSourceLoader.availableMediaSourceTemplates.produceState(emptyList()),
        onReorder = { mediaSourceInstanceRepository.reorder(it) },
        backgroundScope,
    )

    val editMediaSourceState = EditMediaSourceState(
        getConfigFlow = { mediaSourceManager.instanceConfigFlow(it) },
        onAdd = { factoryId, config -> mediaSourceManager.addInstance(Uuid.randomString(), factoryId, config) },
        onEdit = { instanceId, config -> mediaSourceManager.updateConfig(instanceId, config) },
        onDelete = { instanceId -> mediaSourceManager.removeInstance(instanceId) },
        onSetEnabled = { instanceId, enabled -> mediaSourceManager.setEnabled(instanceId, enabled) },
        backgroundScope,
    )


    val debugTriggerState = DebugTriggerState(debugSettingsState, backgroundScope)
}

class DebugTriggerState(
    private val debugSettingsState: SettingsState<DebugSettings>,
    backgroundScope: CoroutineScope,
) {
    private val debugTriggerRecord = ArrayDeque<Long>()

    private val tasker = MonoTasker(backgroundScope)

    val debugSettings by debugSettingsState

    init {
        debugTriggerRecord.clear()
    }

    private suspend fun clearTriggerRecord() {
        delay(5000L)
        debugTriggerRecord.clear()
    }

    fun triggerDebugMode(): Boolean {
        if (debugSettings.enabled) return false
        tasker.launch { clearTriggerRecord() }

        if (debugTriggerRecord.size == 5) {
            debugTriggerRecord.clear()
            tasker.cancel()
        }
        debugTriggerRecord.addFirst(currentTimeMillis())

        if (
            debugTriggerRecord.size == 5 &&
            debugTriggerRecord.zipWithNext().all { (prev, next) -> next - prev < 1000L }
        ) {
            debugTriggerRecord.clear()
            debugSettingsState.update(debugSettings.copy(enabled = true))
            tasker.cancel()
            return true
        }
        return false
    }
}
