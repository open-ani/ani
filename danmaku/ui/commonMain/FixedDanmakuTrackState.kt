package me.him188.ani.danmaku.ui

import androidx.annotation.UiThread
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import me.him188.ani.danmaku.api.DanmakuPresentation

/**
 * 顶部/底部的固定位置的弹幕
 */
@Stable
class FixedDanmakuTrackState(
    isPaused: State<Boolean>,
    /**
     * 每个弹幕显示的时长
     */
    private val displayDurationMillis: Long = 5000,
) : DanmakuTrackState {
    @PublishedApi
    internal val channel = Channel<DanmakuPresentation>(0, onBufferOverflow = BufferOverflow.SUSPEND)

    internal val isPaused by isPaused

    var visibleDanmaku: DanmakuState? by mutableStateOf(null)
    var sendTime: Long by mutableStateOf(0)

    /**
     * 尝试发送一条弹幕到这个轨道. 当轨道已满时返回 `false`.
     * @see channel
     */
    override fun trySend(danmaku: DanmakuPresentation): Boolean = channel.trySend(danmaku).isSuccess

    /**
     * 挂起当前协程, 直到成功发送这条弹幕.
     */
    suspend inline fun send(danmaku: DanmakuPresentation) {
        // inline to avoid additional Continuation as this is called frequently
        channel.send(danmaku)
    }

    override fun clear() {
        while (channel.tryReceive().getOrNull() != null) {
            // noop
        }
        visibleDanmaku = null
        sendTime = 0L
    }

    /**
     * Called on every frame to update the state.
     */
    @UiThread
    internal suspend fun receiveNewDanmaku(frameTime: Long) {
        if (visibleDanmaku != null) {
            if (frameTime - sendTime > displayDurationMillis) {
                // 时间到了, 清除
                visibleDanmaku = null
                sendTime = 0L
            } else {
                return // 有一个了, 时间还没到
            }
        }

        val danmaku = channel.receiveCatching().getOrNull() ?: return
        place(danmaku, frameTime)
    }

    /**
     * 立即将弹幕放置到轨道中, 忽视轨道是否已满或是否有弹幕仍然占据了初始位置.
     */
    @UiThread
    fun place(
        presentation: DanmakuPresentation,
        frameTime: Long,
    ): DanmakuState {
        return DanmakuState(presentation).also {
            visibleDanmaku = it
            sendTime = frameTime
        }
    }
}
