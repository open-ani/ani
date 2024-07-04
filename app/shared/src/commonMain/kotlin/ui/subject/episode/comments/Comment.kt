package me.him188.ani.app.ui.subject.episode.comments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import me.him188.ani.app.tools.formatDateTime
import me.him188.ani.app.ui.external.placeholder.placeholder
import me.him188.ani.app.ui.foundation.avatar.AvatarImage
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle


@Composable
fun CommentColumn(
    vm: CommentViewModel,
    modifier: Modifier = Modifier
) {
    val comments by vm.comments.collectAsStateWithLifecycle(emptyList())
    LazyColumn(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { }
        items(comments) {
            Comment(
                comment = it,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
        item { }
    }
}

private const val LOREM_IPSUM =
    "Ipsum dolor sit amet, consectetur adipiscing elit. Integer nec odio. Praesent libero. Sed cursus ante dapibus diam. Sed nisi. Nulla quis sem at nibh elementum imperdiet."

@Composable
fun Comment(
    comment: UiComment,
    modifier: Modifier = Modifier
) {
    val constraintSet = ConstraintSet {
        val (avatar, nickname, time, indicator, content) =
            createRefsFor("avatar", "nickname", "time", "indicator", "content")

        constrain(avatar) {
            top.linkTo(parent.top)
            start.linkTo(parent.start)
        }
        constrain(nickname) {
            top.linkTo(parent.top)
            start.linkTo(avatar.end, 16.dp)
        }
        constrain(time) {
            top.linkTo(nickname.bottom, 4.dp)
            start.linkTo(avatar.end, 16.dp)
        }
        constrain(indicator) {
            end.linkTo(parent.end)
            top.linkTo(parent.top)
        }
        constrain(content) {
            start.linkTo(avatar.end, 16.dp)
            top.linkTo(time.bottom, 16.dp)
            end.linkTo(parent.end)
            width = Dimension.fillToConstraints
        }
    }
    ConstraintLayout(
        constraintSet = constraintSet,
        modifier = modifier,
    ) {
        Box(
            Modifier.layoutId("avatar").clip(CircleShape),
        ) {
            AvatarImage(
                comment.avatarUrl ?: "",
                Modifier.size(36.dp),
            )
        }
        Text(
            comment.id,
            modifier = Modifier.layoutId("indicator"),
            style = MaterialTheme.typography.labelMedium,

            )
        SelectionContainer(modifier = Modifier.layoutId("nickname")) {
            Text(
                comment.nickname ?: "nickname",
                Modifier.placeholder(comment.nickname == null),
                style = MaterialTheme.typography.labelLarge,
            )
        }
        SelectionContainer(modifier = Modifier.layoutId("time")) {
            Text(
                formatDateTime(comment.createdAt),
                style = MaterialTheme.typography.labelMedium,
            )
        }
        SelectionContainer(modifier = Modifier.layoutId("content")) {
            Text(
                comment.summary,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
