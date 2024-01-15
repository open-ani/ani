package me.him188.ani.app.ui.subject.episode

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.him188.ani.app.ui.foundation.TopAppBarGoBackButton
import me.him188.ani.app.videoplayer.PlayerController
import me.him188.ani.app.videoplayer.VideoPlayerView
import me.him188.ani.app.videoplayer.VideoSource
import me.him188.ani.app.videoplayer.ui.PlayerControllerOverlay
import me.him188.ani.app.videoplayer.ui.PlayerControllerOverlayBottomBar
import me.him188.ani.app.videoplayer.ui.PlayerControllerOverlayTopBar
import me.him188.ani.app.videoplayer.ui.VideoLoadingIndicator
import me.him188.ani.datasources.api.topic.FileSize
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import kotlin.time.Duration.Companion.seconds

/**
 * 剧集详情页面顶部的视频控件.
 */
@Composable
internal fun EpisodeVideo(
    video: VideoSource<*>?,
    playerController: PlayerController,
    onClickGoBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (controllerVisible, setControllerVisible) = remember { mutableStateOf(true) }

    val controllerAlpha by animateFloatAsState(if (controllerVisible) 1f else 0f)
    LaunchedEffect(controllerVisible) {
        // 2 秒后隐藏 TopAppBar
        if (controllerVisible) {
            launch {
                delay(2.seconds)
                setControllerVisible(false)
            }
        }
    }

    BoxWithConstraints(modifier.fillMaxWidth()) {
        Box(
            Modifier.fillMaxWidth().height(maxWidth * 9 / 16)
                .clickable(
                    remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        setControllerVisible(!controllerVisible)
                    }
                )
        ) { // 16:9 box
            VideoPlayerView(playerController, Modifier.matchParentSize())

            PlayerControllerOverlay(
                topBar = {
                    PlayerControllerOverlayTopBar(
                        controllerAlpha,
                        startActions = {
                            TopAppBarGoBackButton(onClickGoBack)
                        },
                        Modifier.statusBarsPadding().padding(top = 8.dp)
                    )
                },
                floatingBox = {
                    val isBuffering by playerController.isBuffering.collectAsStateWithLifecycle(true)
                    if (isBuffering) {
                        VideoLoadingIndicator(text = {
                            if (video == null) {
                                Text("正在下载种子")
                            } else {
//                                val speed by video.downloadRate.collectAsStateWithLifecycle(null)
                                val speed = 1L
                                speed?.let {
                                    Text("正在缓冲 ${FileSize(it)}/s")
                                } ?: kotlin.run {
                                    Text("正在连接")
                                }
                            }
                        })
                    }
                },
                bottomBar = {
                    PlayerControllerOverlayBottomBar(
                        controller = playerController,
                        modifier = Modifier.padding(bottom = 8.dp).alpha(controllerAlpha).fillMaxWidth()
                    )
                },
                Modifier.matchParentSize()
            )
        }
    }
}
