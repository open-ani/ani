package me.him188.ani.danmaku.ui

import androidx.annotation.UiThread
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import me.him188.ani.danmaku.api.Danmaku
import me.him188.ani.danmaku.api.DanmakuLocation
import me.him188.ani.danmaku.api.DanmakuPresentation
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import kotlin.concurrent.Volatile

@Stable
class DanmakuHostState(
    private val danmakuConfig: State<DanmakuConfig> = mutableStateOf(DanmakuConfig.Default),
    private val danmakuTrackProperties: DanmakuTrackProperties = DanmakuTrackProperties.Default,
) {
    val config by danmakuConfig
    
    private val isPausedState = mutableStateOf(false)
    var isPaused: Boolean by isPausedState
        internal set
    
    // use this to repopulate danmaku
    @Volatile
    internal lateinit var textMeasurer: TextMeasurer
    
    // use this to repopulate danmaku
    @Volatile
    internal lateinit var baseStyle: TextStyle

    /**
     * text measurer 需要的缓存数量, 每一次 [DrawScope.drawDanmakuText] 都需要两次 measure text.
     * 
     * Canvas 每一帧绘制都需要 drawDanmakuText, 即 measure text. 
     * 这是非常重的工作. 需要 TextMeasurer 有足够的缓存来避免重复 measure.
     * 
     * 同屏最多的弹幕数量: `浮动弹幕轨道数量 * 每个轨道最多容纳弹幕数量 + 底部轨道数量 + 顶部轨道数量`.
     * 外加 20 个冗余缓存空间.
     */
    val textMeasurerCacheSize: Int 
        get() = danmakuTrackProperties.maxDanmakuInTrack * floatingTracks.size + bottomTracks.size + topTracks.size + 20

    /**
     * 轨道宽度，例如屏幕宽度
     */
    internal val trackWidthState = mutableStateOf(0)
    val trackWidth by trackWidthState

    /**
     * 轨道高度，包含 vertical padding
     */
    internal val trackHeightState = mutableStateOf(0)
    val trackHeight by trackHeightState

    /**
     * 浮动弹幕，显示为从右到左移动的弹幕
     */
    val floatingTracks: MutableList<FloatingDanmakuTrackState> = mutableStateListOf(
        FloatingDanmakuTrackState(trackWidthState, danmakuConfig, danmakuTrackProperties),
    )

    /**
     * 顶部弹幕，显示为屏幕顶部中间的弹幕
     */
    val topTracks: MutableList<FixedDanmakuTrackState> = mutableStateListOf(
        FixedDanmakuTrackState(isPausedState),
    )

    /**
     * 底部弹幕，显示为屏幕底部中间的弹幕
     */
    val bottomTracks: MutableList<FixedDanmakuTrackState> = mutableStateListOf(
        FixedDanmakuTrackState(isPausedState),
    )

    /**
     * 设置所有类型的弹幕允许使用的轨道个数.
     * 
     * 对于[顶部弹幕][topTracks]和底部弹幕[bottomTracks], 将设置最大可同时显示的数量.
     * 
     * 对于[浮动弹幕][floatingTracks], 将设置最大可显示浮动弹幕的轨道数量.
     */
    @UiThread
    fun setTrackCount(count: Int) {
        floatingTracks.setTrackCountImpl(
            count = count,
            newInstance = { FloatingDanmakuTrackState(trackWidthState, danmakuConfig, danmakuTrackProperties) },
        )
        topTracks.setTrackCountImpl(
            count = (count / 2).coerceAtLeast(1),
            newInstance = { FixedDanmakuTrackState(isPausedState) },
        )
        bottomTracks.setTrackCountImpl(
            count = (count / 2).coerceAtLeast(1),
            newInstance = { FixedDanmakuTrackState(isPausedState) },
        )
    }

    private inline fun <T> MutableList<T>.setTrackCountImpl(
        count: Int,
        newInstance: () -> T,
    ) {
        when {
            size == count -> return
            count < size -> repeat(size - count) { removeLast() }
            else -> addAll(List(count - size) { newInstance() })
        }
    }

    /**
     * Sends the [danmaku] to the first track that is currently ready to receive it.
     *
     * @return `true` if the [danmaku] was sent to a track, `false` if all tracks are currently occupied.
     */
    fun trySend(danmaku: DanmakuPresentation): Boolean {
        val tracks = getTracks(danmaku)
        return tracks.any { it.trySend(danmaku) }
    }

    /**
     * 清空所有弹幕轨道并重新填充
     */
    suspend fun repopulate(list: List<DanmakuPresentation>) {
        withContext(Dispatchers.Main.immediate) { // immediate: do not pay for dispatch
            // 还没 layout 之前等着
            while (!::textMeasurer.isInitialized
                || !::baseStyle.isInitialized
                || floatingTracks.fastAny { it.trackOffset.isNaN() }
            ) {
                delay(100)
            }
            runPopulate(list)
        }
    }

    private fun runPopulate(list: List<DanmakuPresentation>) {
        logger<DanmakuHostState>().info("repopulate danmaku, size = ${list.size}")
        val textMeasurer = textMeasurer

        // 重置所有轨道以及它们的偏移
        for (track in floatingTracks) {
            track.clear()
            track.trackOffset = 0f
            track.populationVersion++
        }

        if (list.isEmpty()) return // fast path

        class Track(
            val state: FloatingDanmakuTrackState,
        ) {
            var lastSent: Danmaku? = null

            /**
             * 是否有弹幕还没有完全显示
             */
            fun hasStartingDanmaku(): Boolean {
                if (state.startingDanmaku.isEmpty()) {
                    return false
                }
                return state.startingDanmaku.fastAny {
                    !it.isFullyVisible()
                }
            }
        }

        // TODO: repopulate 支持顶部和底部 tracks
        topTracks.fastForEach { it.clear() }
        bottomTracks.fastForEach { it.clear() }

        val tracks = floatingTracks.map { Track(it) }
        var curTrack = 0

        fun useNextTrackOrNull(): Track? {
            for (i in curTrack until (curTrack + tracks.size)) {
                val track = tracks[i % tracks.size]
                if (!track.hasStartingDanmaku()) {
                    curTrack = (i + 1) % tracks.size
                    return track
                }
            }
            return null
        }
        
        var lastSent: Danmaku? = null
        for (danmaku in list) { // 时间由旧到新
            // 调整到正确时间间隔 (模拟出两条弹幕之间的间隔)
            for (track in tracks) {
                lastSent ?: continue
                if (track.state.lastBaseSpeed.isNaN()) continue
                check(danmaku.danmaku.playTimeMillis >= lastSent.playTimeMillis) {
                    "danmaku list must be sorted by playTime"
                }
                val off =
                    (danmaku.danmaku.playTimeMillis - lastSent.playTimeMillis) / 1000f * track.state.lastBaseSpeed
                check(off >= 0f)
                track.state.trackOffset -= off
            }
            lastSent = danmaku.danmaku

            val track = useNextTrackOrNull() ?: continue // 所有轨道都有弹幕还未完全显示, 也就是都不能发弹幕, 跳过

            track.state.place(danmaku).apply { 
                measure(textMeasurer, baseStyle) 
            }
            track.lastSent = danmaku.danmaku
        }
    }

    /**
     * Pauses the movement of danmaku.
     */
    fun pause() {
        isPaused = true
    }

    /**
     * Resumes the movement of danmaku. Danmaku will continue to move from where it was paused.
     */
    fun resume() {
        isPaused = false
    }
}

/**
 * 发送弹幕, 挂起直到发送成功
 */
suspend inline fun DanmakuHostState.send(danmaku: DanmakuPresentation) {
    if (!trySend(danmaku)) {
        this.getTracks(danmaku).randomOrNull()?.send(danmaku)
    }
}

@PublishedApi
internal fun DanmakuHostState.getTracks(danmaku: DanmakuPresentation) =
    when (danmaku.danmaku.location) {
        DanmakuLocation.TOP -> topTracks
        DanmakuLocation.BOTTOM -> bottomTracks.asReversed()
        DanmakuLocation.NORMAL -> floatingTracks
    }