package me.him188.ani.danmaku.ui.new

import androidx.annotation.UiThread
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import me.him188.ani.danmaku.api.Danmaku
import me.him188.ani.danmaku.api.DanmakuLocation
import me.him188.ani.danmaku.api.DanmakuPresentation
import me.him188.ani.danmaku.ui.DanmakuConfig
import me.him188.ani.danmaku.ui.DanmakuStyle
import me.him188.ani.danmaku.ui.DanmakuTrackProperties
import me.him188.ani.utils.platform.Uuid
import kotlin.concurrent.Volatile
import kotlin.jvm.JvmInline
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.time.Duration

@Stable
class DanmakuHostState(
    private val progress: Flow<Duration>,
    private val danmakuConfig: DanmakuConfig = DanmakuConfig.Default, // state 
    private val danmakuTrackProperties: DanmakuTrackProperties = DanmakuTrackProperties.Default, // state
) {
    /**
     * DanmakuHost 显示大小, 在显示时修改
     */
    private val hostWidthState = mutableIntStateOf(0)
    internal var hostWidth by hostWidthState
    private val hostHeightState = mutableIntStateOf(0)
    internal var hostHeight by hostHeightState
    
    @Volatile
    internal lateinit var baseStyle: TextStyle
    @Volatile
    internal lateinit var textMeasurer: TextMeasurer

    private val trackHeightState = mutableIntStateOf(0)

    /**
     * 当前播放时间, 读取 [progress] 并插帧过渡.
     * 
     * 为了避免弹幕跳动, 插帧过度必须平滑, 详见 [interpolateFrameLoop]
     */
    private val currentTimeMillisState = mutableLongStateOf(0)
    internal var currentTimeMillis by currentTimeMillisState
    
    private val floatingTrack: MutableList<FloatingDanmakuTrack> = mutableListOf()
    
    /**
     * All presented danmaku which should be shown on screen.
     */
    internal val presentDanmaku: MutableList<PositionedDanmakuState> = mutableStateListOf()

    /**
     * position of danmaku is calculated at [interpolateFrameLoop].
     */
    internal val presentDanmakuPositions: Array<Float> = Array(3000) { 0f }
    internal var presetDanmakuCount: Int by mutableIntStateOf(0)
    
    /**
     * Measure track count and track height and apply to states
     */
    internal suspend fun observeTrack(measurer: TextMeasurer, density: Density) {
        combine(
            snapshotFlow { danmakuConfig }.distinctUntilChanged { old, new ->
                old.displayArea == new.displayArea && old.style == new.style
            },
            snapshotFlow { hostHeight }.debounce(500)
        ) { config, height ->
            val dummyTextLayout = dummyDanmaku(measurer, baseStyle, config.style).solidTextLayout
            val verticalPadding = with(density) { (danmakuTrackProperties.verticalPadding * 2).dp.toPx() }
            
            val trackHeight = dummyTextLayout.size.height + verticalPadding
            val trackCount = height / trackHeight * config.displayArea

            Pair(
                trackCount.roundToInt().coerceAtLeast(1),
                trackHeight.toInt()
            )
        }
            .distinctUntilChanged()
            .collect { (trackCount, trackHeight) ->
                setTrackCount(trackCount)
                trackHeightState.value = trackHeight
            }
    }
    
    private fun setTrackCount(count: Int) {
        floatingTrack.setTrackCountImpl(count) { index ->
            FloatingDanmakuTrack(
                currentTimeMillis = currentTimeMillisState, 
                trackHeight = trackHeightState, 
                screenWidth = hostWidthState, 
                trackIndex = index, 
                speedPxPerSecond = danmakuConfig.speed.toInt(),
                onRemoveDanmaku = { removed -> presentDanmaku.remove(removed) }
            )
        }
    }

    private fun <T : DanmakuTrack> MutableList<T>.setTrackCountImpl(
        count: Int,
        newInstance: (index: Int) -> T,
    ) {
        when {
            size == count -> return
            // 清除 track 的同时要把 track 里的 danmaku 也要清除
            count < size -> repeat(size - count) { removeLast().clearAll() }
            else -> addAll(List(count - size) { newInstance(size + it) })
        }
    }

    /**
     * 根据 [progress] 为每一帧平滑插值到 [currentTimeMillis].
     */
    @UiThread
    internal suspend fun interpolateFrameLoop() {
        var latestUpstreamTimeMillis by atomic(0L)
        var restartInterpolate by atomic(true)
        
        coroutineScope {
            launch {
                progress.collectLatest {
                    latestUpstreamTimeMillis = it.inWholeMilliseconds
                    restartInterpolate = true
                }
            }
            launch {
                var elapsedFrame = ElapsedFrame(0, 0)
                
                while (true) {
                    val current = currentTimeMillis
                    val upstream = latestUpstreamTimeMillis
                    
                    val avgFrameTime = elapsedFrame.avg()
                    var glitched = false
                    
                    val interpolationBase = when {
                        /**
                         * initial state.
                         * 
                         *   current 
                         * ->> |v-----------------------------------------------------------|
                         * ->> |^-----------------------------------------------------------|
                         *   upstream
                         */
                        upstream == 0L && current == 0L -> { 0L }
                        /**
                         * no-op, just start interpolate.
                         *
                         *                     current
                         * ->> |-------------------v----------------------------------------|
                         * ->> |-------------------^----------------------------------------|
                         *                     upstream
                         */
                        current == upstream -> { current }
                        /**
                         * Diff between current time and upstream time is smaller than average frame time.
                         * so ust apply the maximum of these two to prevent glitch from interpolating.
                         * This branch should happens the most.
                         *
                         *                     current
                         * ->> |-------------------v----------------------------------------|
                         * ->> |------------(--^------------)-------------------------------|
                         *                 upstream         ^ avg time of each frame 
                         */
                        (current - upstream).absoluteValue <= avgFrameTime / 2 -> {
                            max(current, upstream)
                        }
                        /**
                         * Current time and upstream time differ two large, possible reason:
                         * 1. User click progress bar to airdrop to specific time, diff is smaller than repopulate threshold.
                         * 2. Frame rate becomes low when interpolating in interval of upstream updates.
                         * Glitch in interpolating cannot be avoided, use upstream time to ensure accuracy of time. 
                         *
                         *                     current
                         * ->> |-------------------v----------------------------------------|
                         * ->> |-^--(---------------)---------------------------------------|
                         *     upstream             ^ avg time of each frame 
                         */
                        else -> {
                            glitched = true
                            upstream.also { currentTimeMillis = it }
                        }
                    }
                    
                    var lastFrameTime = withFrameMillis { it }
                    if (glitched) { 
                        // interpolation is not necessary if glitched because current time is amended.
                        do {
                            withFrameMillis { millis ->
                                val delta = millis - lastFrameTime
                                currentTimeMillis += delta // update state
                                elapsedFrame = elapsedFrame.addDelta(delta)
                                lastFrameTime = millis
                            }
                            calculateDanmakuPosition()
                        } while (!restartInterpolate)
                    } else {
                        @Suppress("NAME_SHADOWING") var current = interpolationBase
                        @Suppress("NAME_SHADOWING") var upstream = latestUpstreamTimeMillis
                        do {
                            /**
                             * interpolate on each frame. `curr` should be interpolated and move close to `upstream next`.
                             * Here is the process of interpolation.
                             * 
                             * Case 1: current < upstream
                             *
                             *                      interp        interp
                             *       curr       (next)|       (next)|
                             * ->>----|v-----------|v-*-----------|v*----------------
                             * ->>----------|^-----------|^-----------|^-------------
                             *           upstream      (next)       (next)
                             *           
                             * Case 2: current > upstream
                             * 
                             *                      interp     interp
                             *             curr       |(next)    |(next)
                             * ->>----------|v--------*--|v------*|v-----------------
                             * ->>----|^-----------|^-----------|^------------------- 
                             *     upstream      (next)       (next)
                             */ 
                            withFrameMillis { millis ->
                                val delta = millis - lastFrameTime
                                
                                val interpolated = current + delta + (upstream - current) / 2
                                currentTimeMillis = interpolated
                                
                                current = interpolated
                                upstream += delta
                                
                                lastFrameTime = millis
                            }
                            calculateDanmakuPosition()
                        } while (!restartInterpolate)
                    }
                }
            }
        }
    }
    
    private inline fun calculateDanmakuPosition() {
        var currentIndex = 0
        for (danmaku in presentDanmaku.take(presentDanmakuPositions.size / 2)) {
            presentDanmakuPositions[currentIndex * 2] = danmaku.calculatePosX()
            presentDanmakuPositions[currentIndex * 2 + 1] = danmaku.calculatePosY()
            currentIndex ++
        }
        presetDanmakuCount = currentIndex
    }
    
    suspend fun send(danmaku: DanmakuPresentation) {
        while (!::baseStyle.isInitialized || !::textMeasurer.isInitialized) {
            delay(100)
        }
        when (danmaku.danmaku.location) {
            DanmakuLocation.NORMAL -> {
                val danmakuState = floatingTrack.firstNotNullOfOrNull { track ->
                    track.tryPlace(
                        DanmakuState(
                            presentation = danmaku,
                            measurer = textMeasurer,
                            baseStyle = baseStyle,
                            style = danmakuConfig.style,
                            enableColor = danmakuConfig.enableColor,
                            isDebug = danmakuConfig.isDebug,
                        )
                    )
                }
                // if danmakuState is not null, it means successfully placed.
                if (danmakuState != null) presentDanmaku.add(danmakuState)
            }
            else -> { }
        }
    }

    /**
     * 重置当前弹幕状态, 重新绘制弹幕
     */
    suspend fun invalidate() {
        while (!::baseStyle.isInitialized || !::textMeasurer.isInitialized) {
            delay(100)
        }
        
        val currentPresentDanmakuPresentation = presentDanmaku.map { it.state.presentation }
        repopulate(currentPresentDanmakuPresentation)
    }
    
    suspend fun repopulate(list: List<DanmakuPresentation>) {
        while (!::baseStyle.isInitialized || !::textMeasurer.isInitialized) {
            delay(100)
        }
        
        floatingTrack.forEach { it.clearAll() }
        
        for (danmaku in list) { send(danmaku) }
    }

    /**
     * DanmakuState which is positioned and an be placed on [Canvas].
     */
    interface PositionedDanmakuState {
        val state: DanmakuState
        
        fun calculatePosX(): Float
        fun calculatePosY(): Float
    }
}

// Elapsed time is used at [interpolateFrame].
// higher 32 bit = time, lower 32 bit = count
@JvmInline
private value class ElapsedFrame private constructor(private val packed: Long) {
    constructor(time: Long, count: Int) : this((time shl 32) or count.toLong())
    
    fun addDelta(delta: Long): ElapsedFrame {
        var time = packed shr 32
        time += delta
        return ElapsedFrame((time shl 32) or (packed and 0x0000_0000_ffff_ffff) + 1)
    }
    
    fun avg(): Long {
        val time = packed shr 32
        val count = packed and 0x0000_0000_ffff_ffff
        
        if (count == 0L) return 0L
        return time / count
    }
}

internal fun dummyDanmaku(
    measurer: TextMeasurer,
    baseStyle: TextStyle,
    style: DanmakuStyle,
): DanmakuState {
    return DanmakuState(
        presentation = DanmakuPresentation(
            Danmaku(
                Uuid.randomString(),
                "dummy",
                0L, "1",
                DanmakuLocation.NORMAL, "dummy 占位 攟 の 😄", 0,
            ),
            isSelf = false
        ),
        measurer = measurer,
        baseStyle = baseStyle,
        style = style,
        enableColor = false,
        isDebug = false
    )
}