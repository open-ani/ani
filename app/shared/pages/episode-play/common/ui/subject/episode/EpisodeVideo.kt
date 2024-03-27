package me.him188.ani.app.ui.subject.episode

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import me.him188.ani.app.platform.AniBuildConfig
import me.him188.ani.app.platform.isInLandscapeMode
import me.him188.ani.app.ui.foundation.TopAppBarGoBackButton
import me.him188.ani.app.ui.theme.aniDarkColorTheme
import me.him188.ani.app.ui.theme.slightlyWeaken
import me.him188.ani.app.videoplayer.PlayerController
import me.him188.ani.app.videoplayer.VideoPlayerView
import me.him188.ani.app.videoplayer.ui.PlayerNavigationBar
import me.him188.ani.app.videoplayer.ui.PlayerProgressController
import me.him188.ani.app.videoplayer.ui.VideoLoadingIndicator
import me.him188.ani.danmaku.ui.DanmakuHost
import me.him188.ani.danmaku.ui.DanmakuHostState
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import kotlin.time.Duration.Companion.seconds


@Composable
internal fun VideoScaffold(
    controllersVisible: Boolean = true,
    onControllersVisibleChange: (Boolean) -> Unit,
    topBar: @Composable RowScope.() -> Unit = {},
    video: @Composable BoxScope.() -> Unit = {},
    danmakuHost: @Composable BoxScope.() -> Unit = {},
    floatingMessage: @Composable BoxScope.() -> Unit = {},
    bottomBar: @Composable RowScope.() -> Unit = {},
    modifier: Modifier = Modifier,
    isFullscreen: Boolean = isInLandscapeMode(),
) {
    BoxWithConstraints(
        modifier.then(if (isFullscreen) Modifier.fillMaxHeight() else Modifier.fillMaxWidth()),
        contentAlignment = Alignment.Center
    ) { // 16:9 box
        Box(
            Modifier
                .then(
                    if (isFullscreen) {
                        Modifier.fillMaxSize()
                    } else {
                        Modifier.fillMaxWidth().height(maxWidth * 9 / 16) // 16:9 box
                    }
                )
        ) {
            Box(Modifier
                .clickable(
                    remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        onControllersVisibleChange(!controllersVisible)
                    }
                )
                .matchParentSize()
            ) {
                video()
                Box(Modifier.matchParentSize()) // 防止点击事件传播到 video 里
            }

            // 弹幕
            Box(Modifier.matchParentSize().fillMaxWidth().padding(vertical = 8.dp)) {
                CompositionLocalProvider(LocalContentColor provides aniDarkColorTheme().onBackground) {
                    danmakuHost()
                }
            }

            Column(Modifier.fillMaxSize().background(Color.Transparent)) {
                val controllerBackground = aniDarkColorTheme().background.copy(0.8f)

                // 顶部控制栏: 返回键, 标题, 设置
                AnimatedVisibility(
                    visible = controllersVisible,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(color = controllerBackground)
                            .statusBarsPadding()
                            .padding(top = 12.dp)
                            .height(28.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CompositionLocalProvider(LocalContentColor provides aniDarkColorTheme().onBackground) {
                            topBar()
                        }
                    }
                }

                Box(Modifier.weight(1f, fill = true))

                // 底部控制栏: 播放/暂停, 进度条, 切换全屏
                AnimatedVisibility(
                    visible = controllersVisible,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(color = controllerBackground)
                            .height(40.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CompositionLocalProvider(LocalContentColor provides Color.White) {
                            bottomBar()
                        }
                    }
                }
            }

            // 悬浮消息, 例如正在缓冲
            Box(Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                ProvideTextStyle(MaterialTheme.typography.labelSmall) {
                    CompositionLocalProvider(LocalContentColor provides aniDarkColorTheme().onBackground.slightlyWeaken()) {
                        floatingMessage()
                    }
                }
            }
        }
    }
}

/**
 * 剧集详情页面顶部的视频控件.
 */
@Composable
internal fun EpisodeVideo(
    videoSourceSelected: Boolean,
    videoReady: Boolean,
    playerController: PlayerController,
    danmakuHostState: DanmakuHostState,
    onClickFullScreen: () -> Unit,
    modifier: Modifier = Modifier,
    isFullscreen: Boolean = isInLandscapeMode(),
) {
    val controllerVisible by playerController.controllerVisible.collectAsStateWithLifecycle()

    VideoScaffold(
        controllersVisible = controllerVisible,
        onControllersVisibleChange = { playerController.setControllerVisible(it) },
        topBar = {
            PlayerNavigationBar(
                actions = {
                    TopAppBarGoBackButton()
                },
            )
        },
        video = {
            VideoPlayerView(playerController, Modifier.matchParentSize())
        },
        bottomBar = {
            PlayerProgressController(
                controller = playerController,
                onClickFullScreen = onClickFullScreen,
            )
        },
        floatingMessage = {
            Column {
                EpisodeVideoLoadingIndicator(playerController, videoSourceSelected, videoReady)
                if (AniBuildConfig.current().isDebug) {
                    playerController.videoSource.collectAsStateWithLifecycle().value?.let {
                        EpisodeVideoDebugInfo(
                            it,
                            Modifier.padding(8.dp)
                        )
                    }
                }
            }
        },
        danmakuHost = {
            DanmakuHost(danmakuHostState, Modifier.matchParentSize())
        },
        modifier = modifier,
        isFullscreen = isFullscreen
    )
}

@Composable
private fun EpisodeVideoLoadingIndicator(
    playerController: PlayerController,
    videoSourceSelected: Boolean,
    videoReady: Boolean,
    modifier: Modifier = Modifier,
) {
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

                    !videoReady -> {
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
            },
            modifier,
        )
    }
}
