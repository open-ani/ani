package me.him188.ani.danmaku.dandanplay

import me.him188.ani.danmaku.api.DanmakuEpisode
import me.him188.ani.danmaku.api.DanmakuMatcher
import me.him188.ani.danmaku.api.DanmakuProvider
import me.him188.ani.danmaku.api.DanmakuSearchRequest
import me.him188.ani.danmaku.api.DanmakuSession
import me.him188.ani.danmaku.api.TimeBasedDanmakuSession
import me.him188.ani.danmaku.dandanplay.data.toDanmakuOrNull
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger

class DandanplayDanmakuProvider(
    private val dandanplayClient: DandanplayClient,
) : DanmakuProvider {
    companion object {
        const val ID = "弹弹play"
        private val logger = logger<DandanplayDanmakuProvider>()
    }

    override val id: String get() = ID

    override suspend fun startSession(
        request: DanmakuSearchRequest,
        matcher: DanmakuMatcher,
    ): DanmakuSession? {
        val searchEpisodeResponse = dandanplayClient.searchEpisode(
            subjectName = request.subjectName,
            episodeName = "第${request.episodeSort.toString().removePrefix("0")}话",
            // 弹弹数据库有时候会只有 "第x话" 没有具体标题, 所以不带标题搜索就够了
        )
        logger.info { "Ep search result: ${searchEpisodeResponse}}" }
        val episodes = searchEpisodeResponse.animes.flatMap { it ->
            it.episodes.map { ep ->
                DanmakuEpisode(
                    id = ep.episodeId.toString(),
                    subjectName = it.animeTitle ?: "",
                    episodeName = ep.episodeTitle ?: "",
                )
            }
        }
        if (episodes.isNotEmpty()) {
            matcher.match(episodes)?.let {
                logger.info { "Matched episode by ep search: ${it.subjectName} - ${it.episodeName}" }
                return createSession(it.id.toLong(), 0)
            }
        }

        val resp = dandanplayClient.matchVideo(
            filename = request.filename,
            fileHash = request.fileHash,
            fileSize = request.fileSize,
            videoDuration = request.videoDuration
        )
        val match = if (resp.isMatched) {
            resp.matches.firstOrNull() ?: return null
        } else {
            matcher.match(resp.matches.map {
                DanmakuEpisode(
                    it.episodeId.toString(),
                    it.animeTitle, it.episodeTitle,
                )
            })?.let { match ->
                resp.matches.first { it.episodeId.toString() == match.id }
            } ?: return null
        }
        logger.info { "Best match by file match: ${match.animeTitle} - ${match.episodeTitle}" }
        val episodeId = match.episodeId
        return createSession(episodeId, (match.shift * 1000L).toLong())
    }

    private suspend fun createSession(
        episodeId: Long,
        shiftMillis: Long
    ): DanmakuSession {
        val list = dandanplayClient.getDanmakuList(episodeId = episodeId)
        return TimeBasedDanmakuSession.create(
            list.asSequence().mapNotNull { it.toDanmakuOrNull() },
            shiftMillis = shiftMillis,
        )
    }
}
