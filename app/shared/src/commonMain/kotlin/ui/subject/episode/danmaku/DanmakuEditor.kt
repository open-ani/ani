package me.him188.ani.app.ui.subject.episode.danmaku

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import me.him188.ani.app.ui.foundation.theme.aniDarkColorTheme
import me.him188.ani.app.videoplayer.ui.progress.PlayerControllerDefaults

@Composable
fun DanmakuEditor(
    text: String,
    onTextChange: (String) -> Unit,
    isSending: Boolean,
    placeholderText: String,
    onSend: (text: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val textUpdated by rememberUpdatedState(text)
    val onTextChangeUpdated by rememberUpdatedState(onTextChange)
    val onSendUpdated by rememberUpdatedState(onSend)
    MaterialTheme(aniDarkColorTheme()) {
        PlayerControllerDefaults.DanmakuTextField(
            text,
            onValueChange = onTextChange,
            modifier = modifier,
            onSend = {
                if (textUpdated.isEmpty()) return@DanmakuTextField
                val textSnapshot = textUpdated
                onTextChangeUpdated("")
                onSendUpdated(textSnapshot)
            },
            isSending = isSending,
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
