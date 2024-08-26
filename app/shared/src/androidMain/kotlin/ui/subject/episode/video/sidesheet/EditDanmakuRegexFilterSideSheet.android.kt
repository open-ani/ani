package me.him188.ani.app.ui.subject.episode.video.sidesheet


import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.subject.episode.video.settings.createDanmakuRegexFilterState

@Composable
@Preview(device = Devices.TABLET)
fun PreviewEditDanmakuRegexFilterSideSheet() {
    ProvideCompositionLocalsForPreview {
        EditDanmakuRegexFilterSideSheet(
            danmakuRegexFilterState = createDanmakuRegexFilterState(),
            onDismissRequest = { },
        )
    }
}