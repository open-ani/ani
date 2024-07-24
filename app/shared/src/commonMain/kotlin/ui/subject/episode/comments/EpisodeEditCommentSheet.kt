package me.him188.ani.app.ui.subject.episode.comments

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.him188.ani.app.ui.subject.components.comment.EditCommentSheet
import me.him188.ani.app.ui.subject.components.comment.EditCommentSheetDefault
import me.him188.ani.app.ui.subject.episode.EpisodeViewModel

@Composable
fun EpisodeEditCommentSheet(
    vm: EpisodeViewModel,
    focusRequester: FocusRequester
) {
    val editCommentSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    ModalBottomSheet(
        sheetState = editCommentSheetState,
        onDismissRequest = {
            scope.launch { editCommentSheetState.hide() }
        },
    ) {
        Column(Modifier.padding(all = 16.dp)) {
            var expanded by remember { mutableStateOf(false) }

            EditCommentSheet(
                title = {
                    EditCommentSheetDefault.Title("")
                },
                actionRow = {
                    EditCommentSheetDefault.ActionRow(
                        onSend = {
                            focusManager.clearFocus()
                            scope.launch { editCommentSheetState.hide() }
                        },
                    )
                },
                expanded = expanded,
                onClickExpanded = { expanded = it },
            ) {
                EditCommentSheetDefault.EditText(
                    value = "",
                    maxLine = if (expanded) null else 3,
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .composed {
                            if (expanded) {
                                fillMaxHeight().animateContentSize().weight(1.0f)
                            } else {
                                animateContentSize()
                            }
                        },
                    onValueChange = { },
                )
            }
        }
    }
    LaunchedEffect(true) {
        focusRequester.requestFocus()
    }
}