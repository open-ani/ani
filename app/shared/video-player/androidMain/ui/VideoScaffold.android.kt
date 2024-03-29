package me.him188.ani.app.videoplayer.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.preview.PHONE_LANDSCAPE
import me.him188.ani.app.videoplayer.DummyPlayerController

@Preview(device = PHONE_LANDSCAPE)
@Composable
private fun PreviewVideoScaffold() = ProvideCompositionLocalsForPreview {
    val controller = remember {
        DummyPlayerController()
    }

    val controllerVisible by remember {
        mutableStateOf(true)
    }

    VideoScaffold(
        controllersVisible = controllerVisible,
        onControllersVisibleChange = { controller.setControllerVisible(it) },
        topBar = {
            PlayerNavigationBar(
                title = { Text(text = "Title") },
                actions = {
                },
            )
        },
        video = {
//            AniKamelImage(resource = asyncPainterResource(data = "https://picsum.photos/536/354"))
        },
        bottomBar = {
            PlayerProgressController(
                controller = controller,
                onClickFullScreen = {},
            )
        },
        floatingMessage = {
            Column {
                VideoLoadingIndicator(true, text = { Text(text = "正在缓冲") })
            }

        },
        danmakuHost = {
        },
        modifier = Modifier,
        isFullscreen = true,
    )
}