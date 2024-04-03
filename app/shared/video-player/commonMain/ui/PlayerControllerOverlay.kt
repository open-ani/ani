package me.him188.ani.app.videoplayer.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.theme.aniDarkColorTheme
import me.him188.ani.app.ui.theme.slightlyWeaken
import me.him188.ani.app.videoplayer.DummyPlayerController
import me.him188.ani.app.videoplayer.ui.top.PlayerTopBar


/**
 * 覆盖在视频播放器上层的控制器
 */
@Composable
fun PlayerControllerOverlay(
    topBar: @Composable RowScope.() -> Unit = {},
    floatingBox: @Composable BoxScope.() -> Unit = {},
    bottomBar: @Composable RowScope.() -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Surface(modifier.fillMaxSize(), color = Color.Transparent) {
        Box {
            Column(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth()) {
                    topBar()
                }

                Spacer(Modifier.weight(1f, fill = true))

                Row(Modifier.fillMaxWidth()) {
                    CompositionLocalProvider(LocalContentColor provides Color.White) {
                        bottomBar()
                    }
                }
            }

            Box(Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                ProvideTextStyle(MaterialTheme.typography.labelSmall) {
                    CompositionLocalProvider(LocalContentColor provides aniDarkColorTheme().onBackground.slightlyWeaken()) {
                        floatingBox()
                    }
                }
            }
        }
    }
}

@Composable
internal expect fun PreviewPlayerControllerOverlay()

@Preview
@Composable
internal fun PreviewPlayerControllerOverlayImpl() {
    ProvideCompositionLocalsForPreview {
        val controller = remember {
            DummyPlayerController()
        }
        Box(modifier = Modifier.background(Color.Black)) {
            PlayerControllerOverlay(
                topBar = {
                    PlayerTopBar(
                        title = null,
                        actions = {
                        },
                        Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    )
                },
                bottomBar = {
                    PlayerProgressController(
                        controller = controller,
                        isFullscreen = true,
                        onClickFullscreen = {},
                        danmakuEnabled = false,
                        setDanmakuEnabled = {},
                        Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    )
                },
            )
        }
    }
}
