package me.him188.ani.app.ui.settings.tabs.network

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import me.him188.ani.app.data.models.preference.DanmakuSettings
import me.him188.ani.app.data.models.preference.ProxySettings
import me.him188.ani.app.data.repository.MediaSourceInstanceRepository
import me.him188.ani.app.data.repository.SettingsRepository
import me.him188.ani.app.data.source.danmaku.AniBangumiSeverBaseUrls
import me.him188.ani.app.data.source.media.fetch.MediaSourceManager
import me.him188.ani.app.data.source.media.instance.MediaSourceInstance
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.app.ui.settings.framework.AbstractSettingsViewModel
import me.him188.ani.app.ui.settings.framework.ConnectionTestResult
import me.him188.ani.app.ui.settings.framework.ConnectionTester
import me.him188.ani.datasources.api.source.ConnectionStatus
import me.him188.ani.datasources.api.source.FactoryId
import me.him188.ani.datasources.api.source.MediaSourceConfig
import me.him188.ani.datasources.api.source.MediaSourceFactory
import me.him188.ani.datasources.api.source.MediaSourceInfo
import me.him188.ani.datasources.api.source.asAutoCloseable
import me.him188.ani.datasources.api.source.parameter.MediaSourceParameters
import me.him188.ani.datasources.api.subject.SubjectProvider
import me.him188.ani.datasources.bangumi.BangumiSubjectProvider
import me.him188.ani.utils.ktor.createDefaultHttpClient
import me.him188.ani.utils.platform.Uuid
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.seconds

/**
 * @see MediaSourceInstance
 */
@Stable
class MediaSourcePresentation(
    val instanceId: String,
    val isEnabled: Boolean,
    val mediaSourceId: String,
    val factoryId: FactoryId,
    val info: MediaSourceInfo,
    val parameters: MediaSourceParameters,
    val connectionTester: ConnectionTester,
    val instance: MediaSourceInstance,
)

/**
 * 对应一个 Factory
 */
@Immutable
class MediaSourceTemplate(
    val factoryId: FactoryId,
    val info: MediaSourceInfo,
    val parameters: MediaSourceParameters
)

@Stable
class NetworkSettingsViewModel : AbstractSettingsViewModel(), KoinComponent {
    private val settingsRepository: SettingsRepository by inject()
    private val mediaSourceManager: MediaSourceManager by inject()
    private val mediaSourceInstanceRepository by inject<MediaSourceInstanceRepository>()
    private val bangumiSubjectProvider by inject<SubjectProvider>()

    ///////////////////////////////////////////////////////////////////////////
    // Media Testing
    ///////////////////////////////////////////////////////////////////////////

    val mediaSourcesFlow = mediaSourceManager.allInstances
        .map { instances ->
            instances.mapNotNull { instance ->
                val factory = findFactory(instance.factoryId) ?: return@mapNotNull null
                MediaSourcePresentation(
                    instanceId = instance.instanceId,
                    isEnabled = instance.isEnabled,
                    mediaSourceId = instance.source.mediaSourceId,
                    factoryId = instance.factoryId,
                    info = instance.source.info,
                    parameters = factory.parameters,
                    connectionTester = ConnectionTester(
                        id = instance.mediaSourceId,
                        testConnection = {
                            when (instance.source.checkConnection()) {
                                ConnectionStatus.SUCCESS -> ConnectionTestResult.SUCCESS
                                ConnectionStatus.FAILED -> ConnectionTestResult.FAILED
                            }
                        },
                    ),
                    instance,
                )
            }
            // 不能 sort, 会用来 reorder
        }
        .stateInBackground(emptyList())

    val mediaSources by mediaSourcesFlow.produceState()

    val availableMediaSourceTemplates by mediaSourcesFlow.map { mediaSources ->
        mediaSourceManager.allFactories.mapNotNull { factory ->
            if (!factory.allowMultipleInstances && mediaSources.any { it.factoryId == factory.factoryId }) {
                return@mapNotNull null
            }
            MediaSourceTemplate(
                factoryId = factory.factoryId,
                info = factory.info,
                parameters = factory.parameters,
            )
        }
    }.produceState(emptyList())

    val mediaSourceTesters by derivedStateOf {
        Testers(
            mediaSources.map { it.connectionTester },
            backgroundScope,
        )
    }

    // do not add more, check ui first.
    val otherTesters = Testers(
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

    ///////////////////////////////////////////////////////////////////////////
    // Editing media source
    ///////////////////////////////////////////////////////////////////////////

    var editMediaSourceState by mutableStateOf<EditMediaSourceState?>(null)
        private set

    fun startAdding(template: MediaSourceTemplate): EditMediaSourceState {
        cancelEdit()
        val state = EditMediaSourceState(
            editingMediaSourceId = null,
            factoryId = template.factoryId,
            info = template.info,
            parameters = template.parameters,
            persistedArguments = flowOf(MediaSourceConfig()),
            editType = EditType.Add,
            backgroundScope.coroutineContext,
        )
        editMediaSourceState = state
        return state
    }

    fun startEditing(presentation: MediaSourcePresentation) {
        cancelEdit()
        editMediaSourceState = EditMediaSourceState(
            editingMediaSourceId = presentation.mediaSourceId,
            factoryId = presentation.factoryId,
            info = presentation.info,
            parameters = presentation.parameters,
            persistedArguments = mediaSourceManager.instanceConfigFlow(presentation.instanceId),
            editType = EditType.Edit(presentation.instanceId),
            backgroundScope.coroutineContext,
        )
    }

    fun confirmEdit(state: EditMediaSourceState) {
        when (state.editType) {
            EditType.Add -> {
                launchInBackground {
                    mediaSourceManager.addInstance(
                        Uuid.randomString(),
                        state.factoryId,
                        state.createConfig(),
                    )
                    withContext(Dispatchers.Main) { cancelEdit() }
                }
            }

            is EditType.Edit -> {
                launchInBackground {
                    mediaSourceManager.updateConfig(
                        state.editType.instanceId,
                        state.createConfig(),
                    )
                    withContext(Dispatchers.Main) { cancelEdit() }
                }
            }
        }
    }

    fun cancelEdit() {
        editMediaSourceState?.close()
        editMediaSourceState = null
    }

    private fun findFactory(factoryId: FactoryId): MediaSourceFactory? {
        return mediaSourceManager.allFactories.find { it.factoryId == factoryId }
    }

    fun deleteMediaSource(item: MediaSourcePresentation) {
        launchInBackground {
            mediaSourceManager.removeInstance(item.instanceId)
        }
    }

    fun toggleMediaSourceEnabled(item: MediaSourcePresentation, enabled: Boolean) {
        launchInBackground {
            mediaSourceManager.setEnabled(item.instanceId, enabled)
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    // Sorting media source
    ///////////////////////////////////////////////////////////////////////////

    var isCompletingReorder by mutableStateOf(false)
        private set

    fun reorderMediaSources(newOrder: List<String>) {
        launchInBackground {
            isCompletingReorder = true
            try {
                mediaSourceInstanceRepository.reorder(newOrder)
            } finally {
                delay(0.5.seconds)
                isCompletingReorder = false
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Proxy
    ///////////////////////////////////////////////////////////////////////////

    val proxySettings by settings(
        settingsRepository.proxySettings,
        placeholder = ProxySettings(_placeHolder = -1),
    )

    ///////////////////////////////////////////////////////////////////////////
    // DanmakuSettings
    ///////////////////////////////////////////////////////////////////////////

    private val httpClient = createDefaultHttpClient {
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 30_000
        }
    }.also {
        addCloseable(it.asAutoCloseable())
    }

    val danmakuSettings by settings(
        settingsRepository.danmakuSettings,
        placeholder = DanmakuSettings(_placeholder = -1),
    )

    val danmakuServerTesters = Testers(
        AniBangumiSeverBaseUrls.list.map {
            ConnectionTester(
                id = it,
            ) {
                httpClient.get("$it/status")
                ConnectionTestResult.SUCCESS
            }
        },
        backgroundScope,
    )

//    private val placeholderDanmakuSettings = DanmakuSettings.Default.copy()
//    val danmakuSettings by preferencesRepository.danmakuSettings.flow
//        .produceState(placeholderDanmakuSettings)
//    val danmakuSettingsLoaded by derivedStateOf {
//        danmakuSettings != placeholderDanmakuSettings
//    }
//
//    private val danmakuSettingsUpdater = MonoTasker(backgroundScope)
//    fun updateDanmakuSettings(settings: DanmakuSettings) {
//        danmakuSettingsUpdater.launch {
//            logger.info { "Updating danmaku settings: $settings" }
//            preferencesRepository.danmakuSettings.set(settings)
//        }
//    }
}

fun EditMediaSourceState.createConfig(): MediaSourceConfig {
    return MediaSourceConfig(
        arguments = arguments.associate { it.name to it.toPersisted() },
    )
}