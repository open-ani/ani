package me.him188.ani.danmaku.ui

import androidx.annotation.UiThread
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import me.him188.ani.danmaku.api.Danmaku
import me.him188.ani.danmaku.api.DanmakuLocation
import me.him188.ani.danmaku.api.DanmakuPresentation
import me.him188.ani.utils.logging.logger
import me.him188.ani.utils.logging.warn
import me.him188.ani.utils.platform.Uuid
import me.him188.ani.utils.platform.currentTimeMillis
import kotlin.concurrent.Volatile
import kotlin.math.absoluteValue
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
    // internal var delta: Long by mutableLongStateOf(0)
    // internal var restartEvent: String by mutableStateOf("")
    // internal var elapsedFrame: Long by mutableLongStateOf(0)
    // internal var elapsedFramePercent: Double by mutableDoubleStateOf(0.0)
    // internal var totalDiff: Long by mutableLongStateOf(0)
    // internal var totalPercent: Double by mutableDoubleStateOf(0.0)
    
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
                if (trackHeight != trackHeightState.value) {
                    trackHeightState.value = trackHeight
                }
                updateTrack(trackCount, config, density)
            }
    }

    /**
     * 更新弹幕轨道信息, 更新完成后调用 [invalidate] 显示新的信息.
     */
    private suspend fun updateTrack(count: Int, config: DanmakuConfig, density: Density) {
        val newFloatingTrackSpeed = with(density) { danmakuConfig.speed.dp.toPx() }
        val newFloatingTrackSafeSeparation = with(density) { danmakuConfig.safeSeparation.toPx() }
        floatingTrack.setTrackCountImpl(if (config.enableFloating) count else 0) { index ->
            FloatingDanmakuTrack(
                trackIndex = index,
                currentTimeMillis = currentTimeMillisState,
                trackHeight = trackHeightState,
                screenWidth = hostWidthState, 
                speedPxPerSecond = newFloatingTrackSpeed,
                safeSeparation = newFloatingTrackSafeSeparation,
                onRemoveDanmaku = { removed -> presentDanmaku.remove(removed) }
            )
        }
        floatingTrack.forEach {
            it.speedPxPerSecond = newFloatingTrackSpeed
            it.safeSeparation = newFloatingTrackSafeSeparation
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
        invalidate()
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
        var elapsedFrame = ElapsedFrame.zero()
        val progressTickTime = FastLongSumQueue(50)
        var currentUpstreamTickTime = 0L
        
        progress.map { it.inWholeMilliseconds }.collectLatest { upstreamTimeMillis ->
            val currentTimeMillis = currentTimeMillis
            val lastIntpAvgFrameTime = elapsedFrame.avg()
            val progressTickAvgTime = progressTickTime.avg()

            // restartEvent = "lastIntpAvgFrameTime: $lastIntpAvgFrameTime, progressTickAvgTime: $progressTickAvgTime\n" +
            //         "current: $currentTimeMillis, upstream: $upstreamTimeMillis"

            var interpolationBase = when {
                /**
                 * 初始状态
                 *
                 *   current
                 * ->> |v-----------------------------------------------------------|
                 * ->> |^-----------------------------------------------------------|
                 *   upstream
                 */
                upstreamTimeMillis == 0L && currentTimeMillis == 0L -> { 0L }
                /**
                 * upstream 和 current 相等
                 *
                 *                     current
                 * ->> |-------------------v----------------------------------------|
                 * ->> |-------------------^----------------------------------------|
                 *                     upstream
                 */
                currentTimeMillis == upstreamTimeMillis -> { currentTimeMillis }
                /**
                 * current 和 upstream 的差小于上一次插值循环的帧时间
                 * 每次插值循环的总帧时间是趋于稳定的, 所以我们使用前一次插值循环的总帧时间来作为这一次的抖动判断.
                 * 
                 * 如果 current 与 upstream 的差小于前一次插值循环的总帧时间, 
                 * 根据预测可以判断通过插值可以让 current 在这一个插值循环中完全逼近 upstream
                 *
                 *                     current
                 * ->> |-------------------v----------------------------------------|
                 * ->> |------------(--^------------)-------------------------------|
                 *                 upstream         ^ lastIntpTotalFrameTime
                 */
                (currentTimeMillis - upstreamTimeMillis).absoluteValue <= progressTickAvgTime -> {
                    currentTimeMillis
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
                 *     upstream             ^ lastIntpTotalFrameTime
                 */
                else -> {
                    logger.warn { " diff = ${upstreamTimeMillis - currentTimeMillis}" }
                    upstreamTimeMillis.also { this@DanmakuHostState.currentTimeMillis = it }
                }
            }


            // this@DanmakuHostState.glitched = if (glitched) 1 else 0 // test only

            // 在这里使用了一帧的时间获取当前帧的时间，用上一次插值的平均帧时间补偿这一帧的运动
            var lastFrameTime = withFrameMillis {
                // frameVersion += 1 // test only
                interpolationBase += lastIntpAvgFrameTime
                this@DanmakuHostState.currentTimeMillis = interpolationBase
                
                if (currentUpstreamTickTime != 0L) progressTickTime += it - currentUpstreamTickTime
                currentUpstreamTickTime = it
                
                it
            }
            
            elapsedFrame = ElapsedFrame.zero()
            val timeDiff = upstreamTimeMillis - interpolationBase // interpolationBase = currentTimeMillis
            // this@DanmakuHostState.totalDiff = totalDiff // test only
            
            // 根据上一次插帧循环总帧时间预测这一次的插帧进度
            while (true) {
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
                    elapsedFrame += delta
                    
                    // this@DanmakuHostState.elapsedFrame = elapsedFrame.sum() // test only
                    // this@DanmakuHostState.elapsedFramePercent = elapsedFrame.sum() / progressTickAvgTime.toDouble() // test only

                    // 使用 20 个 progress tick 时间来预测
                    // 也就是我们假设接下来 20 个 progress tick upstream 和 current 是完全同步的
                    val elapsedFramePercent = if (progressTickAvgTime == 0L) 0.0 else
                            (elapsedFrame.sum() / progressTickAvgTime.toDouble()).coerceAtMost(1.0) / 20.0
                    this@DanmakuHostState.currentTimeMillis =
                        interpolationBase + (elapsedFramePercent * 20 * progressTickAvgTime + timeDiff).toLong()
                    
                    // this@DanmakuHostState.totalPercent = elapsedFramePercent // test only

                    lastFrameTime = millis
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
    private suspend fun invalidate() {
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