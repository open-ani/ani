package me.him188.ani.danmaku.dandanplay

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
        videoDuration: Duration
    ): DanmakuSession? {
        val resp = dandanplayClient.matchVideo(
            filename = filename,
            fileHash = fileHash,
            fileSize = fileSize,
            videoDuration = videoDuration
        )
        if (resp.matches.isEmpty()) {
            return null
        }
        val match = resp.matches.first()
        val episodeId = match.episodeId
        val list = dandanplayClient.getDanmakuList(episodeId = episodeId)
        return TimeBasedDanmakuSession.create(
            list.asSequence().mapNotNull { it.toDanmakuOrNull() },
            shiftMillis = (match.shift * 1000L).toLong(),
        )
    }
}
