package me.him188.ani.danmaku.ui

import androidx.annotation.UiThread
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.him188.ani.danmaku.api.Danmaku
import me.him188.ani.danmaku.api.DanmakuLocation
import me.him188.ani.danmaku.api.DanmakuPresentation
import me.him188.ani.utils.logging.logger
import me.him188.ani.utils.logging.warn
import me.him188.ani.utils.platform.Uuid
import kotlin.concurrent.Volatile
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.time.Duration

@Stable
class DanmakuHostState(
    private val progress: Flow<Duration>,
    danmakuConfigState: State<DanmakuConfig> = mutableStateOf(DanmakuConfig.Default), // state 
    private val danmakuTrackProperties: DanmakuTrackProperties = DanmakuTrackProperties.Default, // state
) {
    private val logger = logger<DanmakuHostState>()
    
    private val danmakuConfig by danmakuConfigState
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

    internal val trackHeightState = mutableIntStateOf(0)
    
    internal val canvasAlpha by derivedStateOf { danmakuConfig.style.alpha }
    internal var paused by mutableStateOf(false)
    

    /**
     * 当前播放时间, 读取 [progress] 并插帧过渡.
     * 
     * 为了避免弹幕跳动, 插帧过度必须平滑, 详见 [interpolateFrameLoop]
     */
    private val currentTimeMillisState = mutableLongStateOf(0)
    internal var currentTimeMillis by currentTimeMillisState

    /**
     * 弹幕轨道
     */
    internal val floatingTrack: MutableList<FloatingDanmakuTrack> = mutableListOf()
    internal val topTrack: MutableList<FixedDanmakuTrack> = mutableListOf()
    internal val bottomTrack: MutableList<FixedDanmakuTrack> = mutableListOf()
    
    /**
     * All presented danmaku which should be shown on screen.
     */
    internal val presentDanmaku: MutableList<PositionedDanmakuState> = mutableStateListOf()

    /**
     * position of danmaku is calculated at [interpolateFrameLoop].
     */
    // internal val presentDanmakuPositions: Array<Float> = Array(3000) { 0f }
    // internal var presetDanmakuCount: Int by mutableIntStateOf(0)
    
    // test only prop
    // internal var glitched: Int by mutableIntStateOf(0)
    // internal var delta: Long by mutableLongStateOf(0)
    // internal var frameVersion: Long by mutableLongStateOf(0)
    // internal var interpCurr: Long by mutableLongStateOf(0)
    // internal var interpUpst: Long by mutableLongStateOf(0)
    // internal var restartEvent: String by mutableStateOf("")
    
    /**
     * 监听 轨道数量, 轨道高度 和 弹幕配置项目的变化
     */
    internal suspend fun observeTrack(measurer: TextMeasurer, density: Density) {
        combine(
            snapshotFlow { danmakuConfig }.distinctUntilChanged(),
            snapshotFlow { hostHeight }.debounce(500)
        ) { config, height ->
            val dummyTextLayout = dummyDanmaku(measurer, baseStyle, config.style).solidTextLayout
            val verticalPadding = with(density) { (danmakuTrackProperties.verticalPadding * 2).dp.toPx() }

            val trackHeight = dummyTextLayout.size.height + verticalPadding
            val trackCount = height / trackHeight * config.displayArea

            Triple(
                trackCount.roundToInt().coerceAtLeast(1),
                trackHeight.toInt(),
                danmakuConfig
            )
        }
            .distinctUntilChanged()
            .collect { (trackCount, trackHeight, config) ->
                setTrackCount(trackCount, config, density)
                if (trackHeight != trackHeightState.value) {
                    trackHeightState.value = trackHeight
                }
                invalidate()
            }
    }
    
    // 更新所有浮动轨道的滚动速度
    private fun setTrackCount(count: Int, config: DanmakuConfig, density: Density) {
        floatingTrack.setTrackCountImpl(if (config.enableFloating) count else 0) { index ->
            FloatingDanmakuTrack(
                trackIndex = index,
                currentTimeMillis = currentTimeMillisState,
                trackHeight = trackHeightState,
                screenWidth = hostWidthState, 
                speedPxPerSecond = derivedStateOf { with(density) { danmakuConfig.speed.dp.toPx() } },
                safeSeparation = derivedStateOf { with(density) { danmakuConfig.safeSeparation.toPx() } },
                onRemoveDanmaku = { removed -> presentDanmaku.remove(removed) }
            )
        }
        topTrack.setTrackCountImpl(if (config.enableTop) count else 0) { index ->
            FixedDanmakuTrack(
                trackIndex = index,
                currentTimeMillis = currentTimeMillisState,
                trackHeight = trackHeightState,
                screenWidth = hostWidthState,
                screenHeight = hostHeightState,
                fromBottom = false,
                onRemoveDanmaku = { removed -> presentDanmaku.remove(removed) }
            )
        }
        bottomTrack.setTrackCountImpl(if (config.enableBottom) count else 0) { index ->
            FixedDanmakuTrack(
                trackIndex = index,
                currentTimeMillis = currentTimeMillisState,
                trackHeight = trackHeightState,
                screenWidth = hostWidthState,
                screenHeight = hostHeightState,
                fromBottom = true,
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
        var restartInterpolate by atomic(false)
        
        coroutineScope {
            launch {
                progress.collectLatest {
                    latestUpstreamTimeMillis = it.inWholeMilliseconds
                    restartInterpolate = true
                }
            }
            launch {
                var elapsedFrame = ElapsedFrame.zero()
                
                while (true) {
                    val current = currentTimeMillis
                    val upstream = latestUpstreamTimeMillis
                    val lastInterpolationAvgFrameTime = elapsedFrame.avg()
                    var glitched = false
                    
                    // restartEvent = "c: $current, u: $upstream, a: $avgFrameTime"
                    
                    var interpolationBase = when {
                        /**
                         * 初始状态
                         * 
                         *   current 
                         * ->> |v-----------------------------------------------------------|
                         * ->> |^-----------------------------------------------------------|
                         *   upstream
                         */
                        upstream == 0L && current == 0L -> { 0L }
                        /**
                         * upstream 和 current 相等
                         *
                         *                     current
                         * ->> |-------------------v----------------------------------------|
                         * ->> |-------------------^----------------------------------------|
                         *                     upstream
                         */
                        current == upstream -> { current }
                        /**
                         * current 和 upstream 的差小于 3 * 平均帧时间
                         * 通过插值来更新 current 以逼近 upstream, 插值更新会改变 current 的速度.
                         * 为什么阈值是 3 倍帧时间?
                         * - 突然的帧时间波动随时可能发生, 在可接受的范围内插值不会在视觉上感受到弹幕的速度变快或变慢.
                         *
                         *                     current
                         * ->> |-------------------v----------------------------------------|
                         * ->> |------------(--^------------)-------------------------------|
                         *                 upstream         ^ 3 * avgFrameTime
                         */
                        (current - upstream).absoluteValue <= lastInterpolationAvgFrameTime * 3 -> {
                            max(current, upstream)
                        }
                        /**
                         * current 和 upstream 差别过大, 可能原因:
                         * 1. [progress] flow 发生了重大改变, 例如用户用进度条调整视频时间.
                         * 2. 帧时间突然增大很多, 例如 CPU 突然满载.
                         * 出现这种情况时没办法避免弹幕的抖动, 直接使用 upstream 时间来确保弹幕的准确性. 
                         *
                         *                     current
                         * ->> |-------------------v----------------------------------------|
                         * ->> |-^--(---------------)---------------------------------------|
                         *     upstream             ^ avgFrameTime
                         */
                        else -> {
                            glitched = true
                            upstream.also { currentTimeMillis = it }
                        }
                    }
                    
                    // this@DanmakuHostState.glitched = if (glitched) 1 else 0 // test only
                    
                    // 在这里使用了一帧的时间获取当前帧的时间，用上一次插值的平均帧时间补偿这一帧的运动
                    var lastFrameTime = withFrameMillis { 
                        // frameVersion += 1 // test only
                        interpolationBase += lastInterpolationAvgFrameTime
                        currentTimeMillis = interpolationBase
                        it
                    }
                    elapsedFrame = ElapsedFrame.zero()
                    
                    
                    if (glitched) {
                        // 如果弹幕抖动就不用插值.
                        do {
                            withFrameMillis { millis ->
                                // frameVersion += 1
                                val delta = millis - lastFrameTime
                                // this@DanmakuHostState.delta = delta // test only
                                
                                currentTimeMillis += delta // update state
                                
                                elapsedFrame = elapsedFrame.addDelta(delta)
                                lastFrameTime = millis
                            }
                        } while (!restartInterpolate)
                    } else {
                        var current1 = interpolationBase
                        var upstream1 = latestUpstreamTimeMillis
                        
                        do {
                            /**
                             * 在每一帧进行插值, 使 current 更逼近 upstream.
                             * 插值会导致 current 增加的速率发生变化: 如果 current < upstream, 那速度会稍微加快.
                             * 
                             * case 1: current < upstream
                             *
                             *                      interp        interp
                             *       curr       (next)|       (next)|
                             * ->>----|v-----------|v-*-----------|v*----------------
                             * ->>----------|^-----------|^-----------|^-------------
                             *           upstream      (next)       (next)
                             *           
                             * case 2: current > upstream
                             * 
                             *                      interp     interp
                             *             curr       |(next)    |(next)
                             * ->>----------|v--------*--|v------*|v-----------------
                             * ->>----|^-----------|^-----------|^------------------- 
                             *     upstream      (next)       (next)
                             */ 
                            withFrameMillis { millis ->
                                val delta = millis - lastFrameTime
                                // this@DanmakuHostState.delta = delta // test only
                                
                                val interpolated = current1 + delta + (upstream1 - current1) / 2
                                currentTimeMillis = interpolated
                                
                                // this@DanmakuHostState.interpCurr = current1 // test only
                                // this@DanmakuHostState.interpUpst = upstream1 // test only
                                
                                current1 = interpolated
                                upstream1 += delta

                                elapsedFrame = elapsedFrame.addDelta(delta)
                                lastFrameTime = millis
                            }
                        } while (!restartInterpolate)
                    }
                    restartInterpolate = false
                }
            }
        }
    }
    
    @UiThread
    internal fun tick() {
        floatingTrack.forEach { it.tick() }
        topTrack.forEach { it.tick() }
        bottomTrack.forEach { it.tick() }
    }

    /**
     * send a danmaku to the host.
     */
    suspend fun send(danmaku: DanmakuPresentation) {
        while (!::baseStyle.isInitialized || !::textMeasurer.isInitialized) {
            delay(100)
        }
        
        fun createDanmakuState(): DanmakuState {
            return DanmakuState(
                presentation = danmaku,
                measurer = textMeasurer,
                baseStyle = baseStyle,
                style = danmakuConfig.style,
                enableColor = danmakuConfig.enableColor,
                isDebug = danmakuConfig.isDebug,
            )
        }
        
        withContext(Dispatchers.Main.immediate) {
            val positionedDanmakuState: PositionedDanmakuState? = when (danmaku.danmaku.location) {
                DanmakuLocation.NORMAL -> floatingTrack.firstNotNullOfOrNull { track ->
                    track.tryPlace(createDanmakuState())
                }
                else -> (if (danmaku.danmaku.location == DanmakuLocation.TOP) topTrack else bottomTrack)
                    .firstNotNullOfOrNull { track -> track.tryPlace(createDanmakuState()) }
            }
            // if danmakuState is not null, it means successfully placed.
            if (positionedDanmakuState != null) presentDanmaku.add(positionedDanmakuState)
        }
    }

    /**
     * 重置当前弹幕状态, 重新绘制弹幕
     */
    suspend fun invalidate() {
        while (!::baseStyle.isInitialized || !::textMeasurer.isInitialized) {
            delay(100)
        }
        
        withContext(Dispatchers.Main.immediate) {
            val currentPresentDanmakuPresentation = presentDanmaku.map { it.state.presentation }
            repopulate(currentPresentDanmakuPresentation)
        }
    }
    
    suspend fun repopulate(list: List<DanmakuPresentation>) {
        if (list.isEmpty()) return
        
        while (!::baseStyle.isInitialized || !::textMeasurer.isInitialized) {
            delay(100)
        }
        
        withContext(Dispatchers.Main.immediate) {
            floatingTrack.forEach { it.clearAll() }
            topTrack.forEach { it.clearAll() }
            bottomTrack.forEach { it.clearAll() }
            
            if (presentDanmaku.isNotEmpty()) {
                logger.warn { "presentDanmaku is not totally cleared after releasing track. This may cause memory leak" }
                presentDanmaku.clear()
            }

            for (danmaku in list) { send(danmaku) }
        }
    }
    
    @UiThread
    fun setPause(pause: Boolean) {
        paused = pause
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