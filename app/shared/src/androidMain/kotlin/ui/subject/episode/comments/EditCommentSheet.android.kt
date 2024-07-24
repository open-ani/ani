package me.him188.ani.app.ui.subject.episode.comments

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.ifThen
import me.him188.ani.app.ui.subject.components.comment.EditCommentSheet
import me.him188.ani.app.ui.subject.components.comment.EditCommentSheetDefault

@Preview
@Composable
fun PreviewEditCommentSheet() {
    ProvideCompositionLocalsForPreview {
        var expanded by remember { mutableStateOf(false) }

        EditCommentSheet(
            title = {
                EditCommentSheetDefault.Title("评论: 我心里危险的东西 第二季 ep.24")
            },
            actionRow = {
                EditCommentSheetDefault.ActionRow()
            },
            expanded = expanded,
            onClickExpanded = { expanded = !expanded },
        ) {
            EditCommentSheetDefault.EditText(
                value = "本来以为这集可以来一波互相表白做爆点的，结果演变成了经典乱入女生房间的剧情了()",
                maxLine = if (expanded) null else 3,
                modifier = Modifier
                    .ifThen(expanded) { fillMaxHeight().weight(1.0f) }
                    .animateContentSize(),
                onValueChange = { },
            )
        }

    }
}