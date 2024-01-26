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
        val match = dandanplayClient.matchVideo(
            filename = filename,
            fileHash = fileHash,
            fileSize = fileSize,
            videoDuration = videoDuration
        )
        if (match.matches.isEmpty()) {
            return null
        }
        val episodeId = match.matches.first().episodeId
        val list = dandanplayClient.getDanmakuList(episodeId = episodeId)
        return TimeBasedDanmakuSession.create(list.asSequence().mapNotNull { it.toDanmakuOrNull() })
    }
}
