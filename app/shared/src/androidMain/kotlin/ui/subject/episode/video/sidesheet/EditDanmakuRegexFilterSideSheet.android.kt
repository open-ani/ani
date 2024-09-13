package me.him188.ani.app.ui.subject.episode.video.sidesheet


import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.settings.danmaku.createTestDanmakuRegexFilterState
import me.him188.ani.utils.platform.annotations.TestOnly

@OptIn(TestOnly::class)
@Composable
@Preview(device = "spec:width=1280dp,height=800dp,dpi=240")
fun PreviewEditDanmakuRegexFilterSideSheet() {
    ProvideCompositionLocalsForPreview {
        EditDanmakuRegexFilterSideSheet(
            state = createTestDanmakuRegexFilterState(),
            onDismissRequest = { },
        )
    }
}