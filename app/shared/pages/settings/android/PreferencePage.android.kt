package me.him188.ani.app.ui.settings.tabs

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import me.him188.ani.app.data.media.MediaSourceManager
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.rememberViewModel
import me.him188.ani.app.ui.settings.SettingsPage
import me.him188.ani.app.ui.settings.SettingsTab
import me.him188.ani.app.ui.settings.framework.ConnectionTestResult
import me.him188.ani.app.ui.settings.framework.components.SettingsScope
import me.him188.ani.app.ui.settings.framework.components.SwitchItem
import me.him188.ani.app.ui.settings.framework.components.TextFieldDialog
import me.him188.ani.app.ui.settings.tabs.network.NetworkSettingsTab
import me.him188.ani.app.ui.settings.tabs.network.NetworkSettingsViewModel
import me.him188.ani.datasources.acgrip.AcgRipMediaSource
import me.him188.ani.datasources.api.paging.PageBasedPagedSource
import me.him188.ani.datasources.api.paging.Paged
import me.him188.ani.datasources.api.paging.PagedSource
import me.him188.ani.datasources.api.source.ConnectionStatus
import me.him188.ani.datasources.api.source.DownloadSearchQuery
import me.him188.ani.datasources.api.source.MediaSource
import me.him188.ani.datasources.api.source.TopicMediaSource
import me.him188.ani.datasources.api.topic.Topic
import me.him188.ani.datasources.dmhy.DmhyMediaSource
import me.him188.ani.datasources.mikan.MikanMediaSource
import kotlin.random.Random

@Composable
private fun PreviewTab(
    content: @Composable SettingsScope.() -> Unit,
) {
    ProvideCompositionLocalsForPreview {
        SettingsTab {
            content()
        }
    }
}

@Preview
@Composable
private fun PreviewPreferencePage() {
    ProvideCompositionLocalsForPreview {
        SettingsPage()
    }
}

private class TestMediaSource(
    override val mediaSourceId: String,
) : TopicMediaSource() {
    override suspend fun checkConnection(): ConnectionStatus {
        return Random.nextBoolean().let {
            if (it) ConnectionStatus.SUCCESS else ConnectionStatus.FAILED
        }
    }

    override suspend fun startSearch(query: DownloadSearchQuery): PagedSource<Topic> {
        return PageBasedPagedSource {
            Paged.empty()
        }
    }
}

@Preview
@Composable
private fun PreviewNetworkPreferenceTab() {
    ProvideCompositionLocalsForPreview(
        module = {
            single<MediaSourceManager> {
                object : MediaSourceManager {
                    override val enabledSources: MutableStateFlow<List<MediaSource>> = MutableStateFlow(
                        listOf(
                            TestMediaSource(AcgRipMediaSource.ID),
                            TestMediaSource(DmhyMediaSource.ID),
                            TestMediaSource(MikanMediaSource.ID),
                            TestMediaSource("local"),
                        )
                    )
                    override val allSources: SharedFlow<List<MediaSource>>
                        get() = enabledSources
                    override val allIds: List<String> = enabledSources.value.map { it.mediaSourceId }
                    override val allIdsExceptLocal: List<String>
                        get() = allIds.filter { !isLocal(it) }
                }
            }
        }
    ) {
        val vm = rememberViewModel { NetworkSettingsViewModel() }
        SideEffect {
            val testers = vm.allMediaTesters.testers
            testers.first().result = ConnectionTestResult.SUCCESS
            testers.drop(1).first().result = ConnectionTestResult.FAILED
            testers.drop(2).first().result = ConnectionTestResult.NOT_ENABLED
        }
        NetworkSettingsTab()
    }
}

@Preview
@Composable
private fun PreviewPreferenceScope() {
    ProvideCompositionLocalsForPreview {
        SettingsTab {
            SwitchItem(
                checked = true,
                onCheckedChange = {},
                title = {
                    Text("Test")
                },
                description = {
                    Text(text = "Test description")
                },
            )
        }
    }
}

@Preview
@Composable
private fun PreviewTextFieldDialog() {
    PreviewTab {
        TextFieldDialog(
            onDismissRequest = {},
            onConfirm = {},
            title = { Text(text = "编辑") },
            description = { Text(LoremIpsum(20).values.first()) }
        ) {
            OutlinedTextField(
                value = "test",
                onValueChange = {},
                shape = MaterialTheme.shapes.medium
            )
        }
    }
}