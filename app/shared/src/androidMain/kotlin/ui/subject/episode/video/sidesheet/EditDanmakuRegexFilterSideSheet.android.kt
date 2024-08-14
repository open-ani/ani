package me.him188.ani.app.ui.subject.episode.video.sidesheet


import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import me.him188.ani.app.data.models.danmaku.DanmakuRegexFilter
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.utils.platform.Uuid

@Composable
@Preview(device = Devices.TABLET)
fun PreviewAddDanmakuRegexFilterSideSheet() {
    ProvideCompositionLocalsForPreview {
        EditDanmakuRegexFilterSideSheet(
            danmakuRegexFilterList = listOf(
                DanmakuRegexFilter(
                    id = Uuid.randomString(),
                    regex = ".*",
                    enabled = true
                ),
                DanmakuRegexFilter(
                    id = Uuid.randomString(),
                    regex = ".*hahe.*",
                    enabled = true
            )
            ),
            onRemove = {},
            onSwitch = {},
            onAdd = {},
            onDismissRequest = {},
        )
    }
}