package me.him188.ani.app.videoplayer.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.preview.PHONE_LANDSCAPE
import me.him188.ani.app.ui.subject.episode.components.EpisodePlayerTitle
import me.him188.ani.app.videoplayer.DummyPlayerController
import me.him188.ani.app.videoplayer.ui.guesture.GestureLock
import me.him188.ani.app.videoplayer.ui.guesture.LockableVideoGestureHost
import me.him188.ani.app.videoplayer.ui.guesture.rememberSwipeSeekerState
import me.him188.ani.app.videoplayer.ui.top.PlayerTopBar

@Preview(device = PHONE_LANDSCAPE)
@Composable
private fun PreviewVideoScaffoldFullscreen() = ProvideCompositionLocalsForPreview {
    val controller = remember {
        DummyPlayerController()
    }

    var controllerVisible by remember {
        mutableStateOf(true)
    }
    var isLocked by remember { mutableStateOf(false) }

    VideoScaffold(
        controllersVisible = controllerVisible,
        gestureLocked = isLocked,
        topBar = {
            PlayerTopBar(
                title = {
                    EpisodePlayerTitle(
                        ep = "28",
                        episodeTitle = "因为下次再见的时候会很难为情",
                        subjectTitle = "葬送的芙莉莲"
                    )
                },
                actions = {
                },
            )
        },
        video = {
//            AniKamelImage(resource = asyncPainterResource(data = "https://picsum.photos/536/354"))
        },
        danmakuHost = {
        },
        gestureHost = {
            LockableVideoGestureHost(
                rememberSwipeSeekerState(constraints.maxWidth) {

                },
                controllerVisible = controllerVisible,
                locked = isLocked,
                setControllerVisible = { controllerVisible = it },
                Modifier.fillMaxSize(),
                onDoubleClickScreen = {}
            )
        },
        floatingMessage = {
            Column {
                VideoLoadingIndicator(true, text = { Text(text = "正在缓冲") })
            }

        },
        rhsBar = {
            GestureLock(isLocked = isLocked, onClick = { isLocked = !isLocked })
        },
        bottomBar = {
            PlayerProgressController(
                controller = controller,
                isFullscreen = true,
                onClickFullscreen = {},
            )
        },
        modifier = Modifier,
        isFullscreen = true,
    )
}
