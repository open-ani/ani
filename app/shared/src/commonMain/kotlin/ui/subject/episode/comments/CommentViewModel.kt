package me.him188.ani.app.ui.subject.episode.comments

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import me.him188.ani.app.data.repository.EpisodeRevisionRepository
import me.him188.ani.app.data.repository.UserRepository
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.utils.coroutines.runUntilSuccess
import me.him188.ani.utils.coroutines.runningList
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CommentViewModel(
    episodeId: Int,
) : AbstractViewModel(), KoinComponent {
    private val revisionRepository by inject<EpisodeRevisionRepository>()
    private val userRepo by inject<UserRepository>()

    val comments = revisionRepository.getCommentsByEpisodeId(episodeId)
        .flatMapMerge { comment ->
            flow {
                val user = comment.authorUsername?.let { username ->
                    runUntilSuccess {
                        userRepo.getUserByUsername(username)
                    }
                }
                emit(
                    UiComment(
                        id = comment.id,
                        type = comment.type,
                        summary = comment.summary,
                        createdAt = comment.createdAt,
                        nickname = user?.nickname,
                        avatarUrl = user?.avatar?.small,
                    ),
                )
            }
        }
        .runningList()
        .shareInBackground()
}

@Immutable
class UiComment(
    val id: String,
    val type: Int,
    val summary: String,
    val createdAt: Long, // timestamp millis

    val nickname: String?,
    val avatarUrl: String?,
)