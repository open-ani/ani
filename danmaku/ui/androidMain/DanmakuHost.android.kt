package me.him188.ani.danmaku.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

@Composable
@Preview
internal actual fun PreviewDanmakuHost() {
    PreviewDanmakuHostImpl()
}

@Composable
internal actual fun currentWindowSize(): DpSize {
    return with(LocalDensity.current) {
//        LocalWindowInfo.current.containerSize.run {
//            DpSize(width.toDp(), height.toDp())
//        }
        DpSize(300.dp, 300.dp)
    }
}