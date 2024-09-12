package me.him188.ani.danmaku.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface DanmakuSession {
    val events: Flow<DanmakuEvent>

    /**
     * 尝试在下一逻辑帧重新填充弹幕
     */
    fun requestRepopulate()
}

private object EmptyDanmakuSession : DanmakuSession {
    override val events: Flow<DanmakuEvent> get() = flowOf(DanmakuEvent.Repopulate(emptyList(), 0))
    override fun requestRepopulate() {
    }
}

fun emptyDanmakuSession(): DanmakuSession = EmptyDanmakuSession
