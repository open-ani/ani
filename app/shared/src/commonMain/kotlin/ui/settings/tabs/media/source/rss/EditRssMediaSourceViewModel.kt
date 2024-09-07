package me.him188.ani.app.ui.settings.tabs.media.source.rss

import androidx.compose.runtime.Stable
import io.ktor.client.plugins.BrowserUserAgent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformLatest
import me.him188.ani.app.data.repository.SettingsRepository
import me.him188.ani.app.data.source.media.fetch.MediaSourceManager
import me.him188.ani.app.data.source.media.fetch.toClientProxyConfig
import me.him188.ani.app.data.source.media.fetch.updateMediaSourceArguments
import me.him188.ani.app.data.source.media.source.DefaultRssMediaSourceEngine
import me.him188.ani.app.data.source.media.source.RssMediaSourceArguments
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.settings.tabs.media.source.EditMediaSourceMode
import me.him188.ani.app.ui.settings.tabs.media.source.rss.test.RssTestPaneState
import me.him188.ani.datasources.api.source.HttpMediaSource
import me.him188.ani.datasources.api.source.createHttpClient
import me.him188.ani.datasources.api.source.deserializeArgumentsOrNull
import me.him188.ani.utils.coroutines.onReplacement
import me.him188.ani.utils.ktor.proxy
import me.him188.ani.utils.platform.Uuid
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Stable
class EditRssMediaSourceViewModel(
    initialMode: EditMediaSourceMode
) : AbstractViewModel(), KoinComponent {
    private val mediaSourceManager: MediaSourceManager by inject()
    private val settingsRepository: SettingsRepository by inject()

    private val mode: MutableStateFlow<EditMediaSourceMode> = MutableStateFlow(initialMode)

    val onSave: suspend (String, RssMediaSourceArguments) -> Unit = { instanceId, arguments ->
        mediaSourceManager.updateMediaSourceArguments(
            instanceId,
            RssMediaSourceArguments.serializer(),
            arguments,
        )
    }

    private val arguments = mode.transformLatest { mode ->
        when (mode) {
            EditMediaSourceMode.Add -> emit(RssMediaSourceArguments.Default)
            is EditMediaSourceMode.Edit -> {
                emitAll(
                    mediaSourceManager.instanceConfigFlow(mode.instanceId).map {
                        it.deserializeArgumentsOrNull(
                            RssMediaSourceArguments.serializer(),
                        ) ?: RssMediaSourceArguments.Default
                    },
                )
            }
        }
    }

    val state: Flow<EditRssMediaSourceState> = mode.transformLatest { mode ->
        when (mode) {
            EditMediaSourceMode.Add -> {
                val instanceId = Uuid.randomString()
                emit(
                    EditRssMediaSourceState(
                        arguments = RssMediaSourceArguments.Default,
                        editMediaSourceMode = mode,
                        instanceId = instanceId,
                        onSave = { onSave(instanceId, it) },
                        backgroundScope,
                    ),
                )
            }

            is EditMediaSourceMode.Edit -> {
                val instanceId = mode.instanceId
                emitAll(
                    mediaSourceManager.instanceConfigFlow(instanceId).map { config ->
                        EditRssMediaSourceState(
                            arguments = config.deserializeArgumentsOrNull(RssMediaSourceArguments.serializer())
                                ?: RssMediaSourceArguments.Default,
                            editMediaSourceMode = mode,
                            instanceId = instanceId,
                            onSave = { onSave(instanceId, it) },
                            backgroundScope,
                        )
                    },
                )
            }
        }
    }

    private val client = settingsRepository.proxySettings.flow.map {
        HttpMediaSource.createHttpClient {
            proxy(it.default.toClientProxyConfig())
            BrowserUserAgent()
        }
    }.onReplacement {
        it.close()
    }.shareInBackground(started = SharingStarted.Lazily)

    val testState: RssTestPaneState = RssTestPaneState(
        searchUrlState = arguments.map { it.searchUrl }.debounce(1000)
            .produceState(""),
        engine = DefaultRssMediaSourceEngine(client),
        backgroundScope,
    )

    override fun onCleared() {
        super.onCleared()
        client.replayCache.firstOrNull()?.close()
    }
}