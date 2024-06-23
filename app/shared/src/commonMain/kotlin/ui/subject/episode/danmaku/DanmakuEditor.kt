package me.him188.ani.app.ui.subject.episode.danmaku

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.app.ui.foundation.theme.aniDarkColorTheme
import me.him188.ani.app.ui.subject.episode.EpisodeViewModel
import me.him188.ani.app.videoplayer.ui.progress.PlayerControllerDefaults
import me.him188.ani.danmaku.protocol.DanmakuInfo
import me.him188.ani.danmaku.protocol.DanmakuLocation

@Composable
fun DanmakuEditor(
    vm: EpisodeViewModel,
    text: String,
    onTextChange: (String) -> Unit,
    placeholderText: String,
    setControllerVisible: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val textUpdated by rememberUpdatedState(text)
    val onTextChangeUpdated by rememberUpdatedState(onTextChange)
    MaterialTheme(aniDarkColorTheme()) {
        PlayerControllerDefaults.DanmakuTextField(
            text,
            onValueChange = onTextChange,
            modifier = modifier,
            onSend = remember(vm) {
                onSend@{
                    if (textUpdated.isEmpty()) return@onSend
                    val textSnapshot = textUpdated
                    onTextChangeUpdated("")
                    val exactPosition = vm.playerState.getExactCurrentPositionMillis()
                    vm.launchInBackground {
                        try {
                            danmaku.send(
                                episodeId = vm.episodeId,
                                DanmakuInfo(
                                    exactPosition,
                                    text = textSnapshot,
                                    color = Color.White.toArgb(),
                                    location = DanmakuLocation.NORMAL,
                                ),
                            )
                            withContext(Dispatchers.Main) { setControllerVisible(false) }
                        } catch (e: Throwable) {
                            withContext(Dispatchers.Main) { onTextChangeUpdated(textSnapshot) }
                            throw e
                        }
                    }
                }
            },
            isSending = vm.danmaku.isSending,
            placeholder = {
                Text(
                    placeholderText,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
            },
        )
    }
}
