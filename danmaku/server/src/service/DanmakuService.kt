package me.him188.ani.danmaku.server.service

import me.him188.ani.danmaku.server.data.DanmakuRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface DanmakuService {
    fun postDanmaku(): Boolean
    fun getDanmaku(): List<Any>
}

class DanmakuServiceImpl : DanmakuService, KoinComponent {
    private val danmakuRepository: DanmakuRepository by inject()
    
    override fun postDanmaku(): Boolean {
        danmakuRepository.add()
        return true
    }

    override fun getDanmaku(): List<Any> {
        danmakuRepository.selectByEpisodeAndTime()
        return listOf()
    }
}
