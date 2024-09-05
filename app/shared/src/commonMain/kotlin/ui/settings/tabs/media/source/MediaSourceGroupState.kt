package me.him188.ani.app.ui.settings.tabs.media.source

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import me.him188.ani.app.data.source.media.fetch.MediaSourceManager
import me.him188.ani.app.data.source.media.instance.MediaSourceInstance
import me.him188.ani.app.tools.MonoTasker
import me.him188.ani.app.ui.settings.framework.ConnectionTestResult
import me.him188.ani.app.ui.settings.framework.ConnectionTester
import me.him188.ani.app.ui.settings.framework.DefaultConnectionTesterRunner
import me.him188.ani.datasources.api.source.ConnectionStatus
import me.him188.ani.datasources.api.source.FactoryId
import me.him188.ani.datasources.api.source.MediaSourceConfig
import me.him188.ani.datasources.api.source.MediaSourceFactory
import me.him188.ani.datasources.api.source.MediaSourceInfo
import me.him188.ani.datasources.api.source.parameter.MediaSourceParameters
import me.him188.ani.utils.coroutines.childScope
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds


class MediaSourceLoader(
    private val mediaSourceManager: MediaSourceManager,
    parentCoroutineContext: CoroutineContext,
) {
    private val scope = parentCoroutineContext.childScope()

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
        .shareIn(scope, started = SharingStarted.WhileSubscribed(), replay = 1)

    val availableMediaSourceTemplates = mediaSourcesFlow.map { mediaSources ->
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
    }.stateIn(scope, SharingStarted.WhileSubscribed(), emptyList())

    private fun findFactory(factoryId: FactoryId): MediaSourceFactory? {
        return mediaSourceManager.allFactories.find { it.factoryId == factoryId }
    }
}

class MediaSourceGroupState(
    mediaSourcesState: State<List<MediaSourcePresentation>>,
    availableMediaSourceTemplatesState: State<List<MediaSourceTemplate>>,
    private val onReorder: suspend (newOrder: List<String>) -> Unit,
    private val backgroundScope: CoroutineScope,
) {
    val mediaSources by mediaSourcesState
    val availableMediaSourceTemplates by availableMediaSourceTemplatesState

    val mediaSourceTesters by derivedStateOf {
        DefaultConnectionTesterRunner(
            mediaSources.map { it.connectionTester },
            backgroundScope,
        )
    }

    ///////////////////////////////////////////////////////////////////////////
    // Sorting media source
    ///////////////////////////////////////////////////////////////////////////

    var isCompletingReorder by mutableStateOf(false)
        private set

    private val reorderTasker = MonoTasker(backgroundScope)
    fun reorderMediaSources(newOrder: List<String>) {
        reorderTasker.launch {
            isCompletingReorder = true
            try {
                onReorder(newOrder)
            } finally {
                delay(0.5.seconds)
                isCompletingReorder = false
            }
        }
    }
}

class EditMediaSourceState(
    private val getConfigFlow: (instanceId: String) -> Flow<MediaSourceConfig>,
    private val onAdd: suspend (factoryId: FactoryId, config: MediaSourceConfig) -> Unit,
    private val onEdit: suspend (instanceId: String, config: MediaSourceConfig) -> Unit,
    private val onDelete: suspend (instanceId: String) -> Unit,
    private val onSetEnabled: suspend (instanceId: String, enabled: Boolean) -> Unit,
    private val backgroundScope: CoroutineScope,
) {
    var editMediaSourceState by mutableStateOf<EditingMediaSource?>(null)
        private set

    fun startAdding(template: MediaSourceTemplate): EditingMediaSource {
        cancelEdit()
        val state = EditingMediaSource(
            editingMediaSourceId = null,
            factoryId = template.factoryId,
            info = template.info,
            parameters = template.parameters,
            persistedArguments = flowOf(MediaSourceConfig()),
            editMediaSourceMode = EditMediaSourceMode.Add,
            onSave = { confirmEdit(it) },
            backgroundScope.coroutineContext, // TODO: this can be a memory leak
        )
        editMediaSourceState = state
        return state
    }

    fun startEditing(presentation: MediaSourcePresentation) {
        cancelEdit()
        editMediaSourceState = EditingMediaSource(
            editingMediaSourceId = presentation.mediaSourceId,
            factoryId = presentation.factoryId,
            info = presentation.info,
            parameters = presentation.parameters,
            persistedArguments = getConfigFlow(presentation.instanceId),
            editMediaSourceMode = EditMediaSourceMode.Edit(presentation.instanceId),
            onSave = {
                editTasker.launch {
                    confirmEditImpl(it)
                }.join()
            },
            backgroundScope.coroutineContext, // TODO: this can be a memory leak
        )
    }

    private val editTasker = MonoTasker(backgroundScope)

    fun confirmEdit(state: EditingMediaSource) {
        editTasker.launch {
            confirmEditImpl(state)
        }
    }

    private suspend fun confirmEditImpl(state: EditingMediaSource) {
        when (state.editMediaSourceMode) {
            EditMediaSourceMode.Add -> {
                onAdd(
                    state.factoryId,
                    state.createConfig(),
                )
                withContext(Dispatchers.Main) { cancelEdit() }
            }

            is EditMediaSourceMode.Edit -> {
                onEdit(
                    state.editMediaSourceMode.instanceId,
                    state.createConfig(),
                )
                withContext(Dispatchers.Main) { cancelEdit() }
            }
        }
    }

    fun cancelEdit() {
        editMediaSourceState?.close()
        editMediaSourceState = null
    }

    fun deleteMediaSource(item: MediaSourcePresentation) {
        editTasker.launch {
            onDelete(item.instanceId)
        }
    }

    fun toggleMediaSourceEnabled(item: MediaSourcePresentation, enabled: Boolean) {
        editTasker.launch {
            onSetEnabled(item.instanceId, enabled)
        }
    }
}


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

fun EditingMediaSource.createConfig(): MediaSourceConfig {
    return MediaSourceConfig(
        arguments = arguments.associate { it.name to it.toPersisted() },
    )
}
