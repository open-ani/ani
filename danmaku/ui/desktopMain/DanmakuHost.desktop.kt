package me.him188.ani.danmaku.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.DpSize

@Composable
@Preview
internal actual fun PreviewDanmakuHost() {
}

@Composable
internal actual fun currentWindowSize(): DpSize {
    return with(LocalDensity.current) {
        LocalWindowInfo.current.containerSize.run {
            DpSize(width.toDp(), height.toDp())
        }
    }
}