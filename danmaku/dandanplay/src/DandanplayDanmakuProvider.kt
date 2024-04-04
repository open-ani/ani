package me.him188.ani.danmaku.dandanplay

import me.him188.ani.danmaku.api.DanmakuEpisode
import me.him188.ani.danmaku.api.DanmakuMatcher
import me.him188.ani.danmaku.api.DanmakuProvider
import me.him188.ani.danmaku.api.DanmakuSession
import me.him188.ani.danmaku.api.TimeBasedDanmakuSession
import kotlin.time.Duration

class DandanplayDanmakuProvider(
    private val dandanplayClient: DandanplayClient,
) : DanmakuProvider {
    override suspend fun startSession(
        filename: String,
        fileHash: String?,
        fileSize: Long,
        videoDuration: Duration,
        matcher: DanmakuMatcher,
    ): DanmakuSession? {
        val resp = dandanplayClient.matchVideo(
            filename = filename,
            fileHash = fileHash,
            fileSize = fileSize,
            videoDuration = videoDuration
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
        val episodeId = match.episodeId
        val list = dandanplayClient.getDanmakuList(episodeId = episodeId)
        return TimeBasedDanmakuSession.create(
            list.asSequence().mapNotNull { it.toDanmakuOrNull() },
            shiftMillis = (match.shift * 1000L).toLong(),
        )
    }
}
