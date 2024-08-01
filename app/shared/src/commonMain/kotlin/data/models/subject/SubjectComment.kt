package me.him188.ani.app.data.models.subject

import me.him188.ani.app.data.models.UserInfo
import me.him188.ani.datasources.bangumi.next.models.BangumiNextSubjectInterestCommentListInner

data class SubjectComment(
    val id: Int,
    val updatedAt: Int,
    val content: String,
    val creator: UserInfo,
    val rating: Int
)

fun BangumiNextSubjectInterestCommentListInner.toSubjectComment() = SubjectComment(
    id = 31 * comment.hashCode() + 31 * updatedAt + 31 * (user?.id ?: UserInfo.EMPTY.id),
    content = comment,
    updatedAt = updatedAt,
    rating = rate,
    creator = user?.let { u ->
        UserInfo(
            id = u.id,
            nickname = u.nickname,
            username = u.id.toString(),
            avatarUrl = u.avatar.medium,
        ) // 没有username
    } ?: UserInfo.EMPTY,
)