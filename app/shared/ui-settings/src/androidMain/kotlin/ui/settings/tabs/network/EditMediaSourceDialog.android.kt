package me.him188.ani.app.ui.settings.tabs.network

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import kotlinx.coroutines.flow.flowOf
import me.him188.ani.app.ui.settings.tabs.media.source.EditMediaSourceDialog
import me.him188.ani.app.ui.settings.tabs.media.source.EditingMediaSource
import me.him188.ani.app.ui.settings.tabs.media.source.TestEditMediaSourceModeAdd
import me.him188.ani.datasources.api.source.FactoryId
import me.him188.ani.datasources.api.source.MediaSourceConfig
import me.him188.ani.datasources.api.source.MediaSourceInfo
import me.him188.ani.datasources.api.source.parameter.MediaSourceParameters
import me.him188.ani.datasources.api.source.parameter.buildMediaSourceParameters
import me.him188.ani.utils.platform.annotations.TestOnly
import kotlin.coroutines.EmptyCoroutineContext

@OptIn(TestOnly::class)
@PreviewLightDark
@Composable
private fun PreviewEditMediaSourceDialogNoConfig() {
    EditMediaSourceDialog(
        state = EditingMediaSource(
            editingMediaSourceId = "test",
            factoryId = FactoryId("test"),
            info = MediaSourceInfo(
                displayName = "Test",
                description = "Test description",
            ),
            parameters = MediaSourceParameters.Empty,
            persistedArguments = flowOf(MediaSourceConfig.Default),
            editMediaSourceMode = TestEditMediaSourceModeAdd,
            onSave = {},
            parentCoroutineContext = EmptyCoroutineContext,
        ),
        {},
    )
}

@OptIn(TestOnly::class)
@PreviewLightDark
@Composable
private fun PreviewEditMediaSourceDialog() {
    EditMediaSourceDialog(
        state = EditingMediaSource(
            editingMediaSourceId = "test",
            factoryId = FactoryId("test"),
            info = MediaSourceInfo(
                displayName = "Test",
                description = "Test description",
            ),
            parameters = buildMediaSourceParameters {
                string("username", description = "用户名")
                string("password", description = "密码")
                boolean("Switch", true, description = "这是一个开关")
                boolean("开关", false, description = "This is a switch")
                boolean("开关", false, description = "This is a switch.".repeat(10))
                boolean("Switch2", false)
                simpleEnum("dropdown", "a", "b", "c", default = "b", description = "这是一个下拉菜单")
            },
            persistedArguments = flowOf(MediaSourceConfig.Default),
            editMediaSourceMode = TestEditMediaSourceModeAdd,
            onSave = {},
            parentCoroutineContext = EmptyCoroutineContext,
        ),
        {},
    )
}

