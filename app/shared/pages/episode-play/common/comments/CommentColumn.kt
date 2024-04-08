package me.him188.ani.app.ui.subject.episode.comments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import me.him188.ani.app.data.repositories.Comment
import me.him188.ani.app.data.repositories.EpisodeRevisionRepository
import me.him188.ani.app.data.repositories.UserRepository
import me.him188.ani.app.tools.formatDateTime
import me.him188.ani.app.ui.external.placeholder.placeholder
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.avatar.AvatarImage
import me.him188.ani.utils.coroutines.runUntilSuccess
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.openapitools.client.models.User


class CommentViewModel(
    episodeId: Int,
) : AbstractViewModel(), KoinComponent {
    private val revisionRepository by inject<EpisodeRevisionRepository>()
    private val userRepo by inject<UserRepository>()

    val comments = revisionRepository.getCommentsByEpisodeId(episodeId)
        .flatMapMerge { comment ->
            flow {
                emit(UiComment(comment,
                    comment.authorUsername?.let { username ->
                        runUntilSuccess {
                            userRepo.getUserByUsername(username)
                        }
                    }
                ))
            }
        }
        .runningList()
        .shareInBackground()
}

@Immutable
class UiComment(
    val comment: Comment,
    val author: User?
)

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
                author = it.author,
                comment = it.comment,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        item { }
    }
}

private const val LOREM_IPSUM =
    "Ipsum dolor sit amet, consectetur adipiscing elit. Integer nec odio. Praesent libero. Sed cursus ante dapibus diam. Sed nisi. Nulla quis sem at nibh elementum imperdiet."

@Composable
fun Comment(
    author: User?,
    comment: Comment?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
    ) {
        // 头像, 昵称, 时间
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
                SelectionContainer {
                    Text(
                        author?.nickname ?: "nickname",
                        Modifier.placeholder(author?.nickname == null),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                SelectionContainer {
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

        Column(Modifier.padding(vertical = 8.dp)) {
            SelectionContainer {
                Text(
                    comment?.summary ?: LOREM_IPSUM,
                    Modifier.placeholder(comment?.summary == null),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        Row {

        }
    }
}
