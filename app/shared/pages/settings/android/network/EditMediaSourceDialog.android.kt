package me.him188.ani.app.ui.settings.tabs.network

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import kotlinx.coroutines.flow.flowOf
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.datasources.api.source.MediaSourceConfig
import me.him188.ani.datasources.api.source.MediaSourceParameters
import me.him188.ani.datasources.api.source.buildMediaSourceParameters
import kotlin.coroutines.EmptyCoroutineContext

@PreviewLightDark
@Composable
private fun PreviewEditMediaSourceDialogNoConfig() {
    ProvideCompositionLocalsForPreview {
        EditMediaSourceDialog(
            state = EditMediaSourceState(
                info = MediaSourceInfo(
                    mediaSourceId = "test",
                    name = "Test",
                    description = "Test description",
                    iconUrl = null,
                    parameters = MediaSourceParameters.Empty,
                ),
                persistedArguments = flowOf(MediaSourceConfig.Default),
                editType = EditType.Add,
                parentCoroutineContext = EmptyCoroutineContext,
            ),
            onDismissRequest = {},
            onConfirm = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun PreviewEditMediaSourceDialog() {
    ProvideCompositionLocalsForPreview {
        EditMediaSourceDialog(
            state = EditMediaSourceState(
                info = MediaSourceInfo(
                    mediaSourceId = "test",
                    name = "Test",
                    description = "Test description",
                    iconUrl = null,
                    parameters = buildMediaSourceParameters {
                        string("username", description = "用户名")
                        string("password", description = "密码")
                        boolean("Switch", true, description = "这是一个开关")
                        boolean("开关", false, description = "This is a switch")
                        boolean("开关", false, description = "This is a switch.".repeat(10))
                        boolean("Switch2", false)
                        simpleEnum("dropdown", "a", "b", "c", default = "b", description = "这是一个下拉菜单")
                    },
                ),
                persistedArguments = flowOf(MediaSourceConfig.Default),
                editType = EditType.Add,
                parentCoroutineContext = EmptyCoroutineContext,
            ),
            onDismissRequest = {},
            onConfirm = {},
        )
    }
}
