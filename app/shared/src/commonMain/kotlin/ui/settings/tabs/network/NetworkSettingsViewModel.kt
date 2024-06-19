package me.him188.ani.app.ui.settings.tabs.network

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import me.him188.ani.app.data.media.MediaSourceManager
import me.him188.ani.app.data.media.instance.MediaSourceInstance
import me.him188.ani.app.data.models.DanmakuSettings
import me.him188.ani.app.data.models.ProxySettings
import me.him188.ani.app.data.repositories.MediaSourceInstanceRepository
import me.him188.ani.app.data.repositories.SettingsRepository
import me.him188.ani.app.ui.foundation.feedback.ErrorMessage
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.app.ui.icons.renderMediaSource
import me.him188.ani.app.ui.icons.renderMediaSourceDescription
import me.him188.ani.app.ui.settings.framework.AbstractSettingsViewModel
import me.him188.ani.app.ui.settings.framework.ConnectionTestResult
import me.him188.ani.app.ui.settings.framework.ConnectionTester
import me.him188.ani.danmaku.ani.client.AniBangumiSeverBaseUrls
import me.him188.ani.datasources.api.source.ConnectionStatus
import me.him188.ani.datasources.api.source.MediaSourceConfig
import me.him188.ani.datasources.api.source.MediaSourceFactory
import me.him188.ani.datasources.api.source.MediaSourceParameters
import me.him188.ani.datasources.api.subject.SubjectProvider
import me.him188.ani.datasources.bangumi.BangumiSubjectProvider
import me.him188.ani.datasources.ikaros.IkarosMediaSource
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
    val info: MediaSourceInfo,
    val connectionTester: ConnectionTester,
    val instance: MediaSourceInstance
)

val MediaSourcePresentation.mediaSourceId get() = info.mediaSourceId

@Immutable
class MediaSourceInfo(
    val mediaSourceId: String,
    val name: String,
    val description: String? = null, // localized
    val iconUrl: String? = null,
    val website: String? = null,
    val parameters: MediaSourceParameters,
)

/**
 * 对应一个 Factory
 */
@Immutable
class MediaSourceTemplate(
    val info: MediaSourceInfo,
)

val MediaSourceTemplate.mediaSourceId get() = info.mediaSourceId


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
                val factory = findFactory(instance.source.mediaSourceId) ?: return@mapNotNull null
                MediaSourcePresentation(
                    instanceId = instance.instanceId,
                    isEnabled = instance.isEnabled,
                    info = MediaSourceInfo(
                        mediaSourceId = instance.source.mediaSourceId,
                        name = renderMediaSource(instance.source.mediaSourceId),
                        description = renderMediaSourceDescription(instance.source.mediaSourceId),
                        iconUrl = null,
                        website = null,
                        parameters = factory.parameters,
                    ),
                    connectionTester = ConnectionTester(
                        id = instance.mediaSourceId,
                        testConnection = {
                            when (instance.source.checkConnection()) {
                                ConnectionStatus.SUCCESS -> ConnectionTestResult.SUCCESS
                                ConnectionStatus.FAILED -> ConnectionTestResult.FAILED
                            }
                        }
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
            if (!factory.allowMultipleInstances && mediaSources.any { it.mediaSourceId == factory.mediaSourceId }) {
                return@mapNotNull null
            }
            MediaSourceTemplate(
                MediaSourceInfo(
                    mediaSourceId = factory.mediaSourceId,
                    name = renderMediaSource(factory.mediaSourceId),
                    description = renderMediaSourceDescription(factory.mediaSourceId),
                    iconUrl = if (factory.mediaSourceId == IkarosMediaSource.ID)
                        "https://docs.ikaros.run/logo.png" else null, // TODO: properly configure icon
                    website = null,
                    parameters = factory.parameters,
                ),
            )
        }
    }.produceState(emptyList())

    val mediaSourceTesters by derivedStateOf {
        Testers(
            mediaSources.map { it.connectionTester },
            backgroundScope
        )
    }

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
            }
        ),
        backgroundScope
    )

    ///////////////////////////////////////////////////////////////////////////
    // Editing media source
    ///////////////////////////////////////////////////////////////////////////

    var editMediaSourceState by mutableStateOf<EditMediaSourceState?>(null)
        private set

    fun startAdding(template: MediaSourceTemplate): EditMediaSourceState {
        cancelEdit()
        val state = EditMediaSourceState(
            info = template.info,
            persistedArguments = flowOf(MediaSourceConfig.Default),
            editType = EditType.Add,
            backgroundScope.coroutineContext,
        )
        editMediaSourceState = state
        return state
    }

    fun startEditing(presentation: MediaSourcePresentation) {
        cancelEdit()
        editMediaSourceState = EditMediaSourceState(
            info = presentation.info,
            persistedArguments = mediaSourceManager.instanceConfigFlow(presentation.instanceId),
            editType = EditType.Edit(presentation.instanceId),
            backgroundScope.coroutineContext,
        )
    }

    var savingError = MutableStateFlow<ErrorMessage?>(null)

    fun confirmEdit(state: EditMediaSourceState) {
        when (state.editType) {
            EditType.Add -> {
                savingError.value = ErrorMessage.processing("正在添加")
                launchInBackground {
                    try {
                        mediaSourceManager.addInstance(
                            state.info.mediaSourceId,
                            state.createConfig(),
                        )
                        savingError.value = null
                        withContext(Dispatchers.Main) { cancelEdit() }
                    } catch (e: Throwable) {
                        savingError.value = ErrorMessage.simple("添加失败", cause = e)
                        return@launchInBackground
                    }
                }
            }

            is EditType.Edit -> {
                savingError.value = ErrorMessage.processing("正在保存")
                launchInBackground {
                    try {
                        mediaSourceManager.updateConfig(
                            state.editType.instanceId,
                            state.createConfig(),
                        )
                        savingError.value = null
                        withContext(Dispatchers.Main) { cancelEdit() }
                    } catch (e: Throwable) {
                        savingError.value = ErrorMessage.simple("保存失败", cause = e)
                        return@launchInBackground
                    }
                }
            }
        }
    }

    fun cancelEdit() {
        editMediaSourceState?.close()
        editMediaSourceState = null
    }

    private fun findFactory(mediaSourceId: String): MediaSourceFactory? {
        return mediaSourceManager.allFactories.find { it.mediaSourceId == mediaSourceId }
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
        placeholder = ProxySettings(_placeHolder = -1)
    )

    ///////////////////////////////////////////////////////////////////////////
    // DanmakuSettings
    ///////////////////////////////////////////////////////////////////////////

    val danmakuSettings by settings(
        settingsRepository.danmakuSettings,
        placeholder = DanmakuSettings(_placeholder = -1)
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
        backgroundScope
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
        arguments = arguments.associate { it.name to it.toPersisted() }
    )
}