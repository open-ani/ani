package me.him188.ani.app.ui.subject.episode.details.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddTask
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.subject.collection.EditableSubjectCollectionTypeState
import me.him188.ani.datasources.api.topic.UnifiedCollectionType


@Composable
fun EpisodeWatchStatusButton(
    state: EditableSubjectCollectionTypeState,
    modifier: Modifier = Modifier,
) {
    EpisodeWatchStatusButton(
        state.isDone,
        onUnmark = {
            state.setSelfCollectionType(UnifiedCollectionType.NOT_COLLECTED)
        },
        onMarkAsDone = {
            state.setSelfCollectionType(UnifiedCollectionType.DONE)
        },
        modifier = modifier,
    )
}

@Composable
private fun EpisodeWatchStatusButton(
    isDone: Boolean,
    onUnmark: () -> Unit,
    onMarkAsDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier,
        contentAlignment = Alignment.CenterEnd,
    ) {
        if (isDone) {
            IconButton(
                onClick = onUnmark,
                Modifier.offset(x = 8.dp),
            ) {
                Icon(Icons.Outlined.TaskAlt, null)
            }
//            AssistChip(
//                onClick = onUnmark,
//                label = {
//                    Text("已看")
//                },
//                leadingIcon = {
//                    Icon(Icons.Outlined.TaskAlt, null)
//                },
//                colors = AssistChipDefaults.assistChipColors(
//                    leadingIconContentColor = LocalContentColor.current,
//                ),
//                border = null,
//            )
        } else {
            AssistChip(
                onClick = onMarkAsDone,
                label = {
                    Text("看过", softWrap = false)
                },
                leadingIcon = {
                    Icon(Icons.Outlined.AddTask, null)
                },
            )
        }
    }
}

