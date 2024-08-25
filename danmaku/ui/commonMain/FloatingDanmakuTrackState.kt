package me.him188.ani.danmaku.ui

import androidx.annotation.UiThread
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import me.him188.ani.danmaku.api.DanmakuPresentation
import me.him188.ani.danmaku.api.DanmakuSessionAlgorithm
import kotlin.jvm.JvmField

/**
 * 浮动弹幕
 */
class FloatingDanmakuTrackState(
    private val trackWidth: State<Int>,
    private val danmakuTrackProperties: DanmakuTrackProperties = DanmakuTrackProperties.Default,
) : DanmakuTrackState {
    /**
     * 正在发送的弹幕. 用于缓存后台逻辑帧发送的弹幕, 以便在下一 UI 帧开始渲染
     *
     * impl notes: 弹幕逻辑帧已经相当于有缓存, 这里不要缓存多余的, 可能造成滞后
     * @see DanmakuSessionAlgorithm
     */
    @PublishedApi
    internal val channel = Channel<DanmakuPresentation>(0, onBufferOverflow = BufferOverflow.SUSPEND)

    /**
     * 在屏幕中可见的弹幕
     */
    @Stable
    internal val visibleDanmaku: MutableList<FloatingDanmakuState> = SnapshotStateList() // Random Access is needed

    /**
     * 刚发送的还在屏幕右边边缘的弹幕
     */
    @Stable
    internal val startingDanmaku: MutableList<FloatingDanmakuState> = ArrayList() // actually contains only one element

    /**
     * 上次 [animateMove] 的速度. px/s
     */
    @JvmField
    var lastBaseSpeed: Float = 0f

    @JvmField
    var lastSafeSeparation: Float = 0f
    
    /**
     * 轨道位置偏移量, 会是负数. 轨道初始在屏幕右边缘.
     *
     * 使用时注意检查 [Float.isNaN]
     */
    var trackOffsetX: Float by mutableFloatStateOf(Float.NaN)
        internal set

    var populationVersion: Int by mutableIntStateOf(0)
        internal set
    
    /**
     * track 在屏幕里的 y offset
     */
    var trackPosOffsetY by mutableStateOf(0f)
        internal set
    
    internal inner class FloatingDanmakuState(val state: DanmakuState) {
        internal val posXInScreen by derivedStateOf { 
            state.offsetInsideTrack + if (trackOffsetX.isNaN()) 0f else trackOffsetX
        }
        internal val posYInScreen get() = trackPosOffsetY

        /**
         * 此弹幕是否已完全滑到屏幕左侧，表示此弹幕已消失
         */
        internal val isGone: Boolean
            get() {
                if (state.textWidth == -1) return false // not yet placed
                return posXInScreen + state.textWidth + danmakuTrackProperties.visibilitySafeArea <= 0
            }

        /**
         * 弹幕是否已经完全显示在屏幕上. 因为弹幕初始的时候是整个都在屏幕右边外面
         */
        internal fun isFullyVisible(
            safeSeparation: Float = lastSafeSeparation,
            layoutDirection: LayoutDirection = LayoutDirection.Ltr,
        ) = if (layoutDirection == LayoutDirection.Ltr) {
            posXInScreen + state.textWidth + safeSeparation + 
                    danmakuTrackProperties.visibilitySafeArea < trackWidth.value
        } else {
            posXInScreen - safeSeparation > 0
        }
    }

    override fun trySend(danmaku: DanmakuPresentation): Boolean = channel.trySend(danmaku).isSuccess

    /**
     * 挂起当前协程, 直到成功发送这条弹幕.
     */
    suspend inline fun send(danmaku: DanmakuPresentation) {
        // inline to avoid additional Continuation as this is called frequently
        channel.send(danmaku)
    }

    /**
     * 立即将弹幕放置到轨道中, 忽视轨道是否已满或是否有弹幕仍然占据了初始位置.
     */
    @UiThread
    fun place(
        presentation: DanmakuPresentation,
        offsetInsideTrack: Float = -trackOffsetX + trackWidth.value,
    ): DanmakuState {
        return DanmakuState(presentation, offsetInsideTrack = offsetInsideTrack).also {
            visibleDanmaku.add(FloatingDanmakuState(it))
            startingDanmaku.add(FloatingDanmakuState(it))
        }
    }

    @UiThread
    override fun clear() {
        @Suppress("ControlFlowWithEmptyBody")
        while (channel.tryReceive().isSuccess);
        visibleDanmaku.clear()
        startingDanmaku.clear()
    }

    /**
     * Called on every logical tick to update the state.
     */
    @UiThread
    internal fun tick(
        layoutDirection: LayoutDirection,
        safeSeparation: Float
    ) {
        lastSafeSeparation = safeSeparation
        // Remove the danmaku from the track when it is out of screen, 
        // so that Track view will remove the Danmaku view from the layout.
        val trackOffset = trackOffsetX
        if (trackOffset.isNaN()) return
        visibleDanmaku.removeAll { it.isGone }

        // Remove the danmaku from [startingDanmaku] if it is fully visible on the screen (with [safeSeparation]),
        // so that the track will receive the next danmaku and display it.
        startingDanmaku.removeAll { danmaku ->
            if (danmaku.state.textWidth == -1) return@removeAll false // not yet placed
            if (danmaku.posXInScreen < 0) return@removeAll true
            return@removeAll danmaku.isFullyVisible(safeSeparation, layoutDirection)
        }
    }
    
    @UiThread
    internal suspend fun receiveNewDanmaku() {
        if (trackWidth.value == 0) return
        if (trackOffsetX.isNaN()) return // track 还未放置
        if (visibleDanmaku.size >= danmakuTrackProperties.maxDanmakuInTrack) return // `>` is impossible, just to be defensive
        if (startingDanmaku.isNotEmpty()) return // 有弹幕仍然在屏幕右边

        val danmaku = channel.receiveCatching().getOrNull() ?: return
        place(danmaku)
    }

    /**
     * 匀速减少 [trackOffsetX].
     *
     * This function can be safely cancelled,
     * because it remembers the last [trackOffsetX].
     *
     * Returns when the animation has ended,
     * which means the danmaku has moved out of the screen (to the start).
     *
     * @param baseSpeed px/s
     */
    suspend fun animateMove(
        baseSpeed: Float,
    ) {
        lastBaseSpeed = baseSpeed
        val speed = -baseSpeed / 1_000_000_000f // px/ns
        if (trackOffsetX.isNaN()) {
            trackOffsetX = trackWidth.value.toFloat()
        }

        restartAnimation@ while (true) {
            val currentVersion = populationVersion
            val startOffset = trackOffsetX
            val startTime = withFrameNanos { it }

            while (true) { // for each frame
                // Update offset on every frame
                val shouldRestart = withFrameNanos {
                    val elapsed = it - startTime
                    val res = startOffset + speed * elapsed
                    if (currentVersion != populationVersion) { // 必须在赋值 trackOffset 之前检查
                        return@withFrameNanos true
                    }
                    trackOffsetX = res
                    false
                }
                if (shouldRestart) {
                    continue@restartAnimation
                }
            }
        }
    }
}