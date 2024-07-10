package me.him188.ani.app.data.models.episode

import me.him188.ani.app.data.models.UserInfo
import me.him188.ani.datasources.bangumi.next.models.BangumiNextGetSubjectEpisodeComments200ResponseInner

data class EpisodeComment(
    val id: Int,
    val createdAt: Int,
    val content: String,
    val episodeId: Int,
    val state: Int,
    val creator: UserInfo,
    val replies: List<EpisodeComment> = listOf()
)

fun BangumiNextGetSubjectEpisodeComments200ResponseInner.toEpisodeComment() = EpisodeComment(
    id = id,
    createdAt = createdAt,
    content = content,
    episodeId = epID,
    state = state,
    creator = user?.let { u ->
        UserInfo(
            id = u.id,
            nickname = u.nickname,
            username = u.id.toString(),
            avatarUrl = u.avatar.medium,
        ) // 没有username
    } ?: UserInfo.EMPTY,
    replies = replies.map { r ->
        EpisodeComment(
            id = r.id,
            createdAt = r.createdAt,
            content = r.content,
            episodeId = r.epID,
            state = r.state,
            creator = r.user?.let { u ->
                UserInfo(
                    id = u.id,
                    nickname = u.nickname,
                    username = u.id.toString(),
                    avatarUrl = u.avatar.medium,
                )
            } ?: UserInfo.EMPTY,
        )
    },
)