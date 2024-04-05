package me.him188.ani.app.videoplayer.ui.progress

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SpeakerNotesOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.effects.onKey
import me.him188.ani.app.ui.theme.aniLightColorTheme
import me.him188.ani.app.ui.theme.slightlyWeaken
import me.him188.ani.app.ui.theme.stronglyWeaken


@Stable
object PlayerControllerDefaults {
    /**
     * To pause/play
     */
    @Composable
    fun PlaybackIcon(
        isPlaying: () -> Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        IconButton(
            onClick = onClick,
            modifier,
        ) {
            if (isPlaying()) {
                Icon(Icons.Rounded.Pause, contentDescription = "Pause", Modifier.size(36.dp))
            } else {
                Icon(Icons.Rounded.PlayArrow, contentDescription = "Play", Modifier.size(36.dp))
            }
        }
    }

    /**
     * To turn danmaku on/off
     */
    @Composable
    fun DanmakuIcon(
        danmakuEnabled: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        IconButton(
            onClick = onClick,
            modifier,
        ) {
            if (danmakuEnabled) {
                Icon(Icons.AutoMirrored.Rounded.Chat, contentDescription = "Disable Danmaku")
            } else {
                Icon(Icons.Rounded.SpeakerNotesOff, contentDescription = "Enable Danmaku")
            }
        }
    }

    @Stable
    private val DANMAKU_PLACEHOLDERS = listOf(
        "来发一条弹幕吧~",
        "小心，我要发射弹幕啦！",
        "每一条弹幕背后，都有一个不为人知的秘密",
        "召唤弹幕精灵！",
        "这一刻的感受，只有你最懂",
        "让弹幕变得不一样",
        "弹幕世界大门已开",
        "字里行间，藏着宇宙的秘密",
        "在光与影的交织中，你的话语是唯一的真实",
        "有趣的灵魂万里挑一",
        "说点什么",
        "长期征集有趣的弹幕广告词",
        "广告位招租",
        "\uD83E\uDD14",
        "梦开始的地方",
        "心念成形",
    )

    /**
     * To send danmaku
     */
    @Composable
    fun DanmakuSendButton(
        onClick: () -> Unit,
        enabled: Boolean = true,
        modifier: Modifier = Modifier,
    ) {
        IconButton(onClick = onClick, enabled = enabled, modifier = modifier) {
            Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "发送")
        }
    }

    /**
     * To edit danmaku and send it by [trailingIcon]
     */
    @Composable
    fun DanmakuTextField(
        value: String,
        onValueChange: (String) -> Unit,
        onSend: () -> Unit = {},
        modifier: Modifier = Modifier,
        interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
        placeholder: @Composable () -> Unit = {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    DANMAKU_PLACEHOLDERS.random(),
                    Modifier.weight(1f),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )

                Text(
                    "发送弹幕仍在努力开发中, 仅供预览界面", Modifier.padding(start = 8.dp),
                    maxLines = 1,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        },
        leadingIcon: @Composable (() -> Unit)? = null,
        trailingIcon: @Composable (() -> Unit)? = {
            DanmakuSendButton(
                onClick = { onSend() },
                enabled = value.isNotBlank()
            )
        },
        enabled: Boolean = true,
        singleLine: Boolean = true,
        isError: Boolean = false,
        shape: Shape = MaterialTheme.shapes.medium,
        style: TextStyle = MaterialTheme.typography.bodyMedium,
    ) {
        val colors: TextFieldColors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surface.stronglyWeaken(),
            focusedContainerColor = MaterialTheme.colorScheme.surface.stronglyWeaken(),
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = Color.Transparent,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface.slightlyWeaken(),
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
        )
        MaterialTheme(aniLightColorTheme()) {
            BasicTextField(
                value,
                onValueChange,
                modifier.onKey(Key.Enter) {
                    onSend()
                }.height(38.dp),
                textStyle = style.copy(color = colors.unfocusedTextColor),
                cursorBrush = SolidColor(rememberUpdatedState(if (isError) colors.errorCursorColor else colors.cursorColor).value),
                decorationBox = { innerTextField ->
                    OutlinedTextFieldDefaults.DecorationBox(
                        value,
                        innerTextField,
                        enabled = enabled,
                        singleLine = singleLine,
                        visualTransformation = VisualTransformation.None,
                        interactionSource = interactionSource,
                        contentPadding = PaddingValues(vertical = 7.dp, horizontal = 16.dp),
                        colors = colors,
                        placeholder = placeholder,
                        leadingIcon = leadingIcon,
                        trailingIcon = trailingIcon,
                        container = {
                            OutlinedTextFieldDefaults.ContainerBox(
                                enabled,
                                isError,
                                interactionSource,
                                colors,
                                shape = shape,
                            )
                        }
                    )
                }
            )
        }
    }

    /**
     * To enter/exit fullscreen
     */
    @Composable
    fun FullscreenIcon(
        isFullscreen: Boolean,
        onClickFullscreen: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        IconButton(
            onClick = onClickFullscreen,
            modifier
        ) {
            if (isFullscreen) {
                Icon(Icons.Rounded.FullscreenExit, contentDescription = "Exit Fullscreen", Modifier.size(32.dp))
            } else {
                Icon(Icons.Rounded.Fullscreen, contentDescription = "Enter Fullscreen", Modifier.size(32.dp))
            }
        }
    }
}

/**
 * The controller bar of a video player. Usually at the bottom of the screen (the video player).
 *
 * See [PlayerControllerDefaults] for components.
 *
 * @param startActions [PlayerControllerDefaults.PlaybackIcon], [PlayerControllerDefaults.DanmakuIcon]
 * @param progressIndicator [ProgressIndicator]
 * @param progressSlider [ProgressSlider]
 * @param danmakuEditor [PlayerControllerDefaults.DanmakuTextField]
 * @param endActions [PlayerControllerDefaults.FullscreenIcon]
 * @param expanded Whether the controller bar is expanded.
 * If `true`, the [progressIndicator] and [progressSlider] will be shown on a separate row above. The bottom row will contain a [danmakuEditor].
 * If `false`, the entire bar will be only one row. [danmakuEditor] will be ignored.
 */
@Composable
fun PlayerControllerBar(
    startActions: @Composable RowScope.() -> Unit,
    progressIndicator: @Composable RowScope.() -> Unit,
    progressSlider: @Composable RowScope.() -> Unit,
    danmakuEditor: @Composable RowScope.() -> Unit,
    endActions: @Composable RowScope.() -> Unit,
    expanded: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .clickable(remember { MutableInteractionSource() }, null, onClick = {}) // Consume touch event
            .padding(
                horizontal = if (expanded) 8.dp else 4.dp,
                vertical = if (expanded) 4.dp else 2.dp
            )
    ) {
        if (expanded) {
            Column {
                Row(Modifier.padding(start = 8.dp)) {
                    progressIndicator()
                }
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProvideTextStyle(MaterialTheme.typography.labelLarge) {
                        progressSlider()
                    }
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 播放 / 暂停按钮
            Row(verticalAlignment = Alignment.CenterVertically) {
                startActions()
            }

            ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                if (expanded) {
                    Column(Modifier.weight(1f)) {
                        Row {
                            danmakuEditor()
                        }
                    }
                } else {
                    Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        progressIndicator()
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProvideTextStyle(MaterialTheme.typography.labelLarge) {
                                progressSlider()
                            }
                        }
                    }
                }
            }


            Row(verticalAlignment = Alignment.CenterVertically) {
                endActions()
            }
        }
    }
}
