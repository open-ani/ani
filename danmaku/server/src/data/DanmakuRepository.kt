package me.him188.ani.danmaku.server.data

interface DanmakuRepository {
    fun add()
    fun selectByEpisodeAndTime()
}

class InMemoryDanmakuRepositoryImpl : DanmakuRepository {
    override fun add() {
        TODO("Not yet implemented")
    }

    override fun selectByEpisodeAndTime() {
        TODO("Not yet implemented")
    }
}

class MongoDbDanmakuRepositoryImpl : DanmakuRepository {
    override fun add() {
        TODO("Not yet implemented")
    }

    override fun selectByEpisodeAndTime() {
        TODO("Not yet implemented")
    }
}
