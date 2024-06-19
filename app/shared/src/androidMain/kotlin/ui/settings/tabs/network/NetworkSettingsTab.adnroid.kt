package me.him188.ani.app.ui.settings.tabs.network

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import me.him188.ani.app.data.media.MediaSourceManager
import me.him188.ani.app.data.media.instance.MediaSourceInstance
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.rememberViewModel
import me.him188.ani.app.ui.settings.framework.ConnectionTestResult
import me.him188.ani.datasources.acgrip.AcgRipMediaSource
import me.him188.ani.datasources.api.source.MediaSourceConfig
import me.him188.ani.datasources.api.source.MediaSourceFactory
import me.him188.ani.datasources.api.source.MediaSourceParameters
import me.him188.ani.datasources.api.source.TestHttpMediaSource
import me.him188.ani.datasources.dmhy.DmhyMediaSource
import me.him188.ani.datasources.mikan.MikanMediaSource


@Preview
@Composable
private fun PreviewNetworkPreferenceTab() {
    ProvideCompositionLocalsForPreview(
        module = {
            single<MediaSourceManager> {
                object : MediaSourceManager {
                    override val allInstances = MutableStateFlow(
                        listOf(
                            MediaSourceInstance(
                                "1",
                                DmhyMediaSource.ID,
                                true,
                                MediaSourceConfig(),
                                TestHttpMediaSource(AcgRipMediaSource.ID, randomConnectivity = true)
                            ),

                            MediaSourceInstance(
                                "1",
                                DmhyMediaSource.ID,
                                true,
                                MediaSourceConfig(),
                                TestHttpMediaSource(DmhyMediaSource.ID, randomConnectivity = true)
                            ),

                            MediaSourceInstance(
                                "1",
                                DmhyMediaSource.ID,
                                true,
                                MediaSourceConfig(),
                                TestHttpMediaSource(MikanMediaSource.ID, randomConnectivity = true)
                            ),

                            MediaSourceInstance(
                                "1",
                                DmhyMediaSource.ID,
                                true,
                                MediaSourceConfig(),
                                TestHttpMediaSource("local", randomConnectivity = true)
                            ),
                        )
                    )
                    override val allFactories: List<MediaSourceFactory> = listOf(MikanMediaSource.Factory())
                    override val allFactoryIds: List<String> = allInstances.value.map { it.mediaSourceId }
                    override val allFactoryIdsExceptLocal: List<String>
                        get() = allFactoryIds.filter { !isLocal(it) }

                    override fun instanceConfigFlow(instanceId: String): Flow<MediaSourceConfig> {
                        return MutableStateFlow(MediaSourceConfig())
                    }

                    override suspend fun addInstance(mediaSourceId: String, config: MediaSourceConfig) {
                    }

                    override suspend fun updateConfig(instanceId: String, config: MediaSourceConfig) {
                    }

                    override suspend fun setEnabled(instanceId: String, enabled: Boolean) {
                    }

                    override suspend fun removeInstance(instanceId: String) {
                    }
                }
            }
        }
    ) {
        val vm = rememberViewModel { NetworkSettingsViewModel() }
        SideEffect {
            val testers = vm.mediaSourceTesters.testers
            if (testers.size < 3) return@SideEffect
            testers.first().result = ConnectionTestResult.SUCCESS
            testers.drop(1).first().result = ConnectionTestResult.FAILED
            testers.drop(2).first().result = ConnectionTestResult.NOT_ENABLED
        }
        NetworkSettingsTab()
    }
}

@Preview
@Composable
private fun PreviewSelectMediaSourceTemplateLayout() {
    ProvideCompositionLocalsForPreview {
        SelectMediaSourceTemplateDialog(
            remember {
                listOf(
                    MediaSourceTemplate(
                        MediaSourceInfo(
                            mediaSourceId = "1",
                            name = "Test",
                            parameters = MediaSourceParameters.Empty,
                        ),
                    ),
                    MediaSourceTemplate(
                        MediaSourceInfo(
                            mediaSourceId = "123",
                            name = "Test2",
                            parameters = MediaSourceParameters.Empty,
                        ),
                    ),
                )
            },
            onClick = {},
            onDismissRequest = {},
        )
    }
}
