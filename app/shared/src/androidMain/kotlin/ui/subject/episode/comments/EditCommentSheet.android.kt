package me.him188.ani.app.ui.subject.episode.comments

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.subject.components.comment.EditComment

@Preview
@Composable
fun PreviewEditCommentSheet() {
    ProvideCompositionLocalsForPreview {
        EditComment(
            content = "本来以为这集可以来一波互相表白做爆点的，结果演变成了经典乱入女生房间的剧情了()",
            title = "评论: 我心里危险的东西 第二季 ep.24",
            onContentChange = { },
            onSend = { },
        )
    }
}