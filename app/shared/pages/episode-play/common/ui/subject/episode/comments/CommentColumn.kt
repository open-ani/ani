package me.him188.ani.app.ui.subject.episode.comments

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import me.him188.ani.app.tools.formatDateTime
import me.him188.ani.app.ui.external.placeholder.placeholder
import me.him188.ani.app.ui.foundation.avatar.AvatarImage
import org.openapitools.client.models.User


@Composable
fun CommentColumn(modifier: Modifier = Modifier) {
    Column {

    }
}

@Composable
fun Comment(
    author: User?,
    comment: Comment?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier
    ) {
        Row {
            Box(
                Modifier.clip(CircleShape)
            ) {
                AvatarImage(
                    author?.avatar?.small ?: "",
                    Modifier.size(36.dp)
                )
            }
            Column(Modifier.padding(start = 8.dp)) {
                Text(
                    author?.nickname ?: "nickname",
                    Modifier.placeholder(author?.nickname == null),
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    comment?.createdAt?.let {
                        formatDateTime(it)
                    } ?: "2021-01-01",
                    Modifier.placeholder(comment?.createdAt == null),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}
