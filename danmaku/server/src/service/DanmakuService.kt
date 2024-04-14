package me.him188.ani.danmaku.server.service

import me.him188.ani.danmaku.protocol.Danmaku
import me.him188.ani.danmaku.protocol.DanmakuInfo
import me.him188.ani.danmaku.server.data.DanmakuRepository
import me.him188.ani.danmaku.server.util.exception.AcquiringTooMuchDanmakusException
import me.him188.ani.danmaku.server.util.exception.EmptyDanmakuException
import me.him188.ani.danmaku.server.util.exception.OperationFailedException
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.qualifier.named

interface DanmakuService {
    suspend fun postDanmaku(episodeId: String, danmakuInfo: DanmakuInfo, userId: String)
    suspend fun getDanmaku(episodeId: String, maxCount: Int, fromTime: Double, toTime: Double): List<Danmaku>
}

class DanmakuServiceImpl : DanmakuService, KoinComponent {
    private val danmakuRepository: DanmakuRepository by inject()
    
    override suspend fun postDanmaku(episodeId: String, danmakuInfo: DanmakuInfo, userId: String) {
        if (danmakuInfo.text.isEmpty()) {
            throw EmptyDanmakuException()
        }
        if (!danmakuRepository.add(episodeId, danmakuInfo, userId)) {
            throw OperationFailedException()
        }
    }

    override suspend fun getDanmaku(episodeId: String, maxCount: Int, fromTime: Double, toTime: Double): List<Danmaku> {
        if (maxCount > get<Int>(named("danmakuGetRequestMaxCountAllowed"))) {
            throw AcquiringTooMuchDanmakusException()
        }
        return danmakuRepository.selectByEpisodeAndTime(episodeId, fromTime, toTime, maxCount)
    }
}
