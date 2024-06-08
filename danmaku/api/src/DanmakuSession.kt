package me.him188.ani.danmaku.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.merge

interface DanmakuSession {
    val events: Flow<DanmakuEvent>

    /**
     * 尝试在下一逻辑帧重新填充弹幕
     */
    fun requestRepopulate()
}

fun List<DanmakuSession>.merge(): DanmakuSession {
    val self = this
    return object : DanmakuSession {
        override val events: Flow<DanmakuEvent> = self.map { it.events }.merge()
        override fun requestRepopulate() {
            self.forEach { it.requestRepopulate() }
        }
    }
}

private object EmptyDanmakuSession : DanmakuSession {
    override val events: Flow<DanmakuEvent> get() = emptyFlow()
    override fun requestRepopulate() {
    }
}

fun emptyDanmakuSession(): DanmakuSession = EmptyDanmakuSession
