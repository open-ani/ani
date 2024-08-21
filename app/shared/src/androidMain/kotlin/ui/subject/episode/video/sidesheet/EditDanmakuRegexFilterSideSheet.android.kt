package me.him188.ani.app.ui.subject.episode.video.sidesheet


import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import me.him188.ani.app.data.models.danmaku.DanmakuRegexFilter
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.subject.episode.video.settings.DanmakuRegexFilterState
import me.him188.ani.utils.platform.Uuid

@Composable
@Preview(device = Devices.TABLET)
fun PreviewAddDanmakuRegexFilterSideSheet() {
    ProvideCompositionLocalsForPreview {
        EditDanmakuRegexFilterSideSheet(
            danmakuRegexFilterState = DanmakuRegexFilterState(
                danmakuRegexFilterList = mutableStateOf(
                    listOf(
                        DanmakuRegexFilter(
                            id = Uuid.randomString(),
                            name = "测试",
                            regex = "测试"
                        ),
                        DanmakuRegexFilter(
                            id = Uuid.randomString(),
                            name = "测试2",
                            regex = "测试2"
                        ),
                    )
                ),
                addDanmakuRegexFilter = { },
                editDanmakuRegexFilter = { _, _ -> },
                removeDanmakuRegexFilter = { },
                switchDanmakuRegexFilter = { }
            ),
            onDismissRequest = { }
        )
    }
}