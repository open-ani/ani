package me.him188.ani.app.ui.subject.episode

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.him188.ani.app.platform.isInLandscapeMode
import me.him188.ani.app.ui.foundation.TopAppBarGoBackButton
import me.him188.ani.app.ui.theme.aniDarkColorTheme
import me.him188.ani.app.ui.theme.looming
import me.him188.ani.app.videoplayer.PlayerController
import me.him188.ani.app.videoplayer.VideoPlayerView
import me.him188.ani.app.videoplayer.VideoSource
import me.him188.ani.app.videoplayer.ui.PlayerControllerOverlay
import me.him188.ani.app.videoplayer.ui.PlayerControllerOverlayBottomBar
import me.him188.ani.app.videoplayer.ui.PlayerControllerOverlayTopBar
import me.him188.ani.app.videoplayer.ui.VideoLoadingIndicator
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import kotlin.time.Duration.Companion.seconds

/**
 * 剧集详情页面顶部的视频控件.
 */
@Composable
internal fun EpisodeVideo(
    videoSourceSelected: Boolean,
    video: VideoSource<*>?,
    playerController: PlayerController,
    onClickGoBack: () -> Unit,
    onClickFullScreen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (controllerVisible, setControllerVisible) = remember { mutableStateOf(true) }

    val controllerAlpha by animateFloatAsState(if (controllerVisible) 1f else 0f, tween())
    LaunchedEffect(controllerVisible) {
        // 2 秒后隐藏 TopAppBar
        if (controllerVisible) {
            launch {
                delay(2.seconds)
                setControllerVisible(false)
            }
        }
    }

    BoxWithConstraints(
        modifier.then(if (isInLandscapeMode()) Modifier.fillMaxWidth() else Modifier.fillMaxHeight()),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier.then(
                if (isInLandscapeMode()) {
                    Modifier.fillMaxHeight().width(maxHeight * 16 / 9)
                } else {
                    Modifier.fillMaxWidth()
                        .height(maxWidth * 9 / 16)
                }
            ).clickable(
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
                        startActions = {
                            TopAppBarGoBackButton(onClickGoBack)
                        },
                        Modifier.alpha(controllerAlpha)
                            .background(color = aniDarkColorTheme().background.copy(0.8f))
                            .statusBarsPadding()
                            .padding(top = 12.dp)
                            .height(24.dp),
                    )
                },
                floatingBox = {
                    val isBuffering by playerController.isBuffering.collectAsStateWithLifecycle(true)
                    if (isBuffering) {
                        var loadedTooLong by remember { mutableStateOf(false) }
                        VideoLoadingIndicator(
                            showProgress = videoSourceSelected,
                            text = {
                                when {
                                    !videoSourceSelected -> {
                                        Text("请选择数据源")
                                    }

                                    video == null -> {
                                        Text("正在下载种子")
                                    }

                                    loadedTooLong -> {
                                        Text("资源较慢, 正在努力缓冲")
                                    }

                                    else -> {
                                        //                                val speed by video.downloadRate.collectAsStateWithLifecycle(null)
                                        Text("正在缓冲")
                                        LaunchedEffect(true) {
                                            delay(10.seconds)
                                            loadedTooLong = true
                                        }
                                    }
                                }
                            }
                        )
                    }
                },
                bottomBar = {
                    PlayerControllerOverlayBottomBar(
                        controller = playerController,
                        onClickFullScreen = onClickFullScreen,
                        modifier = Modifier
                            .alpha(controllerAlpha)
                            .background(color = aniDarkColorTheme().background.looming())
                            .padding(vertical = 4.dp)
                            .fillMaxWidth()
                    )
                },
                Modifier.matchParentSize()
            )
        }
    }
}
