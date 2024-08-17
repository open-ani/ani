package me.him188.ani.app.ui.subject.episode.comments

import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.ifThen
import me.him188.ani.app.ui.foundation.interaction.rememberImeMaxHeight
import me.him188.ani.app.ui.foundation.widgets.ModalBottomImeAwareSheet
import me.him188.ani.app.ui.foundation.widgets.rememberModalBottomImeAwareSheetState
import me.him188.ani.app.ui.subject.components.comment.CommentEditorState
import me.him188.ani.app.ui.subject.components.comment.EditComment

@Composable
fun EpisodeEditCommentSheet(
    state: CommentEditorState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val focusRequester = remember { FocusRequester() }
    val sheetState = rememberModalBottomImeAwareSheetState()
    
    val contentPadding = 16.dp
    val imePresentMaxHeight by rememberImeMaxHeight()

    ModalBottomImeAwareSheet(
        state = sheetState,
        onDismiss = onDismiss,
        modifier = Modifier
            .navigationBarsPadding()
            .ifThen(!state.showStickerPanel) { imePadding() },
    ) {
        EditComment(
            state = state,
            modifier = modifier
                .ifThen(state.editExpanded) { statusBarsPadding() }
                .ifThen(!state.editExpanded) { padding(top = contentPadding) }
                .padding(contentPadding),
            stickerPanelHeight = with(density) { imePresentMaxHeight.toDp() },
            focusRequester = focusRequester,
            onSendComplete = { sheetState.close() },
        )
    }
}