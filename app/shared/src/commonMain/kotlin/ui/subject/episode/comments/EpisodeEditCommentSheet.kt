package me.him188.ani.app.ui.subject.episode.comments

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.subject.components.comment.EditComment
import me.him188.ani.app.ui.subject.episode.EpisodeViewModel

@Composable
fun EpisodeEditCommentSheet(
    vm: EpisodeViewModel,
    show: Boolean,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusManager = LocalFocusManager.current

    if (show) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = onDismiss,
        ) {
            Column(Modifier.padding(all = 16.dp)) {
                EditComment(
                    content = "",
                    title = "评论: ${vm.episodeDetailsState.subjectTitle}",
                    onContentChange = { },
                    onSend = {
                        focusManager.clearFocus()
                        onDismiss()
                    },
                )
            }
        }
    }
}