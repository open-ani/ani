package me.him188.ani.danmaku.server.data.mongodb

import kotlinx.coroutines.flow.toList
import me.him188.ani.danmaku.protocol.Danmaku
import me.him188.ani.danmaku.protocol.DanmakuInfo
import me.him188.ani.danmaku.server.data.DanmakuRepository
import me.him188.ani.danmaku.server.data.model.DanmakuModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MongoDanmakuRepositoryImpl : DanmakuRepository, KoinComponent {
    private val mongoCollectionProvider: MongoCollectionProvider by inject()
    private val danmakuTable = mongoCollectionProvider.danmakuTable

    override suspend fun add(episodeId: String, danmakuInfo: DanmakuInfo, userId: String): Boolean {
        return danmakuTable.insertOne(
            DanmakuModel(
                senderId = userId,
                episodeId = episodeId,
                playTime = danmakuInfo.playTime,
                location = danmakuInfo.location,
                text = danmakuInfo.text,
                color = danmakuInfo.color,
            )
        ).wasAcknowledged()
    }

    override suspend fun selectByEpisodeAndTime(
        episodeId: String,
        fromTime: Long,
        toTime: Long,
        maxCount: Int
    ): List<Danmaku> {
        return danmakuTable.find(
            (Field("episodeId") eq episodeId) and
                    (Field("playTime") gte fromTime) and
                    (Field("playTime") lt toTime)
        ).limit(maxCount).toList().map {
            Danmaku(
                id = it.id.toString(),
                senderId = it.senderId,
                danmakuInfo = DanmakuInfo(
                    playTime = it.playTime,
                    location = it.location,
                    text = it.text,
                    color = it.color
                )
            )
        }
    }
}