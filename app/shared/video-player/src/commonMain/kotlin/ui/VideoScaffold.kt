package me.him188.ani.app.videoplayer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.theme.aniDarkColorTheme
import me.him188.ani.app.ui.foundation.theme.slightlyWeaken
import me.him188.ani.app.videoplayer.ui.guesture.VideoGestureHost
import me.him188.ani.app.videoplayer.ui.progress.PlayerControllerBar
import me.him188.ani.app.videoplayer.ui.top.PlayerTopBar

/**
 * 视频播放器框架, 可以自定义组合控制器等部分.
 *
 * 视频播放器框架由以下层级组成, 由上至下:
 *
 * - 悬浮消息: [floatingMessage], 例如正在缓冲
 * - 控制器: [topBar], [rhsBar] 和 [bottomBar]
 * - 手势: [gestureHost]
 * - 弹幕: [danmakuHost]
 * - 视频: [video]
 * - 右侧侧边栏: [rhsSheet]
 *
 * @param controllersVisible 是否展示 [topBar], [rhsBar] 和 [bottomBar]
 * @param topBar [PlayerTopBar]
 * @param video [VideoPlayer]. video 不会接受到点击事件.
 * @param danmakuHost 为 `DanmakuHost` 留的区域
 * @param gestureHost 手势区域, 例如快进/快退, 音量调节等. See [VideoGestureHost]
 * @param floatingMessage 悬浮消息, 例如正在缓冲. 将会对齐到中央
 * @param rhsBar 右侧控制栏, 锁定手势等.
 * @param bottomBar [PlayerControllerBar]
 * @param expanded 当前是否处于全屏模式. 全屏时此框架会 [Modifier.fillMaxSize], 否则会限制为一个 16:9 的框.
 */
@Composable
fun VideoScaffold(
    expanded: Boolean,
    modifier: Modifier = Modifier,
    maintainAspectRatio: Boolean = !expanded,
    controllersVisible: () -> Boolean = { true },
    gestureLocked: () -> Boolean = { false },
    topBar: @Composable RowScope.() -> Unit = {},
    /**
     * @see VideoPlayer
     */
    video: @Composable BoxScope.() -> Unit = {},
    danmakuHost: @Composable BoxScope.() -> Unit = {},
    gestureHost: @Composable BoxWithConstraintsScope.() -> Unit = {},
    floatingMessage: @Composable BoxScope.() -> Unit = {},
    rhsButtons: @Composable ColumnScope.() -> Unit = {},
    gestureLock: @Composable ColumnScope.() -> Unit = {},
    bottomBar: @Composable RowScope.() -> Unit = {},
    floatingBottomEnd: @Composable RowScope.() -> Unit = {},
    rhsSheet: @Composable () -> Unit = {},
) {
    val controllersVisibleState by derivedStateOf(controllersVisible)
    val gestureLockedState by derivedStateOf(gestureLocked) // delayed access to minimize recomposition

    BoxWithConstraints(
        modifier.then(if (expanded) Modifier.fillMaxHeight() else Modifier.fillMaxWidth()),
        contentAlignment = Alignment.Center,
    ) { // 16:9 box
        Box(
            Modifier
                .then(
                    if (!maintainAspectRatio) {
                        Modifier.fillMaxSize()
                    } else {
                        Modifier.fillMaxWidth().height(maxWidth * 9 / 16) // 16:9 box
                    },
                ),
        ) {
            Box(
                Modifier
                    .background(Color.Transparent)
                    .matchParentSize(),
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

            // 控制手势
            BoxWithConstraints(Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                gestureHost()
            }

            Box {
                Column(Modifier.fillMaxSize().background(Color.Transparent)) {
                    // 顶部控制栏: 返回键, 标题, 设置
                    AnimatedVisibility(
                        visible = controllersVisibleState && !gestureLockedState,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        Box {
                            Box(
                                Modifier
                                    .matchParentSize()
                                    .background(
                                        Brush.verticalGradient(
                                            0f to Color.Transparent.copy(0.72f),
                                            0.32f to Color.Transparent.copy(0.45f),
                                            1f to Color.Transparent,
                                        ),
                                    ),
                            )
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .statusBarsPadding(),
                            ) {
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    CompositionLocalProvider(LocalContentColor provides aniDarkColorTheme().onBackground) {
                                        topBar()
                                    }
                                }
                                Spacer(Modifier.height(16.dp))
                            }

                        }
                    }

                    Box(Modifier.weight(1f, fill = true).fillMaxWidth())

                    // 底部控制栏: 播放/暂停, 进度条, 切换全屏
                    AnimatedVisibility(
                        visible = controllersVisibleState && !gestureLockedState,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        0f to Color.Transparent,
                                        1 - 0.32f to Color.Transparent.copy(0.45f),
                                        1f to Color.Transparent.copy(0.72f),
                                    ),
                                ),
                        ) {
                            Spacer(Modifier.height(if (expanded) 12.dp else 6.dp))
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                MaterialTheme(aniDarkColorTheme()) {
                                    CompositionLocalProvider(LocalContentColor provides Color.White) {
                                        bottomBar()
                                    }
                                }
                            }
                        }
                    }
                }
                AnimatedVisibility(
                    !controllersVisibleState && !expanded,
                    Modifier.align(Alignment.BottomEnd),
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    Row(
                        Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                    ) {
                        MaterialTheme(aniDarkColorTheme()) {
                            CompositionLocalProvider(LocalContentColor provides Color.White) {
                                floatingBottomEnd()
                            }
                        }
                    }
                }
            }
            Column(Modifier.fillMaxSize().background(Color.Transparent)) {
                Box(Modifier.weight(1f, fill = true).fillMaxWidth()) {
                    Column(
                        Modifier.padding(end = 16.dp).align(Alignment.CenterEnd),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        AnimatedVisibility(
                            visible = controllersVisibleState && !gestureLockedState,
                            enter = fadeIn(),
                            exit = fadeOut(),
                        ) {
                            rhsButtons()
                        }

                        // Separate from controllers, to fix position when controllers are/aren't hidden
                        AnimatedVisibility(
                            visible = controllersVisibleState,
                            enter = fadeIn(),
                            exit = fadeOut(),
                        ) {
                            gestureLock()
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

            // 右侧 sheet
            Box(Modifier.matchParentSize()) {
                rhsSheet()
            }
        }
    }
}
