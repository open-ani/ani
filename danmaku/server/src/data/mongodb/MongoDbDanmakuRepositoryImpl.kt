package me.him188.ani.danmaku.server.data.mongodb

import me.him188.ani.danmaku.protocol.Danmaku
import me.him188.ani.danmaku.protocol.DanmakuInfo
import me.him188.ani.danmaku.server.data.DanmakuRepository

class MongoDbDanmakuRepositoryImpl : DanmakuRepository {
    override fun add(episodeId: String, danmakuInfo: DanmakuInfo, userId: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun selectByEpisodeAndTime(episodeId: String, fromTime: Double, toTime: Double, maxCount: Int): List<Danmaku> {
        TODO("Not yet implemented")
    }
}